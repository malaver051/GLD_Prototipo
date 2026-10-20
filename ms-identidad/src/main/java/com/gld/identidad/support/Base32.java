package com.gld.identidad.support;

import java.io.ByteArrayOutputStream;

/** Codificación Base32 (RFC 4648, sin relleno), formato que usan las apps autenticadoras. */
public final class Base32 {

    private static final String ALFABETO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private Base32() {
    }

    public static String codificar(byte[] datos) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0;
        int bits = 0;
        for (byte b : datos) {
            buffer = (buffer << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                sb.append(ALFABETO.charAt((buffer >> (bits - 5)) & 0x1F));
                bits -= 5;
            }
        }
        if (bits > 0) {
            sb.append(ALFABETO.charAt((buffer << (5 - bits)) & 0x1F));
        }
        return sb.toString();
    }

    public static byte[] decodificar(String texto) {
        String limpio = texto.replace("=", "").replace(" ", "").toUpperCase();
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        int buffer = 0;
        int bits = 0;
        for (char c : limpio.toCharArray()) {
            int valor = ALFABETO.indexOf(c);
            if (valor < 0) {
                throw new IllegalArgumentException("Carácter Base32 inválido: " + c);
            }
            buffer = (buffer << 5) | valor;
            bits += 5;
            if (bits >= 8) {
                salida.write((buffer >> (bits - 8)) & 0xFF);
                bits -= 8;
            }
        }
        return salida.toByteArray();
    }
}
