package com.gld.comunidad.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Propiedades "gld.*" de application.properties. */
@ConfigurationProperties(prefix = "gld")
public record PropiedadesGld(Seguridad seguridad, Identidad identidad, Frontend frontend,
                             Almacenamiento almacenamiento, Incidencias incidencias) {

    public record Seguridad(Jwt jwt) {
    }

    public record Jwt(String secreto) {
    }

    public record Identidad(String url) {
    }

    public record Frontend(String url) {
    }

    public record Almacenamiento(String ruta) {
    }

    public record Incidencias(double radioKm) {
    }
}
