package com.gld.identidad.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PoliticaContrasenaTest {

    private final PoliticaContrasena politica = new PoliticaContrasena();

    @Test
    void exigeLongitudMayusculaNumeroYSimbolo() {
        assertEquals(4, politica.incumplimientos("abc").size());
        assertFalse(politica.esValida("Abcdefg1"));
        assertTrue(politica.esValida("Abcdefg1!"));
        assertTrue(politica.esValida("Ñandú#2026"));
    }

    @Test
    void laClaveTemporalCumpleLaPolitica() {
        GeneradorClaveTemporal generador = new GeneradorClaveTemporal();
        for (int i = 0; i < 500; i++) {
            assertTrue(politica.esValida(generador.generar()));
        }
    }
}
