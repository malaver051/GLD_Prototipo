package com.gld.tramites.service;

import com.gld.tramites.dto.Resolucion;
import com.gld.tramites.dto.SolicitudDetalle;
import com.gld.tramites.dto.SolicitudResumen;
import com.gld.tramites.exception.ReglaNegocioException;
import com.gld.tramites.model.*;
import com.gld.tramites.repository.SolicitudTramiteRepository;
import com.gld.tramites.security.UsuarioAutenticado;
import com.gld.tramites.support.AlmacenamientoArchivos;
import com.gld.tramites.support.CalendarioHabil;
import com.gld.tramites.support.ClienteIdentidad;
import com.gld.tramites.support.ClienteIdentidad.PerfilIdentidad;
import com.gld.tramites.support.Codigos;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * Gestor_Tramites del diagrama de clases: coordina el ciclo de vida de una solicitud siguiendo el BPMN
 * (radicar, consultar, pedir información, corregir y resolver).
 *
 * GRASP · Controlador de caso de uso: recibe los eventos del sistema desde los controladores REST
 * y delega las reglas en las entidades (Experto) y en el patrón State.
 */
@Service
@Transactional
public class GestorTramitesService {

    public static final String PREFIJO_ARCHIVO = "requisito-";
    private static final int MAX_OBSERVACIONES = 1000;

    private final SolicitudTramiteRepository solicitudes;
    private final CatalogoService catalogo;
    private final ClienteIdentidad identidad;
    private final AlmacenamientoArchivos almacenamiento;
    private final CalendarioHabil calendario;
    private final DocumentoService documentos;
    private final NotificacionesTramite notificaciones;

    public GestorTramitesService(SolicitudTramiteRepository solicitudes, CatalogoService catalogo,
                                 ClienteIdentidad identidad, AlmacenamientoArchivos almacenamiento,
                                 CalendarioHabil calendario, DocumentoService documentos,
                                 NotificacionesTramite notificaciones) {
        this.solicitudes = solicitudes;
        this.catalogo = catalogo;
        this.identidad = identidad;
        this.almacenamiento = almacenamiento;
        this.calendario = calendario;
        this.documentos = documentos;
        this.notificaciones = notificaciones;
    }

    // ------------------------------------------------------------------ ciudadano

    /**
     * Método radicarSolicitud. BPMN: «Validar datos y adjuntos» → «¿Completa?» → «Radicar y asignar número».
     * Los archivos llegan con el nombre "requisito-{idRequisito}".
     */
    public SolicitudDetalle radicarSolicitud(UsuarioAutenticado ciudadano, Integer idTipoTramite, String observaciones,
                                             Map<String, MultipartFile> archivos) {
        TipoTramite tipo = catalogo.tipoDisponible(ciudadano, idTipoTramite);
        Map<RequisitoTramite, MultipartFile> adjuntos = asociarArchivos(tipo, archivos);

        List<String> faltantes = tipo.getRequisitosObligatorios().stream()
                .filter(r -> !adjuntos.containsKey(r))
                .map(RequisitoTramite::getNombre)
                .toList();
        if (!faltantes.isEmpty()) {
            throw new ReglaNegocioException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Faltan documentos obligatorios. Adjúntalos para radicar la solicitud.", faltantes);
        }
        adjuntos.forEach((r, a) -> almacenamiento.validar(a, r.getNombre()));
        String texto = limpiar(observaciones);

        PerfilIdentidad perfil = identidad.perfil(ciudadano);
        if (perfil.municipio() == null || !perfil.municipio().id().equals(ciudadano.idMunicipio())) {
            throw ReglaNegocioException.prohibido("Tu cuenta no tiene un municipio válido para radicar trámites.");
        }
        DatosCiudadano datos = new DatosCiudadano(perfil.id(), perfil.nombreCompleto(), perfil.correo(),
                perfil.documentoLegible(), perfil.municipio().id(), perfil.municipio().codigo(),
                perfil.municipio().nombre(), perfil.municipio().estado());

        String radicado = Codigos.radicado(perfil.municipio().codigo(), solicitudes.siguienteRadicado());
        SolicitudTramite s = SolicitudTramite.radicar(radicado, tipo, datos, texto,
                calendario.fechaLimiteDesdeHoy(tipo.getPlazoDias()));
        solicitudes.saveAndFlush(s);

        adjuntos.forEach((requisito, archivo) -> s.adjuntar(requisito, nombreOriginal(archivo),
                almacenamiento.guardar(s.getIdSolicitud(), archivo), almacenamiento.tipoDe(archivo),
                archivo.getSize(), ciudadano.idUsuario()));
        notificaciones.radicada(s);
        return detalle(s, ciudadano);
    }

