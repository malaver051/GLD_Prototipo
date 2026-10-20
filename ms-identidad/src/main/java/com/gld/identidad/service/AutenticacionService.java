package com.gld.identidad.service;

import com.gld.identidad.dto.*;
import com.gld.identidad.exception.ReglaNegocioException;
import com.gld.identidad.model.Usuario;
import com.gld.identidad.repository.UsuarioRepository;
import com.gld.identidad.security.JwtService;
import com.gld.identidad.security.PropositoToken;
import com.gld.identidad.support.PoliticaContrasena;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicio de sesión y cambio obligatorio de la clave temporal.
 *
 * Reglas:
 *  - admin entra con usuario "admin" y contraseña "admin", sin pasos adicionales.
 *  - Funcionarios: solo con correo @gobierno.com; si su clave es temporal, deben cambiarla.
 *  - Ciudadanos: siempre con segundo factor (Microsoft Authenticator).
 */
@Service
@Transactional
public class AutenticacionService {

    private static final String CREDENCIALES_INVALIDAS = "El correo o la contraseña no son correctos.";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;
    private final JwtService jwt;
    private final PoliticaContrasena politica;
    /** Hash ficticio: una cuenta inexistente tarda lo mismo que una contraseña errada. */
    private final String hashFicticio;

    public AutenticacionService(UsuarioRepository usuarios, PasswordEncoder codificador, JwtService jwt,
                                PoliticaContrasena politica) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.jwt = jwt;
        this.politica = politica;
        this.hashFicticio = codificador.encode("cuenta-inexistente");
    }

    public RespuestaIngreso ingresar(SolicitudIngreso solicitud) {
        String identificador = solicitud.identificador().trim().toLowerCase();
        Usuario usuario = usuarios.findByIdentificadorAutenticacionIgnoreCase(identificador).orElse(null);

        if (usuario == null) {
            codificador.matches(solicitud.contrasena(), hashFicticio);
            throw ReglaNegocioException.noAutorizado(CREDENCIALES_INVALIDAS);
        }
        if (!codificador.matches(solicitud.contrasena(), usuario.getContrasenaHash())) {
            throw ReglaNegocioException.noAutorizado(CREDENCIALES_INVALIDAS);
        }
        if (usuario.esFuncionario() && !Usuario.esCorreoDeGobierno(identificador)) {
            throw ReglaNegocioException.noAutorizado(CREDENCIALES_INVALIDAS);
        }
        if (usuario.estaInactivo()) {
            throw ReglaNegocioException.prohibido("Tu cuenta está desactivada. Comunícate con el administrador.");
        }

        if (usuario.requiereMfa()) {
            return usuario.tieneMfaConfigurado()
                    ? paso(usuario, PasoIngreso.VERIFICAR_MFA, PropositoToken.MFA)
                    : paso(usuario, PasoIngreso.CONFIGURAR_MFA, PropositoToken.MFA_CONFIGURACION);
        }
        if (usuario.isDebeCambiarClave()) {
            return paso(usuario, PasoIngreso.CAMBIAR_CLAVE, PropositoToken.CAMBIO_CLAVE);
        }
        return concederAcceso(usuario);
    }

    /** El funcionario reemplaza la clave temporal por una que solo él conoce. */
    public RespuestaIngreso cambiarClaveInicial(Integer idUsuario, CambioClave cambio) {
        Usuario usuario = buscarActivo(idUsuario);
        if (!usuario.isDebeCambiarClave()) {
            throw ReglaNegocioException.conflicto("Tu contraseña ya fue actualizada. Vuelve a ingresar.");
        }
        politica.exigir(cambio.nuevaContrasena(), cambio.confirmacion());
        if (codificador.matches(cambio.nuevaContrasena(), usuario.getContrasenaHash())) {
            throw ReglaNegocioException.invalido("La nueva contraseña debe ser distinta de la temporal.");
        }
        usuario.cambiarContrasena(codificador.encode(cambio.nuevaContrasena()));
        return concederAcceso(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioDetalle perfil(Integer idUsuario) {
        return UsuarioDetalle.de(buscarActivo(idUsuario));
    }

    // ------------------------------------------------------------------ apoyo

    public RespuestaIngreso concederAcceso(Usuario usuario) {
        usuario.registrarAcceso();
        return paso(usuario, PasoIngreso.ACCESO_CONCEDIDO, PropositoToken.ACCESO);
    }

    private RespuestaIngreso paso(Usuario usuario, PasoIngreso paso, PropositoToken proposito) {
        return new RespuestaIngreso(paso, jwt.emitir(usuario, proposito), UsuarioResumen.de(usuario));
    }

    public Usuario buscarActivo(Integer idUsuario) {
        Usuario usuario = usuarios.findById(idUsuario)
                .orElseThrow(() -> ReglaNegocioException.noAutorizado("Tu sesión no es válida. Vuelve a ingresar."));
        if (usuario.estaInactivo()) {
            throw ReglaNegocioException.prohibido("Tu cuenta está desactivada. Comunícate con el administrador.");
        }
        return usuario;
    }
}
