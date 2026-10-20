package com.gld.identidad.support;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/** Vectores de prueba del RFC 6238 (HMAC-SHA1, 6 dígitos). */
class GeneradorTotpTest {

    private static final byte[] CLAVE_RFC = "12345678901234567890".getBytes();

    private GeneradorTotp enInstante(long segundos) {
        return new GeneradorTotp(Clock.fixed(Instant.ofEpochSecond(segundos), ZoneOffset.UTC));
    }

    @Test
    void calculaLosCodigosDelRfc() {
        assertEquals("287082", enInstante(59).codigoPara(CLAVE_RFC, 1));
        GeneradorTotp totp = enInstante(1111111109L);
        assertEquals("081804", totp.codigoPara(CLAVE_RFC, totp.pasoActual()));
    }

    @Test
    void aceptaElCodigoVigenteYRechazaUnoErrado() {
        GeneradorTotp totp = enInstante(1111111109L);
        String secreto = Base32.codificar(CLAVE_RFC);
        assertTrue(totp.verificar(secreto, "081804").isPresent());
        assertTrue(totp.verificar(secreto, "000000").isEmpty());
        assertTrue(totp.verificar(secreto, "12ab56").isEmpty());
    }

    @Test
    void base32IdaYVuelta() {
        assertEquals("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", Base32.codificar(CLAVE_RFC));
        assertArrayEquals(CLAVE_RFC, Base32.decodificar("GEZD GNBV GY3T QOJQ GEZD GNBV GY3T QOJQ"));
    }
}
