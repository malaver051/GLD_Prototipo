package com.gld.tramites.support;

import com.gld.tramites.support.pdf.DatosDocumentoOficial;
import com.gld.tramites.support.pdf.GeneradorDocumentoOficial;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneradorDocumentoOficialTest {

    @Test
    void generaUnPdfValido() {
        byte[] pdf = new GeneradorDocumentoOficial().generar(new DatosDocumentoOficial(
                "Guadalajara", "Jalisco", "Secretaría del Ayuntamiento", "Constancia de residencia",
                "GLD-14039-2026-000001", LocalDateTime.now(), LocalDateTime.now(), "Ana María Gómez Ruiz",
                "CURP GORA900315MJCMZN05", "ana@correo.com", null, "Trámite gratuito", null,
                "Laura Méndez Ortega", "GLD-7K3Q-92XA", "http://localhost:5173/verificar?codigo=GLD-7K3Q-92XA",
                "0".repeat(64)));
        String texto = new String(pdf, StandardCharsets.ISO_8859_1);
        assertTrue(texto.startsWith("%PDF-1.4"));
        assertTrue(texto.contains("GLD-7K3Q-92XA"));
        assertTrue(texto.trim().endsWith("%%EOF"));
    }
}
