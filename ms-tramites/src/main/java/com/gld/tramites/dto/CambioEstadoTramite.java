package com.gld.tramites.dto;

import jakarta.validation.constraints.NotNull;

public record CambioEstadoTramite(@NotNull(message = "Indica si el trámite queda habilitado.") Boolean habilitado) {
}
