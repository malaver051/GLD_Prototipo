package com.gld.comunidad.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CalificacionEntrada(
        @NotNull(message = "Elige una calificación de 0 a 10.")
        @Min(value = 0, message = "La calificación debe estar entre 0 y 10.")
        @Max(value = 10, message = "La calificación debe estar entre 0 y 10.") Integer calificacion,
        @Size(max = 500, message = "El comentario admite máximo 500 caracteres.") String comentario) {
}
