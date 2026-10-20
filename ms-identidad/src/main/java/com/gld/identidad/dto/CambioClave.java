package com.gld.identidad.dto;

import jakarta.validation.constraints.NotBlank;

public record CambioClave(
        @NotBlank(message = "Escribe la nueva contraseña.") String nuevaContrasena,
        @NotBlank(message = "Confirma la nueva contraseña.") String confirmacion) {
}
