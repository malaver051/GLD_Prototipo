package com.gld.tramites;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Microservicio de Trámites en línea de la plataforma GLD
 * (catálogo, solicitudes, pagos y documentos oficiales).
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class TramitesApplication {

    public static void main(String[] args) {
        SpringApplication.run(TramitesApplication.class, args);
    }
}
