package com.gld.identidad.config;

import com.gld.identidad.support.GeneradorTotp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

@Configuration
public class ConfiguracionGeneral {

    @Bean
    public Clock reloj() {
        return Clock.systemUTC();
    }

    @Bean
    public GeneradorTotp generadorTotp(Clock reloj) {
        return new GeneradorTotp(reloj);
    }

    /** BCrypt con costo 12 para las contraseñas. */
    @Bean
    public PasswordEncoder codificadorContrasenas() {
        return new BCryptPasswordEncoder(12);
    }
}
