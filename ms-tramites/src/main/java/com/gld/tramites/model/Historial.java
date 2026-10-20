package com.gld.tramites.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** HISTORIAL del modelo relacional: cada cambio de estado de una solicitud. */
@Entity
@Table(name = "historial")
public class Historial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Integer idHistorial;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior")
    private EstadoSolicitud estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false)
    private EstadoSolicitud estadoNuevo;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;

    @Column(name = "motivo", nullable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_cambio", nullable = false)
    private OrigenCambio origenCambio;

    @Column(name = "usuario_id_usuario")
    private Integer idUsuario;

    @Column(name = "nombre_usuario")
    private String nombreUsuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_tramite_id_solicitud")
    private SolicitudTramite solicitud;

    protected Historial() {
    }

    Historial(SolicitudTramite solicitud, EstadoSolicitud anterior, EstadoSolicitud nuevo, String motivo,
              OrigenCambio origen, Integer idUsuario, String nombreUsuario) {
        this.solicitud = solicitud;
        this.estadoAnterior = anterior;
        this.estadoNuevo = nuevo;
        this.motivo = motivo;
        this.origenCambio = origen;
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.fechaCambio = LocalDateTime.now();
    }

    public Integer getIdHistorial() { return idHistorial; }
    public EstadoSolicitud getEstadoAnterior() { return estadoAnterior; }
    public EstadoSolicitud getEstadoNuevo() { return estadoNuevo; }
    public LocalDateTime getFechaCambio() { return fechaCambio; }
    public String getMotivo() { return motivo; }
    public OrigenCambio getOrigenCambio() { return origenCambio; }
    public Integer getIdUsuario() { return idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
}
