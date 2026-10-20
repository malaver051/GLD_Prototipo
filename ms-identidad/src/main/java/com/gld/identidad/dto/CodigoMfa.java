package com.gld.identidad.dto;

import jakarta.validation.constraints.Pattern;

public record CodigoMfa(
        @Pattern(regexp = "^\\d{6}$", message = "El código tiene 6 dígitos.") String codigo) {
}
