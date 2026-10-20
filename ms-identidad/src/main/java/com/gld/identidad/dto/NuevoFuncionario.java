package com.gld.identidad.dto;

import com.gld.identidad.model.TipoDocumento;
import jakarta.validation.constraints.*;

/** Alta de un funcionario por el administrador. La contraseña la genera el sistema. */
public record NuevoFuncionario(
        @NotBlank(message = "Escribe el nombre completo.")
        @Size(max = 150)
        String nombreCompleto,

        @NotNull(message = "Elige un tipo de documento.")
        TipoDocumento tipoDocumento,

        @NotBlank(message = "Escribe el número de documento.")
        @Size(max = 30)
        String numeroDocumento,

        @NotBlank(message = "Escribe el correo institucional.")
        @Email(message = "Escribe un correo válido.")
        @Pattern(regexp = "(?i)^[^@\\s]+@gobierno\\.com$", message = "El correo debe terminar en @gobierno.com.")
        String correo,

        @NotBlank(message = "Escribe el teléfono.")
        @Pattern(regexp = "^\\d{10}$", message = "El teléfono debe tener 10 dígitos.")
        String telefono,

        @NotNull(message = "Elige el municipio.")
        Integer idMunicipio,

        @NotNull(message = "Elige el rol.")
        Integer idRol) {
}
