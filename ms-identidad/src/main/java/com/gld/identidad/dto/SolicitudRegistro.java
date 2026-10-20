package com.gld.identidad.dto;

import com.gld.identidad.model.TipoDocumento;
import jakarta.validation.constraints.*;

/** Registro público de ciudadanos. */
public record SolicitudRegistro(
        @NotBlank(message = "Escribe tu nombre completo.")
        @Size(max = 150, message = "El nombre puede tener hasta 150 caracteres.")
        String nombreCompleto,

        @NotNull(message = "Elige un tipo de documento.")
        TipoDocumento tipoDocumento,

        @NotBlank(message = "Escribe el número de documento.")
        @Size(max = 30, message = "El número de documento puede tener hasta 30 caracteres.")
        String numeroDocumento,

        @NotBlank(message = "Escribe tu correo.")
        @Email(message = "Escribe un correo válido, por ejemplo nombre@correo.com.")
        @Size(max = 150)
        String correo,

        @NotBlank(message = "Escribe tu teléfono.")
        @Pattern(regexp = "^\\d{10}$", message = "El teléfono debe tener 10 dígitos.")
        String telefono,

        @NotNull(message = "Elige tu municipio.")
        Integer idMunicipio,

        @NotBlank(message = "Escribe una contraseña.")
        String contrasena,

        @NotBlank(message = "Confirma la contraseña.")
        String confirmacion,

        @AssertTrue(message = "Debes aceptar el aviso de privacidad para crear la cuenta.")
        boolean aceptaAvisoPrivacidad) {
}
