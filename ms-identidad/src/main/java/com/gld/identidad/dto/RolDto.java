package com.gld.identidad.dto;

import com.gld.identidad.model.Rol;

public record RolDto(Integer id, String nombre, String descripcion) {

    public static RolDto de(Rol r) {
        return new RolDto(r.getIdRol(), r.getNombre().name(), r.getDescripcion());
    }
}
