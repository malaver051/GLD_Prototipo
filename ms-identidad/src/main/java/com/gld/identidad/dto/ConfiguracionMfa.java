package com.gld.identidad.dto;

/**
 * Datos para vincular Microsoft Authenticator.
 * uriOtpauth se muestra como QR; claveManual sirve si la cámara no lee el QR.
 */
public record ConfiguracionMfa(String uriOtpauth, String claveManual, String cuenta, String emisor) {
}
