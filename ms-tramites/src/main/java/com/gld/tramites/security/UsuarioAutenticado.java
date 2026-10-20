package com.gld.tramites.security;

/**
 * Usuario del token de acceso emitido por ms-identidad.
 * Se conserva el token para consultar el perfil en ms-identidad en nombre del usuario.
 */
public record UsuarioAutenticado(Integer idUsuario, String rol, Integer idMunicipio, String token) {

    public boolean esCiudadano() {
        return "CIUDADANO".equals(rol);
    }

    public boolean esFuncionario() {
        return "FUNCIONARIO".equals(rol);
    }
}
