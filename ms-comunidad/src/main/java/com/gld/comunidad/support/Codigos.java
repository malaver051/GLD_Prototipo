package com.gld.comunidad.support;

import java.time.Year;

/** Formato del número de seguimiento de las incidencias. */
public final class Codigos {

    private Codigos() {
    }

    /** INC-14039-2026-000123: clave INEGI del municipio, año y consecutivo. */
    public static String seguimiento(String codigoMunicipio, long consecutivo) {
        return String.format("INC-%s-%d-%06d", codigoMunicipio, Year.now().getValue(), consecutivo);
    }
}
