package com.gld.tramites.dto;

import com.gld.tramites.model.RequisitoTramite;

public record RequisitoDto(Integer id, String nombre, String descripcion, boolean obligatorio) {

    public static RequisitoDto de(RequisitoTramite r) {
        return new RequisitoDto(r.getIdRequisito(), r.getNombre(), r.getDescripcion(), r.isObligatorio());
    }
}
