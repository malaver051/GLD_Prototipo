package com.gld.tramites.dto;

import com.gld.tramites.model.Pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Datos que muestra la pasarela simulada. */
public record OrdenPago(String referencia, BigDecimal valor, String estado, String tramite, String numeroRadicado,
                        Integer idSolicitud, String municipio, LocalDateTime fechaCreacion) {

    public static OrdenPago de(Pago p) {
        var s = p.getSolicitud();
        return new OrdenPago(p.getReferenciaOrden(), p.getValor(), p.getEstado().name(), s.getTipoTramite().getNombre(),
                s.getNumeroRadicado(), s.getIdSolicitud(), s.getMunicipioNombre(), p.getFechaCreacion());
    }
}
