package com.gld.identidad.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Solicitud de un funcionario para que el administrador le restablezca la contraseña. */
@Entity
@Table(name = "solicitud_restablecimiento")
public class SolicitudRestablecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Integer idSolicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id_usuario")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoSolicitud estado;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_atencion")
    private LocalDateTime fechaAtencion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendida_por")
    private Usuario atendidaPor;

    protected SolicitudRestablecimiento() {
    }

    public SolicitudRestablecimiento(Usuario usuario) {
        this.usuario = usuario;
        this.estado = EstadoSolicitud.PENDIENTE;
        this.fechaSolicitud = LocalDateTime.now();
    }

    public boolean estaPendiente() {
        return estado == EstadoSolicitud.PENDIENTE;
    }

    public void atender(Usuario administrador) {
        this.estado = EstadoSolicitud.ATENDIDA;
        this.fechaAtencion = LocalDateTime.now();
        this.atendidaPor = administrador;
    }

    public Integer getIdSolicitud() { return idSolicitud; }
    public Usuario getUsuario() { return usuario; }
    public EstadoSolicitud getEstado() { return estado; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public LocalDateTime getFechaAtencion() { return fechaAtencion; }
}
