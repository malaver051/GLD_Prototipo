package com.gld.tramites.security;

import com.gld.tramites.config.PropiedadesGld;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Optional;

/**
 * Valida los JWT emitidos por ms-identidad (misma clave compartida).
 * Solo acepta tokens de ACCESO: los tokens temporales del ingreso no abren este servicio.
 */
@Service
public class JwtService {

    private static final String EMISOR = "gld-ms-identidad";

    private final SecretKey clave;

    public JwtService(PropiedadesGld propiedades) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(propiedades.seguridad().jwt().secreto()));
    }

    public Optional<UsuarioAutenticado> validar(String token) {
        try {
            Claims c = Jwts.parser()
                    .verifyWith(clave)
                    .requireIssuer(EMISOR)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (!"ACCESO".equals(c.get("proposito", String.class))) {
                return Optional.empty();
            }
            Number municipio = c.get("municipio", Number.class);
            return Optional.of(new UsuarioAutenticado(
                    Integer.valueOf(c.getSubject()),
                    c.get("rol", String.class),
                    municipio == null ? null : municipio.intValue(),
                    token));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
