package com.gld.tramites.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Decisión del funcionario. Al rechazar, el motivo es obligatorio. */
public record Resolucion(
        @NotNull(message = "Indica si apruebas o rechazas la solicitud.") Boolean aprobada,
        @Size(max = 1000, message = "El texto puede tener hasta 1000 caracteres.") String motivo) {
}