    /** BPMN: «Información corregida». El ciudadano responde con un comentario y/o documentos. */
    public SolicitudDetalle reenviarCorreccion(UsuarioAutenticado ciudadano, Integer idSolicitud, String comentario,
                                               Map<String, MultipartFile> archivos) {
        SolicitudTramite s = solicitudDelCiudadano(ciudadano, idSolicitud);
        Map<RequisitoTramite, MultipartFile> adjuntos = asociarArchivos(s.getTipoTramite(), archivos);
        String texto = limpiar(comentario);
        if (texto == null && adjuntos.isEmpty()) {
            throw ReglaNegocioException.invalido("Escribe una respuesta o adjunta al menos un documento.");
        }
        adjuntos.forEach((r, a) -> almacenamiento.validar(a, r.getNombre()));

        String motivo = texto != null ? texto : "Corrección enviada.";
        if (!adjuntos.isEmpty()) {
            motivo += " Documentos nuevos: " + adjuntos.keySet().stream().map(RequisitoTramite::getNombre)
                    .reduce((a, b) -> a + ", " + b).orElse("") + ".";
        }
        s.reenviarCorreccion(motivo, ciudadano.idUsuario());
        adjuntos.forEach((requisito, archivo) -> s.adjuntar(requisito, nombreOriginal(archivo),
                almacenamiento.guardar(s.getIdSolicitud(), archivo), almacenamiento.tipoDe(archivo),
                archivo.getSize(), ciudadano.idUsuario()));
        return detalle(s, ciudadano);
    }

    // ------------------------------------------------------------------ consultas

