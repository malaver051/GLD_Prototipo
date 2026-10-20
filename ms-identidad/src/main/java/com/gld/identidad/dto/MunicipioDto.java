package com.gld.identidad.dto;

import com.gld.identidad.model.Municipio;

/** "estado" es la entidad federativa (columna departamento). */
public record MunicipioDto(Integer id, String codigo, String nombre, String estado) {

    public static MunicipioDto de(Municipio m) {
        return new MunicipioDto(m.getIdMunicipio(), m.getCodigoMunicipio(), m.getNombre(), m.getDepartamento());
    }
}
