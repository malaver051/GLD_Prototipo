package com.gld.identidad.dto;

import com.gld.identidad.model.Usuario;

public record UsuarioResumen(Integer id, String nombreCompleto, String correo, String rol, String municipio) {

    public static UsuarioResumen de(Usuario u) {
        return new UsuarioResumen(
                u.getIdUsuario(),
                u.getNombreCompleto(),
                u.getCorreo(),
                u.getRolPrincipal().name(),
                u.getMunicipio() == null ? null : u.getMunicipio().getNombre());
    }
}
