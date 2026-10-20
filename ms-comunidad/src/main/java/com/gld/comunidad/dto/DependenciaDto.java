package com.gld.comunidad.dto;

import com.gld.comunidad.model.Dependencia;

public record DependenciaDto(Integer id, String nombre, String descripcion, String categoriaSugerida) {

    public static DependenciaDto de(Dependencia d) {
        return new DependenciaDto(d.getIdDependencia(), d.getNombre(), d.getDescripcion(),
                d.getCategoriaSugerida() == null ? null : d.getCategoriaSugerida().name());
    }
}
