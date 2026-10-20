package com.gld.identidad.dto;

import jakarta.validation.constraints.NotNull;

public record CambioEstadoUsuario(@NotNull(message = "Indica si la cuenta queda activa.") Boolean activo) {
}
