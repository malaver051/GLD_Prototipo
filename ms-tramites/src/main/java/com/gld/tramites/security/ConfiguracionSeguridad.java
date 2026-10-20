package com.gld.tramites.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gld.tramites.config.PropiedadesGld;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Reglas de acceso del microservicio. Las rutas son relativas a /api/v1/tramites.
 */
@Configuration
public class ConfiguracionSeguridad {

    @Bean
    public SecurityFilterChain cadenaSeguridad(HttpSecurity http, JwtService jwtService, ObjectMapper json)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(c -> {})
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Pública: cualquiera puede verificar un documento oficial con su código
                        .requestMatchers(HttpMethod.GET, "/verificacion/**").permitAll()
                        // Ciudadano
                        .requestMatchers("/catalogo/**", "/pagos/**").hasRole("CIUDADANO")
                        .requestMatchers(HttpMethod.POST, "/solicitudes").hasRole("CIUDADANO")
                        .requestMatchers("/solicitudes/*/correccion", "/solicitudes/*/pagos").hasRole("CIUDADANO")
                        // Funcionario
                        .requestMatchers("/solicitudes/*/solicitud-informacion", "/solicitudes/*/resolucion").hasRole("FUNCIONARIO")
                        .requestMatchers("/admin/**").hasRole("FUNCIONARIO")
                        // Consulta de solicitudes y archivos: ambos roles (el servicio valida la pertenencia)
                        .requestMatchers(HttpMethod.GET, "/solicitudes", "/solicitudes/**").hasAnyRole("CIUDADANO", "FUNCIONARIO")
                        .requestMatchers("/error").permitAll()
                        .anyRequest().denyAll())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> escribirError(res, json,
                                HttpStatus.UNAUTHORIZED, "Tu sesión no es válida o expiró. Vuelve a ingresar."))
                        .accessDeniedHandler((req, res, ex) -> escribirError(res, json,
                                HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción.")))
                .addFilterBefore(new FiltroJwt(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(PropiedadesGld propiedades) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of(propiedades.frontend().url()));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", cors);
        return fuente;
    }

    private static void escribirError(HttpServletResponse res, ObjectMapper json, HttpStatus estado, String detalle)
            throws IOException {
        res.setStatus(estado.value());
        res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        json.writeValue(res.getOutputStream(), ProblemDetail.forStatusAndDetail(estado, detalle));
    }
}
