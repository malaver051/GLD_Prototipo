package com.gld.tramites.model;

import jakarta.persistence.*;

/** REQUISITO_TRAMITE del modelo relacional. */
@Entity
@Table(name = "requisito_tramite")
public class RequisitoTramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_requisito")
    private Integer idRequisito;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "obligatorio", nullable = false)
    private boolean obligatorio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_tramite_id_tramite")
    private TipoTramite tipoTramite;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    protected RequisitoTramite() {
    }

    RequisitoTramite(TipoTramite tipoTramite, String nombre, String descripcion, boolean obligatorio, int orden) {
        this.tipoTramite = tipoTramite;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.obligatorio = obligatorio;
        this.orden = orden;
    }

    public void actualizar(String nombre, String descripcion, boolean obligatorio) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.obligatorio = obligatorio;
    }

    void ordenar(int orden) {
        this.orden = orden;
    }

    public Integer getIdRequisito() { return idRequisito; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public boolean isObligatorio() { return obligatorio; }
    public Integer getOrden() { return orden; }
    public TipoTramite getTipoTramite() { return tipoTramite; }
}
