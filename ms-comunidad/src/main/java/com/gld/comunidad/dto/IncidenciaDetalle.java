package com.gld.comunidad.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Detalle de una incidencia con su historial y las acciones que el usuario puede hacer ahora. */
public record IncidenciaDetalle(Integer id, String numeroSeguimiento, String categoria, String categoriaEtiqueta,
                                String descripcion, String referencia, double latitud, double longitud,
                                String estado, String estadoEtiqueta, LocalDateTime fechaReporte,
                                LocalDateTime fechaActualizacion, LocalDateTime fechaResolucion,
                                LocalDateTime fechaCierre, String municipio, String estadoMunicipio,
                                Ciudadano ciudadano, Foto foto, DependenciaDto dependencia, String solucion,
                                Integer calificacion, String comentarioCalificacion, int reaperturas,
                                List<HistorialDto> historial, List<String> accionesPermitidas) {

    public record Ciudadano(String nombre, String correo) {
    }

    public record Foto(String nombreArchivo, String tipoContenido, long tamanoBytes) {
    }

    public record HistorialDto(String estadoAnterior, String estadoNuevo, String estadoEtiqueta, LocalDateTime fecha,
                               String motivo, String origen, String usuario) {
    }
}
