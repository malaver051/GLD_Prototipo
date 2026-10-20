package com.gld.tramites.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Year;
import java.util.HexFormat;

/** Formatos de números de radicado, referencias de pago y códigos de verificación. */
public final class Codigos {

    /** Sin 0/O ni 1/I para que el código se pueda dictar sin confusiones. */
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Codigos() {
    }

    /** GLD-14039-2026-000123: clave INEGI del municipio, año y consecutivo. */
    public static String radicado(String codigoMunicipio, long consecutivo) {
        return String.format("GLD-%s-%d-%06d", codigoMunicipio, Year.now().getValue(), consecutivo);
    }

    /** PSE-2026-AB12CD34: referencia de la orden de pago. */
    public static String referenciaPago() {
        return "PSE-" + Year.now().getValue() + "-" + aleatorio(8);
    }

    /** GLD-7K3Q-92XA: código impreso en el documento oficial. */
    public static String codigoVerificacion() {
        return "GLD-" + aleatorio(4) + "-" + aleatorio(4);
    }

    public static String sha256(String texto) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String sha256(byte[] datos) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(datos));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String aleatorio(int largo) {
        StringBuilder sb = new StringBuilder(largo);
        for (int i = 0; i < largo; i++) {
            sb.append(ALFABETO.charAt(ALEATORIO.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }
}
