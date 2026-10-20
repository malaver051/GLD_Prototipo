package com.gld.comunidad.model;

/** Datos del ciudadano y su municipio obtenidos de ms-identidad al reportar. */
public record DatosCiudadano(Integer idUsuario, String nombre, String correo, Integer idMunicipio,
                             String codigoMunicipio, String municipioNombre, String municipioEstado) {
}
