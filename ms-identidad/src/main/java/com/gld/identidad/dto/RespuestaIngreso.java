package com.gld.identidad.dto;

/**
 * Respuesta de cada paso de autenticación.
 * Si el paso no es ACCESO_CONCEDIDO, el token es temporal y solo sirve para completar ese paso.
 */
public record RespuestaIngreso(PasoIngreso paso, String token, UsuarioResumen usuario) {
}
