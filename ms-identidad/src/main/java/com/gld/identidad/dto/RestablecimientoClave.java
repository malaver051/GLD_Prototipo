package com.gld.identidad.dto;

import jakarta.validation.constraints.NotBlank;

public record RestablecimientoClave(
        @NotBlank(message = "El enlace no es válido.") String token,
        @NotBlank(message = "Escribe la nueva contraseña.") String nuevaContrasena,
        @NotBlank(message = "Confirma la nueva contraseña.") String confirmacion) {
}
