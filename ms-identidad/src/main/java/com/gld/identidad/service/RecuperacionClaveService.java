package com.gld.identidad.service;

import com.gld.identidad.config.PropiedadesGld;
import com.gld.identidad.dto.EstadoEnlace;
import com.gld.identidad.dto.Mensaje;
import com.gld.identidad.dto.RespuestaRecuperacion;
import com.gld.identidad.dto.RestablecimientoClave;
import com.gld.identidad.exception.ReglaNegocioException;
import com.gld.identidad.model.EstadoSolicitud;
import com.gld.identidad.model.SolicitudRestablecimiento;
import com.gld.identidad.model.TokenRecuperacion;
import com.gld.identidad.model.Usuario;
import com.gld.identidad.repository.SolicitudRestablecimientoRepository;
import com.gld.identidad.repository.TokenRecuperacionRepository;
import com.gld.identidad.repository.UsuarioRepository;
import com.gld.identidad.support.Hash;
import com.gld.identidad.support.PoliticaContrasena;
import com.gld.identidad.support.notificacion.NotificadorCorreo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * "Olvidé mi contraseña".
 *  - Ciudadano: recibe un enlace por correo (simulado en consola) válido por 30 minutos.
 *  - Funcionario (@gobierno.com): queda una solicitud que atiende el administrador.
 * La respuesta es la misma exista o no la cuenta, para no revelar qué correos están registrados.
 */
@Service
@Transactional
public class RecuperacionClaveService {

    public static final String CANAL_CORREO = "ENLACE_CORREO";
    public static final String CANAL_ADMINISTRADOR = "ADMINISTRADOR";

    private final UsuarioRepository usuarios;
    private final TokenRecuperacionRepository tokens;
    private final SolicitudRestablecimientoRepository solicitudes;
    private final NotificadorCorreo notificador;
    private final PoliticaContrasena politica;
    private final PasswordEncoder codificador;
    private final String urlFrontend;
    private final int minutosVigencia;

    public RecuperacionClaveService(UsuarioRepository usuarios, TokenRecuperacionRepository tokens,
                                    SolicitudRestablecimientoRepository solicitudes, NotificadorCorreo notificador,
                                    PoliticaContrasena politica, PasswordEncoder codificador,
                                    PropiedadesGld propiedades) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.solicitudes = solicitudes;
        this.notificador = notificador;
        this.politica = politica;
        this.codificador = codificador;
        this.urlFrontend = propiedades.frontend().url();
        this.minutosVigencia = propiedades.recuperacion().minutosVigencia();
    }

    public RespuestaRecuperacion solicitar(String correoIngresado) {
        String correo = correoIngresado.trim().toLowerCase();
        Usuario usuario = usuarios.findByCorreoIgnoreCase(correo).orElse(null);

        if (Usuario.esCorreoDeGobierno(correo)) {
            if (usuario != null && usuario.esFuncionario() && !usuario.estaInactivo()
                    && !solicitudes.existsByUsuarioAndEstado(usuario, EstadoSolicitud.PENDIENTE)) {
                solicitudes.save(new SolicitudRestablecimiento(usuario));
            }
            return new RespuestaRecuperacion(CANAL_ADMINISTRADOR,
                    "Si la cuenta existe, el administrador recibió tu solicitud. Te enviará una contraseña "
                            + "temporal a tu correo institucional y deberás cambiarla al ingresar.");
        }

        if (usuario != null && usuario.esCiudadano() && !usuario.estaInactivo()) {
            tokens.invalidarVigentes(usuario);
            String token = Hash.tokenAleatorio();
            tokens.save(new TokenRecuperacion(usuario, Hash.sha256(token), minutosVigencia));
            notificador.enviar(usuario.getCorreo(), "Crea una nueva contraseña en GLD",
                    "Hola, " + usuario.getNombreCompleto() + ".\n\n"
                            + "Recibimos una solicitud para cambiar tu contraseña. Abre este enlace para crear una nueva:\n"
                            + urlFrontend + "/restablecer?token=" + token + "\n\n"
                            + "El enlace vence en " + minutosVigencia + " minutos y solo se puede usar una vez.\n"
                            + "Si no lo solicitaste, ignora este correo: tu contraseña actual sigue funcionando.");
        }
        return new RespuestaRecuperacion(CANAL_CORREO,
                "Si el correo está registrado, te enviamos un enlace para crear una nueva contraseña. "
                        + "Vence en " + minutosVigencia + " minutos.");
    }

    @Transactional(readOnly = true)
    public EstadoEnlace consultarEnlace(String token) {
        return tokens.findByTokenHash(Hash.sha256(token))
                .filter(TokenRecuperacion::estaVigente)
                .map(t -> new EstadoEnlace(true, ocultarCorreo(t.getUsuario().getCorreo())))
                .orElse(new EstadoEnlace(false, null));
    }

    public Mensaje restablecer(RestablecimientoClave datos) {
        TokenRecuperacion token = tokens.findByTokenHash(Hash.sha256(datos.token()))
                .filter(TokenRecuperacion::estaVigente)
                .orElseThrow(() -> ReglaNegocioException.invalido(
                        "El enlace venció o ya se usó. Solicita uno nuevo desde «¿Olvidaste tu contraseña?»."));
        politica.exigir(datos.nuevaContrasena(), datos.confirmacion());
        token.getUsuario().cambiarContrasena(codificador.encode(datos.nuevaContrasena()));
        token.marcarUsado();
        return new Mensaje("Tu contraseña se actualizó. Ya puedes ingresar con ella.");
    }

    /** ana.gomez@correo.com → an•••••••@correo.com */
    static String ocultarCorreo(String correo) {
        int arroba = correo.indexOf('@');
        if (arroba <= 2) {
            return correo;
        }
        return correo.substring(0, 2) + "•".repeat(arroba - 2) + correo.substring(arroba);
    }
}
