package com.gld.identidad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Microservicio de Identidad y Municipios de la plataforma GLD.
 * Se excluye el usuario en memoria de Spring Security: la autenticación es propia (JWT).
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class IdentidadApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdentidadApplication.class, args);
    }
}
