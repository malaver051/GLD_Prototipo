package com.gld.tramites.dto;

import com.gld.tramites.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Vista completa de una solicitud para el ciudadano y el funcionario. */
public record SolicitudDetalle(
        Integer id,
        String numeroRadicado,
        String estado,
        String estadoEtiqueta,
        LocalDateTime fechaRadicacion,
        LocalDate fechaLimite,
        boolean vencida,
        LocalDateTime fechaResolucion,
        String observaciones,
        BigDecimal costo,
        TipoTramiteDto tramite,
        Ciudadano ciudadano,
        String informacionSolicitada,
        String motivoResolucion,
        List<DocumentoDto> adjuntos,
        DocumentoDto documentoOficial,
        List<PagoDto> pagos,
        List<HistorialDto> historial,
        List<String> accionesPermitidas) {

    public record Ciudadano(String nombre, String correo, String documento, String municipio, String estado) {
    }

    public record DocumentoDto(Integer id, String nombreArchivo, String requisito, String tipoContenido, long tamanoBytes,
                               LocalDateTime fechaCarga, String codigoVerificacion) {

        static DocumentoDto de(Documento d) {
            return new DocumentoDto(d.getIdDocumento(), d.getNombreArchivo(),
                    d.getRequisito() == null ? null : d.getRequisito().getNombre(), d.getTipoContenido(),
                    d.getTamanoBytes(), d.getFechaCarga(), d.getCodigoVerificacion());
        }
    }

    public record PagoDto(String referencia, BigDecimal valor, String estado, String banco, LocalDateTime fechaCreacion,
                          LocalDateTime fechaConfirmacion) {

        static PagoDto de(Pago p) {
            return new PagoDto(p.getReferenciaOrden(), p.getValor(), p.getEstado().name(), p.getBanco(),
                    p.getFechaCreacion(), p.getFechaConfirmacion());
        }
    }

    public record HistorialDto(String estadoAnterior, String estadoNuevo, String estadoEtiqueta, LocalDateTime fecha,
                               String motivo, String origen, String usuario) {

        static HistorialDto de(Historial h) {
            return new HistorialDto(h.getEstadoAnterior() == null ? null : h.getEstadoAnterior().name(),
                    h.getEstadoNuevo().name(), h.getEstadoNuevo().getEtiqueta(), h.getFechaCambio(), h.getMotivo(),
                    h.getOrigenCambio().name(), h.getNombreUsuario());
        }
    }

    public static SolicitudDetalle de(SolicitudTramite s, LocalDate hoy, List<String> acciones) {
        return new SolicitudDetalle(
                s.getIdSolicitud(),
                s.getNumeroRadicado(),
                s.getEstado().name(),
                s.getEstado().getEtiqueta(),
                s.getFechaRadicacion(),
                s.getFechaLimite(),
                s.estaVencida(hoy),
                s.getFechaResolucion(),
                s.getObservaciones(),
                s.getCosto(),
                TipoTramiteDto.de(s.getTipoTramite()),
                new Ciudadano(s.getNombreCiudadano(), s.getCorreoCiudadano(), s.getDocumentoCiudadano(),
                        s.getMunicipioNombre(), s.getMunicipioEstado()),
                s.informacionSolicitada().orElse(null),
                s.motivoResolucion().orElse(null),
                s.getDocumentos().stream().filter(d -> !d.esOficial()).map(DocumentoDto::de).toList(),
                s.documentoOficial().map(DocumentoDto::de).orElse(null),
                s.getPagos().stream().map(PagoDto::de).toList(),
                s.getHistorial().stream().map(HistorialDto::de).toList(),
                acciones);
    }
}
