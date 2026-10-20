package com.gld.tramites.dto;

import com.gld.tramites.model.SolicitudTramite;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Fila de las listas «Mis solicitudes» y bandeja del funcionario. */
public record SolicitudResumen(Integer id, String numeroRadicado, String tramite, String estado, String estadoEtiqueta,
                               LocalDateTime fechaRadicacion, LocalDate fechaLimite, boolean vencida,
                               BigDecimal costo, String nombreCiudadano, String correoCiudadano) {

    public static SolicitudResumen de(SolicitudTramite s, LocalDate hoy) {
        return new SolicitudResumen(s.getIdSolicitud(), s.getNumeroRadicado(), s.getTipoTramite().getNombre(),
                s.getEstado().name(), s.getEstado().getEtiqueta(), s.getFechaRadicacion(), s.getFechaLimite(),
                s.estaVencida(hoy), s.getCosto(), s.getNombreCiudadano(), s.getCorreoCiudadano());
    }
}
