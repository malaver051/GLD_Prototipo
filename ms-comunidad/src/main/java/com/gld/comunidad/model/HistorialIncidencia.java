package com.gld.comunidad.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Cada cambio de estado o de asignación de una incidencia. */
@Entity
@Table(name = "historial_incidencia")
public class HistorialIncidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Integer idHistorial;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior")
    private EstadoIncidencia estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false)
    private EstadoIncidencia estadoNuevo;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;

    @Column(name = "comentario", nullable = false)
    private String comentario;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_cambio", nullable = false)
    private OrigenCambio origenCambio;

    @Column(name = "usuario_id_usuario")
    private Integer idUsuario;

    @Column(name = "nombre_usuario")
    private String nombreUsuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incidencia_id_incidencia")
    private Incidencia incidencia;

    protected HistorialIncidencia() {
    }

    HistorialIncidencia(Incidencia incidencia, EstadoIncidencia anterior, EstadoIncidencia nuevo, String comentario,
                        OrigenCambio origen, Integer idUsuario, String nombreUsuario, LocalDateTime fecha) {
        this.incidencia = incidencia;
        this.estadoAnterior = anterior;
        this.estadoNuevo = nuevo;
        this.comentario = comentario;
        this.origenCambio = origen;
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.fechaCambio = fecha;
    }

    public Integer getIdHistorial() { return idHistorial; }
    public EstadoIncidencia getEstadoAnterior() { return estadoAnterior; }
    public EstadoIncidencia getEstadoNuevo() { return estadoNuevo; }
    public LocalDateTime getFechaCambio() { return fechaCambio; }
    public String getComentario() { return comentario; }
    public OrigenCambio getOrigenCambio() { return origenCambio; }
    public String getNombreUsuario() { return nombreUsuario; }
}
