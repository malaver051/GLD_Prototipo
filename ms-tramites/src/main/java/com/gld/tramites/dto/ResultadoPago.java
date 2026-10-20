package com.gld.tramites.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Respuesta de la pasarela simulada. En producción llegaría como webhook firmado del proveedor de pagos.
 */
public record ResultadoPago(
        @NotNull(message = "Indica el resultado del pago.") Boolean aprobado,
        @NotBlank(message = "Elige el banco.") String banco) {
}
