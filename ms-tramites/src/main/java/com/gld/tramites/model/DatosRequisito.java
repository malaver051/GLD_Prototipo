package com.gld.tramites.model;

/** Datos de un requisito al editar el catálogo; id null significa requisito nuevo. */
public record DatosRequisito(Integer id, String nombre, String descripcion, boolean obligatorio) {
}
