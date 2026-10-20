package com.gld.identidad.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Enlace de recuperación de contraseña (solo ciudadanos). Se guarda el hash, nunca el token. */
@Entity
@Table(name = "token_recuperacion")
public class TokenRecuperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Integer idToken;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id_usuario")
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "usado", nullable = false)
    private boolean usado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    protected TokenRecuperacion() {
    }

    public TokenRecuperacion(Usuario usuario, String tokenHash, int minutosVigencia) {
        this.usuario = usuario;
        this.tokenHash = tokenHash;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaExpiracion = fechaCreacion.plusMinutes(minutosVigencia);
    }

    public boolean estaVigente() {
        return !usado && LocalDateTime.now().isBefore(fechaExpiracion);
    }

    public void marcarUsado() {
        this.usado = true;
    }

    public Usuario getUsuario() { return usuario; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
}
