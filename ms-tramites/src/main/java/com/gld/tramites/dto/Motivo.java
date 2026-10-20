package com.gld.tramites.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Mensaje del funcionario al pedir información adicional. */
public record Motivo(
        @NotBlank(message = "Explica qué información o documento necesitas.")
        @Size(max = 1000, message = "El mensaje puede tener hasta 1000 caracteres.") String motivo) {
}
