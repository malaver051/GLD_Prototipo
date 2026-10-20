package com.gld.tramites.model;

/**
 * Copia de los datos del ciudadano tomada de ms-identidad al radicar.
 * Cada microservicio guarda lo que necesita para no depender del otro al consultar.
 */
public record DatosCiudadano(Integer idUsuario, String nombre, String correo, String documento,
                             Integer idMunicipio, String codigoMunicipio, String municipioNombre,
                             String municipioEstado) {
}
