package com.gld.identidad.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SolicitudRecuperacion(
        @NotBlank(message = "Escribe tu correo.")
        @Email(message = "Escribe un correo válido.")
        String correo) {
}
