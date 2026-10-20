package com.gld.identidad.dto;

/**
 * El mensaje es el mismo exista o no la cuenta, para no revelar qué correos están registrados.
 * canal: ENLACE_CORREO (ciudadano) o ADMINISTRADOR (funcionario @gobierno.com).
 */
public record RespuestaRecuperacion(String canal, String mensaje) {
}