    /**
     * Ciudadano: sus solicitudes. Funcionario: las de su municipio, por grupo de la bandeja.
     */
    @Transactional(readOnly = true)
    public List<SolicitudResumen> listar(UsuarioAutenticado usuario, String grupo) {
        var hoy = calendario.hoy();
        if (usuario.esCiudadano()) {
            return solicitudes.delCiudadano(usuario.idUsuario()).stream()
                    .map(s -> SolicitudResumen.de(s, hoy)).toList();
        }
        List<EstadoSolicitud> estados = switch (grupo == null ? "POR_REVISAR" : grupo) {
            case "EN_ESPERA" -> List.of(EstadoSolicitud.PENDIENTE_PAGO, EstadoSolicitud.REQUIERE_INFORMACION);
            case "RESUELTAS" -> List.of(EstadoSolicitud.APROBADA, EstadoSolicitud.RECHAZADA);
            case "TODAS" -> List.of(EstadoSolicitud.values());
            default -> List.of(EstadoSolicitud.EN_REVISION);
        };
        Comparator<SolicitudResumen> orden = "RESUELTAS".equals(grupo)
                ? Comparator.comparing(SolicitudResumen::fechaRadicacion).reversed()
                : Comparator.comparing(SolicitudResumen::fechaLimite, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(SolicitudResumen::fechaRadicacion);
        return solicitudes.delMunicipio(CatalogoService.municipioDe(usuario), estados).stream()
                .map(s -> SolicitudResumen.de(s, hoy))
                .sorted(orden)
                .toList();
    }

    /** Método consultarSolicitud. */
    @Transactional(readOnly = true)
    public SolicitudDetalle consultarSolicitud(UsuarioAutenticado usuario, Integer idSolicitud) {
        return detalle(solicitudVisible(usuario, idSolicitud), usuario);
    }

    @Transactional(readOnly = true)
    public ArchivoDescarga descargar(UsuarioAutenticado usuario, Integer idSolicitud, Integer idDocumento) {
        SolicitudTramite s = solicitudVisible(usuario, idSolicitud);
        Documento d = s.getDocumentos().stream()
                .filter(doc -> doc.getIdDocumento().equals(idDocumento))
                .findFirst()
                .orElseThrow(() -> ReglaNegocioException.noEncontrado("El documento no existe."));
        return new ArchivoDescarga(d.getNombreArchivo(), d.getTipoContenido(), documentos.contenido(d));
    }

    public record ArchivoDescarga(String nombre, String tipoContenido, byte[] contenido) {
    }

    // ------------------------------------------------------------------ funcionario

    /** Método solicitarCorreccion. BPMN: «¿Información adicional? → Sí». */
    public SolicitudDetalle solicitarInformacion(UsuarioAutenticado funcionario, Integer idSolicitud, String motivo) {
        SolicitudTramite s = solicitudDelMunicipio(funcionario, idSolicitud);
        PerfilIdentidad perfil = identidad.perfil(funcionario);
        String texto = limpiar(motivo);
        s.solicitarCorreccion(texto, funcionario.idUsuario(), perfil.nombreCompleto());
        notificaciones.informacionSolicitada(s, texto);
        return detalle(s, funcionario);
    }

    /** Método resolverSolicitud. BPMN: «¿Aprueba?» → emitir documento firmado o registrar rechazo. */
    public SolicitudDetalle resolverSolicitud(UsuarioAutenticado funcionario, Integer idSolicitud, Resolucion resolucion) {
        SolicitudTramite s = solicitudDelMunicipio(funcionario, idSolicitud);
        String texto = limpiar(resolucion.motivo());
        PerfilIdentidad perfil = identidad.perfil(funcionario);
        if (resolucion.aprobada()) {
            s.aprobar(texto != null ? texto : "Solicitud aprobada. Se emitió el documento oficial.",
                    funcionario.idUsuario(), perfil.nombreCompleto());
            Documento oficial = documentos.emitirOficial(s, funcionario.idUsuario(), perfil.nombreCompleto(), texto);
            notificaciones.aprobada(s, oficial.getCodigoVerificacion());
        } else {
            if (texto == null) {
                throw ReglaNegocioException.invalido("Escribe el motivo del rechazo; el ciudadano lo verá en su solicitud.");
            }
            s.rechazar(texto, funcionario.idUsuario(), perfil.nombreCompleto());
            notificaciones.rechazada(s, texto);
        }
        return detalle(s, funcionario);
    }

    // ------------------------------------------------------------------ acceso

    public SolicitudTramite solicitudDelCiudadano(UsuarioAutenticado ciudadano, Integer idSolicitud) {
        return solicitudes.findById(idSolicitud)
                .filter(s -> s.perteneceACiudadano(ciudadano.idUsuario()))
                .orElseThrow(() -> ReglaNegocioException.noEncontrado("La solicitud no existe."));
    }

    private SolicitudTramite solicitudDelMunicipio(UsuarioAutenticado funcionario, Integer idSolicitud) {
        return solicitudes.findById(idSolicitud)
                .filter(s -> s.perteneceAMunicipio(CatalogoService.municipioDe(funcionario)))
                .orElseThrow(() -> ReglaNegocioException.noEncontrado("La solicitud no existe en tu municipio."));
    }

    private SolicitudTramite solicitudVisible(UsuarioAutenticado usuario, Integer idSolicitud) {
        return usuario.esCiudadano()
                ? solicitudDelCiudadano(usuario, idSolicitud)
                : solicitudDelMunicipio(usuario, idSolicitud);
    }

    // ------------------------------------------------------------------ apoyo

    private SolicitudDetalle detalle(SolicitudTramite s, UsuarioAutenticado usuario) {
        solicitudes.flush(); // asigna ids a historial y documentos nuevos antes de responder
        return SolicitudDetalle.de(s, calendario.hoy(), accionesPermitidas(s, usuario));
    }

    /** Acciones que el frontend puede mostrar según el rol y el estado (patrón State). */
    private static List<String> accionesPermitidas(SolicitudTramite s, UsuarioAutenticado usuario) {
        List<String> acciones = new ArrayList<>();
        EstadoSolicitud estado = s.getEstado();
        if (usuario.esCiudadano()) {
            if (estado.admitePago()) acciones.add("PAGAR");
            if (estado == EstadoSolicitud.REQUIERE_INFORMACION) acciones.add("CORREGIR");
        } else if (estado == EstadoSolicitud.EN_REVISION) {
            acciones.addAll(List.of("SOLICITAR_INFORMACION", "APROBAR", "RECHAZAR"));
        }
        return acciones;
    }

    /** Relaciona cada archivo "requisito-{id}" con un requisito del trámite; ignora nombres desconocidos. */
    private static Map<RequisitoTramite, MultipartFile> asociarArchivos(TipoTramite tipo, Map<String, MultipartFile> archivos) {
        Map<RequisitoTramite, MultipartFile> resultado = new LinkedHashMap<>();
        if (archivos == null) {
            return resultado;
        }
        for (RequisitoTramite r : tipo.getRequisitos()) {
            MultipartFile archivo = archivos.get(PREFIJO_ARCHIVO + r.getIdRequisito());
            if (archivo != null && !archivo.isEmpty()) {
                resultado.put(r, archivo);
            }
        }
        return resultado;
    }

    private static String limpiar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String limpio = texto.strip();
        if (limpio.length() > MAX_OBSERVACIONES) {
            throw ReglaNegocioException.invalido("El texto puede tener hasta " + MAX_OBSERVACIONES + " caracteres.");
        }
        return limpio;
    }

    private static String nombreOriginal(MultipartFile archivo) {
        String nombre = archivo.getOriginalFilename();
        if (nombre == null || nombre.isBlank()) {
            return "archivo";
        }
        String soloNombre = nombre.replace("\\", "/");
        soloNombre = soloNombre.substring(soloNombre.lastIndexOf('/') + 1);
        return soloNombre.length() > 180 ? soloNombre.substring(soloNombre.length() - 180) : soloNombre;
    }
}
