package com.gld.comunidad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Microservicio de Comunidad digital de la plataforma GLD.
 * En el prototipo implementa la épica GLD-E2: reporte y atención de incidencias.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class ComunidadApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComunidadApplication.class, args);
    }
}
