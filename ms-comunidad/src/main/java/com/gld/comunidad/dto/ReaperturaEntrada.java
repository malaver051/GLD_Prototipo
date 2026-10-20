package com.gld.comunidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReaperturaEntrada(
        @NotBlank(message = "Cuéntanos por qué el problema no quedó resuelto.")
        @Size(max = 500, message = "El motivo admite máximo 500 caracteres.") String motivo) {
}
