package com.gld.identidad.security;

import com.gld.identidad.config.PropiedadesGld;
import com.gld.identidad.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

/**
 * Emite y valida los JWT de la plataforma.
 * GRASP · Fabricación pura: concentra la lógica de tokens fuera del dominio.
 */
@Service
public class JwtService {

    private static final String EMISOR = "gld-ms-identidad";

    private final SecretKey clave;
    private final int minutosAcceso;
    private final int minutosTemporal;

    public JwtService(PropiedadesGld propiedades) {
        PropiedadesGld.Jwt jwt = propiedades.seguridad().jwt();
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwt.secreto()));
        this.minutosAcceso = jwt.minutosAcceso();
        this.minutosTemporal = jwt.minutosTemporal();
    }

    public String emitir(Usuario usuario, PropositoToken proposito) {
        int minutos = proposito == PropositoToken.ACCESO ? minutosAcceso : minutosTemporal;
        Instant ahora = Instant.now();
        return Jwts.builder()
                .issuer(EMISOR)
                .subject(String.valueOf(usuario.getIdUsuario()))
                .claim("rol", usuario.getRolPrincipal().name())
                .claim("municipio", usuario.getMunicipio() == null ? null : usuario.getMunicipio().getIdMunicipio())
                .claim("proposito", proposito.name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(minutos, ChronoUnit.MINUTES)))
                .signWith(clave)
                .compact();
    }

    /** Devuelve los datos del token si la firma y la vigencia son válidas. */
    public Optional<UsuarioAutenticado> validar(String token) {
        try {
            Claims c = Jwts.parser()
                    .verifyWith(clave)
                    .requireIssuer(EMISOR)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Number municipio = c.get("municipio", Number.class);
            return Optional.of(new UsuarioAutenticado(
                    Integer.valueOf(c.getSubject()),
                    c.get("rol", String.class),
                    municipio == null ? null : municipio.intValue(),
                    PropositoToken.valueOf(c.get("proposito", String.class))));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
