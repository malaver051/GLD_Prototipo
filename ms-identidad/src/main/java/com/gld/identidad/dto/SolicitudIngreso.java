package com.gld.identidad.dto;

import jakarta.validation.constraints.NotBlank;

/** Datos de inicio de sesión: correo (o "admin") y contraseña. */
public record SolicitudIngreso(
        @NotBlank(message = "Escribe tu correo o usuario.") String identificador,
        @NotBlank(message = "Escribe tu contraseña.") String contrasena) {
}
