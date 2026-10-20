package com.gld.identidad.support;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.OptionalLong;

/**
 * Códigos de un solo uso basados en tiempo (TOTP, RFC 6238): HMAC-SHA1, 6 dígitos, 30 segundos.
 * Es el estándar que leen Microsoft Authenticator y Google Authenticator.
 *
 * GRASP · Fabricación pura / Variaciones protegidas: el algoritmo del segundo factor queda
 * aislado aquí; los servicios solo preguntan si un código es válido.
 */
public class GeneradorTotp {

    public static final int DIGITOS = 6;
    public static final int PERIODO_SEGUNDOS = 30;
    /** Pasos de tolerancia antes y después del actual, por desfase del reloj del celular. */
    private static final int VENTANA = 1;

    private final SecureRandom aleatorio = new SecureRandom();
    private final Clock reloj;

    public GeneradorTotp(Clock reloj) {
        this.reloj = reloj;
    }

    /** Secreto de 160 bits en Base32. */
    public String generarSecreto() {
        byte[] bytes = new byte[20];
        aleatorio.nextBytes(bytes);
        return Base32.codificar(bytes);
    }

    public long pasoActual() {
        return reloj.instant().getEpochSecond() / PERIODO_SEGUNDOS;
    }

    /**
     * Verifica el código contra el paso actual y los vecinos.
     * @return el paso que coincidió (para impedir reutilizarlo), o vacío si no es válido.
     */
    public OptionalLong verificar(String secretoBase32, String codigo) {
        if (secretoBase32 == null || codigo == null || !codigo.matches("\\d{" + DIGITOS + "}")) {
            return OptionalLong.empty();
        }
        byte[] clave = Base32.decodificar(secretoBase32);
        long actual = pasoActual();
        for (long paso = actual - VENTANA; paso <= actual + VENTANA; paso++) {
            if (constanteIgual(codigoPara(clave, paso), codigo)) {
                return OptionalLong.of(paso);
            }
        }
        return OptionalLong.empty();
    }

    public String codigoPara(byte[] clave, long paso) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(clave, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(paso).array());
            int desplazamiento = hash[hash.length - 1] & 0x0F;
            int binario = ((hash[desplazamiento] & 0x7F) << 24)
                    | ((hash[desplazamiento + 1] & 0xFF) << 16)
                    | ((hash[desplazamiento + 2] & 0xFF) << 8)
                    | (hash[desplazamiento + 3] & 0xFF);
            int otp = binario % (int) Math.pow(10, DIGITOS);
            return String.format("%0" + DIGITOS + "d", otp);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No fue posible calcular el código TOTP", e);
        }
    }

    /** URI que se convierte en código QR para escanear con Microsoft Authenticator. */
    public String uriOtpauth(String emisor, String cuenta, String secreto) {
        String etiqueta = codificarUrl(emisor) + ":" + codificarUrl(cuenta);
        return "otpauth://totp/" + etiqueta
                + "?secret=" + secreto
                + "&issuer=" + codificarUrl(emisor)
                + "&algorithm=SHA1&digits=" + DIGITOS + "&period=" + PERIODO_SEGUNDOS;
    }

    private static String codificarUrl(String valor) {
        return java.net.URLEncoder.encode(valor, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static boolean constanteIgual(String a, String b) {
        return java.security.MessageDigest.isEqual(a.getBytes(), b.getBytes());
    }
}
