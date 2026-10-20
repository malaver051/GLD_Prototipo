package com.gld.identidad.security;

/** Datos del usuario que viajan en el JWT y quedan disponibles en los controladores. */
public record UsuarioAutenticado(Integer idUsuario, String rol, Integer idMunicipio, PropositoToken proposito) {
}
