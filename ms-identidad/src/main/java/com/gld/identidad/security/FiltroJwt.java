package com.gld.identidad.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee el encabezado "Authorization: Bearer ..." y autentica la petición.
 * - Token de ACCESO   → autoridad ROLE_<rol> (abre la aplicación según el rol).
 * - Token temporal    → autoridad PROPOSITO_<propósito> (solo permite completar ese paso).
 * Se instancia en ConfiguracionSeguridad (no es @Component) para que no se registre dos veces.
 */
public class FiltroJwt extends OncePerRequestFilter {

    private final JwtService jwtService;

    public FiltroJwt(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String encabezado = request.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            jwtService.validar(encabezado.substring(7)).ifPresent(usuario -> {
                GrantedAuthority autoridad = usuario.proposito() == PropositoToken.ACCESO
                        ? new SimpleGrantedAuthority("ROLE_" + usuario.rol())
                        : new SimpleGrantedAuthority("PROPOSITO_" + usuario.proposito().name());
                var autenticacion = new UsernamePasswordAuthenticationToken(usuario, null, List.of(autoridad));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            });
        }
        chain.doFilter(request, response);
    }
}
