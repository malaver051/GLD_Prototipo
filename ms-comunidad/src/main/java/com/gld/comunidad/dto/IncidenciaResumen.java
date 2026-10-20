package com.gld.comunidad.dto;

import com.gld.comunidad.model.Incidencia;

import java.time.LocalDateTime;

/** Fila de las listas y marcador del mapa. */
public record IncidenciaResumen(Integer id, String numeroSeguimiento, String categoria, String categoriaEtiqueta,
                                String descripcion, String estado, String estadoEtiqueta, double latitud,
                                double longitud, String referencia, LocalDateTime fechaReporte,
                                LocalDateTime fechaActualizacion, String dependencia, String nombreCiudadano,
                                Integer calificacion, boolean tieneFoto) {

    public static IncidenciaResumen de(Incidencia i) {
        return new IncidenciaResumen(i.getIdIncidencia(), i.getNumeroSeguimiento(), i.getCategoria().name(),
                i.getCategoria().etiqueta(), i.getDescripcion(), i.getEstado().name(), i.getEstado().etiqueta(),
                i.ubicacion().latitud(), i.ubicacion().longitud(), i.getReferencia(), i.getFechaReporte(),
                i.getFechaActualizacion(), i.getDependencia() == null ? null : i.getDependencia().getNombre(),
                i.getNombreCiudadano(), i.getCalificacion(), i.tieneFoto());
    }
}
