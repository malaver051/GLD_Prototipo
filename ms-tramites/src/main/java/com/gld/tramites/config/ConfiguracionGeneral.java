package com.gld.tramites.config;

import com.gld.tramites.support.pdf.GeneradorDocumentoOficial;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Clock;

@Configuration
public class ConfiguracionGeneral {

    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public GeneradorDocumentoOficial generadorDocumentoOficial() {
        return new GeneradorDocumentoOficial();
    }

    /** Cliente HTTP hacia ms-identidad (integración síncrona entre microservicios). */
    @Bean
    public RestClient restClientIdentidad(RestClient.Builder builder, PropiedadesGld propiedades) {
        return builder.baseUrl(propiedades.identidad().url()).build();
    }
}
