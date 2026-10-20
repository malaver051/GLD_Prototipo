package com.gld.identidad.service;

import com.gld.identidad.dto.*;
import com.gld.identidad.exception.ReglaNegocioException;
import com.gld.identidad.model.*;
import com.gld.identidad.repository.MunicipioRepository;
import com.gld.identidad.repository.RolRepository;
import com.gld.identidad.repository.SolicitudRestablecimientoRepository;
import com.gld.identidad.repository.UsuarioRepository;
import com.gld.identidad.support.GeneradorClaveTemporal;
import com.gld.identidad.support.ValidadorDocumento;
import com.gld.identidad.support.notificacion.NotificadorCorreo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Consola del administrador: alta de funcionarios, activación y restablecimiento de contraseñas.
 * La contraseña temporal la genera el sistema y llega al funcionario por correo (simulado en consola).
 */
@Service
@Transactional
public class GestionUsuariosService {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final MunicipioRepository municipios;
    private final SolicitudRestablecimientoRepository solicitudes;
    private final GeneradorClaveTemporal generadorClave;
    private final PasswordEncoder codificador;
    private final ValidadorDocumento validador;
    private final NotificadorCorreo notificador;

    public GestionUsuariosService(UsuarioRepository usuarios, RolRepository roles, MunicipioRepository municipios,
                                  SolicitudRestablecimientoRepository solicitudes,
                                  GeneradorClaveTemporal generadorClave, PasswordEncoder codificador,
                                  ValidadorDocumento validador, NotificadorCorreo notificador) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.municipios = municipios;
        this.solicitudes = solicitudes;
        this.generadorClave = generadorClave;
        this.codificador = codificador;
        this.validador = validador;
        this.notificador = notificador;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDetalle> listar(NombreRol rol, String texto) {
        if (rol == NombreRol.ADMIN) {
            throw ReglaNegocioException.invalido("Solo se pueden consultar funcionarios o ciudadanos.");
        }
        String filtro = texto == null ? "" : texto.trim();
        return usuarios.buscarPorRol(rol, filtro).stream().map(UsuarioDetalle::de).toList();
    }

    /** Roles que el administrador puede asignar (ámbito GOBIERNO). */
    @Transactional(readOnly = true)
    public List<RolDto> rolesAsignables() {
        return roles.findByAmbito(AmbitoRol.GOBIERNO).stream().map(RolDto::de).toList();
    }

    public UsuarioDetalle crearFuncionario(NuevoFuncionario datos) {
        String correo = datos.correo().trim().toLowerCase();
        if (!Usuario.esCorreoDeGobierno(correo)) {
            throw ReglaNegocioException.invalido("El correo debe terminar en @gobierno.com.");
        }
        if (usuarios.existsByCorreoIgnoreCase(correo)) {
            throw ReglaNegocioException.conflicto("Ya existe una cuenta con este correo.");
        }
        String documento = validador.normalizar(datos.tipoDocumento(), datos.numeroDocumento());
        if (usuarios.existsByTipoDocumentoAndNumeroDocumentoIgnoreCase(datos.tipoDocumento(), documento)) {
            throw ReglaNegocioException.conflicto("Ya existe una cuenta con este documento.");
        }
        Municipio municipio = municipios.findById(datos.idMunicipio())
                .filter(Municipio::estaActivo)
                .orElseThrow(() -> ReglaNegocioException.invalido("El municipio elegido no está disponible."));
        Rol rol = roles.findById(datos.idRol())
                .filter(Rol::esAsignablePorAdministrador)
                .orElseThrow(() -> ReglaNegocioException.invalido("El rol elegido no se puede asignar a un funcionario."));

        String claveTemporal = generadorClave.generar();
        Usuario funcionario = Usuario.crearFuncionario(
                datos.nombreCompleto().trim(),
                datos.tipoDocumento(),
                documento,
                correo,
                validador.normalizarTelefono(datos.telefono()),
                municipio,
                codificador.encode(claveTemporal),
                rol);
        usuarios.save(funcionario);

        notificador.enviar(correo, "Tu cuenta en la consola municipal GLD",
                "Hola, " + funcionario.getNombreCompleto() + ".\n\n"
                        + "El administrador creó tu cuenta para " + municipio.getNombre() + ".\n"
                        + "Usuario: " + correo + "\n"
                        + "Contraseña temporal: " + claveTemporal + "\n\n"
                        + "Al ingresar por primera vez deberás cambiarla por una que solo tú conozcas.");
        return UsuarioDetalle.de(funcionario);
    }

    public UsuarioDetalle cambiarEstado(Integer idUsuario, boolean activo) {
        Usuario usuario = buscarGestionable(idUsuario);
        if (activo) {
            usuario.activar();
        } else {
            usuario.desactivar();
        }
        return UsuarioDetalle.de(usuario);
    }

    /** Genera una nueva contraseña temporal y cierra las solicitudes pendientes del funcionario. */
    public Mensaje restablecerClave(Integer idUsuario, Integer idAdministrador) {
        Usuario funcionario = buscarGestionable(idUsuario);
        if (!funcionario.esFuncionario()) {
            throw ReglaNegocioException.invalido(
                    "Los ciudadanos recuperan su contraseña con el enlace de «¿Olvidaste tu contraseña?».");
        }
        Usuario administrador = usuarios.getReferenceById(idAdministrador);

        String claveTemporal = generadorClave.generar();
        funcionario.asignarClaveTemporal(codificador.encode(claveTemporal));
        solicitudes.findByUsuarioAndEstado(funcionario, EstadoSolicitud.PENDIENTE)
                .forEach(s -> s.atender(administrador));

        notificador.enviar(funcionario.getCorreo(), "Restablecimiento de tu contraseña en GLD",
                "Hola, " + funcionario.getNombreCompleto() + ".\n\n"
                        + "El administrador restableció tu contraseña.\n"
                        + "Contraseña temporal: " + claveTemporal + "\n\n"
                        + "Al ingresar deberás cambiarla por una que solo tú conozcas.");
        return new Mensaje("Se envió una contraseña temporal a " + funcionario.getCorreo() + ".");
    }

    @Transactional(readOnly = true)
    public List<SolicitudRestablecimientoDto> solicitudesPendientes() {
        return solicitudes.listarPorEstado(EstadoSolicitud.PENDIENTE).stream()
                .map(SolicitudRestablecimientoDto::de).toList();
    }

    public Mensaje atenderSolicitud(Integer idSolicitud, Integer idAdministrador) {
        SolicitudRestablecimiento solicitud = solicitudes.findById(idSolicitud)
                .orElseThrow(() -> ReglaNegocioException.noEncontrado("La solicitud no existe."));
        if (!solicitud.estaPendiente()) {
            throw ReglaNegocioException.conflicto("Esta solicitud ya fue atendida.");
        }
        return restablecerClave(solicitud.getUsuario().getIdUsuario(), idAdministrador);
    }

    private Usuario buscarGestionable(Integer idUsuario) {
        Usuario usuario = usuarios.findById(idUsuario)
                .orElseThrow(() -> ReglaNegocioException.noEncontrado("El usuario no existe."));
        if (usuario.esAdministrador()) {
            throw ReglaNegocioException.prohibido("La cuenta del administrador no se puede modificar.");
        }
        return usuario;
    }
}
