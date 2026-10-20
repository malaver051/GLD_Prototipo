package com.gld.tramites.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** PAGO del modelo relacional: orden de pago de una solicitud (pasarela simulada). */
@Entity
@Table(name = "pago")
public class Pago {

    public static final String MEDIO_PSE = "PSE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer idPago;

    @Column(name = "referencia_orden", nullable = false, unique = true)
    private String referenciaOrden;

    @Column(name = "valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @Column(name = "medio_pago", nullable = false)
    private String medioPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPago estado;

    @Column(name = "banco")
    private String banco;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_tramite_id_solicitud")
    private SolicitudTramite solicitud;

    protected Pago() {
    }

    Pago(SolicitudTramite solicitud, String referencia, BigDecimal valor) {
        this.solicitud = solicitud;
        this.referenciaOrden = referencia;
        this.valor = valor;
        this.medioPago = MEDIO_PSE;
        this.estado = EstadoPago.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    public boolean estaPendiente() {
        return estado == EstadoPago.PENDIENTE;
    }

    public void confirmar(String banco) {
        exigirPendiente();
        this.estado = EstadoPago.APROBADO;
        this.banco = banco;
        this.fechaConfirmacion = LocalDateTime.now();
    }

    public void rechazar(String banco) {
        exigirPendiente();
        this.estado = EstadoPago.RECHAZADO;
        this.banco = banco;
        this.fechaConfirmacion = LocalDateTime.now();
    }

    /** Una orden pendiente se cancela cuando el ciudadano genera una nueva. */
    public void cancelar() {
        if (estaPendiente()) {
            this.estado = EstadoPago.CANCELADO;
        }
    }

    private void exigirPendiente() {
        if (!estaPendiente()) {
            throw new TransicionInvalidaException("Esta orden de pago ya fue procesada.");
        }
    }

    public Integer getIdPago() { return idPago; }
    public String getReferenciaOrden() { return referenciaOrden; }
    public BigDecimal getValor() { return valor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaConfirmacion() { return fechaConfirmacion; }
    public String getMedioPago() { return medioPago; }
    public EstadoPago getEstado() { return estado; }
    public String getBanco() { return banco; }
    public SolicitudTramite getSolicitud() { return solicitud; }
}
