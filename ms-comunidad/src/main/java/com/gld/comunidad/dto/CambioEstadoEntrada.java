package com.gld.comunidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** PUT /incidencias/{id}/estado del contrato: EN_ATENCION o RESUELTA (con la solución en el comentario). */
public record CambioEstadoEntrada(
        @NotBlank(message = "Indica el nuevo estado.") String estado,
        @Size(max = 1000, message = "El comentario admite máximo 1000 caracteres.") String comentario) {
}
