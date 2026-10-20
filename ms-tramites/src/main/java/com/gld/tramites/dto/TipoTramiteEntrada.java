package com.gld.tramites.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/** Alta o edición de un tipo de trámite por el funcionario. */
public record TipoTramiteEntrada(
        @NotBlank(message = "Escribe el nombre del trámite.") @Size(max = 150) String nombre,
        @NotBlank(message = "Escribe la descripción.") @Size(max = 600, message = "La descripción puede tener hasta 600 caracteres.") String descripcion,
        @NotBlank(message = "Escribe la dependencia que lo atiende.") @Size(max = 120) String dependencia,
        @NotNull(message = "Escribe el costo (0 si es gratuito).")
        @DecimalMin(value = "0.00", message = "El costo no puede ser negativo.")
        @Digits(integer = 10, fraction = 2, message = "El costo admite hasta 2 decimales.") BigDecimal costo,
        @NotNull(message = "Escribe el plazo en días hábiles.")
        @Min(value = 1, message = "El plazo debe ser de al menos 1 día hábil.")
        @Max(value = 90, message = "El plazo puede ser de hasta 90 días hábiles.") Integer plazoDias,
        @NotEmpty(message = "Agrega al menos un requisito.") List<@Valid RequisitoEntrada> requisitos) {

    public record RequisitoEntrada(
            Integer id,
            @NotBlank(message = "Cada requisito necesita un nombre.") @Size(max = 150) String nombre,
            @Size(max = 400) String descripcion,
            boolean obligatorio) {
    }
}
