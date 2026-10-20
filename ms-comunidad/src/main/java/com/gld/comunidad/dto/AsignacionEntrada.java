package com.gld.comunidad.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AsignacionEntrada(
        @NotNull(message = "Elige la dependencia que atenderá la incidencia.") Integer idDependencia,
        @Size(max = 500, message = "El comentario admite máximo 500 caracteres.") String comentario) {
}
