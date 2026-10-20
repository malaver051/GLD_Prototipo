package com.gld.tramites.dto;

import java.time.LocalDateTime;

/** Resultado público de verificar un documento oficial (sin datos personales completos). */
public record Verificacion(boolean valido, String tramite, String numeroRadicado, String municipio, String estado,
                           String titular, LocalDateTime fechaEmision, String selloSha256) {

    public static Verificacion invalido() {
        return new Verificacion(false, null, null, null, null, null, null, null);
    }
}
