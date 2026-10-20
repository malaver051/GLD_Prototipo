package com.gld.identidad.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Propiedades "gld.*" de application.properties. */
@ConfigurationProperties(prefix = "gld")
public record PropiedadesGld(Seguridad seguridad, Frontend frontend, Recuperacion recuperacion, Mfa mfa) {

    public record Seguridad(Jwt jwt) {
    }

    public record Jwt(String secreto, int minutosAcceso, int minutosTemporal) {
    }

    public record Frontend(String url) {
    }

    public record Recuperacion(int minutosVigencia) {
    }

    public record Mfa(String emisor) {
    }
}
