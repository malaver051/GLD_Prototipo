package com.gld.identidad.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** MUNICIPIO del modelo relacional. "departamento" guarda la entidad federativa. */
@Entity
@Table(name = "municipio")
public class Municipio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_municipio")
    private Integer idMunicipio;

    @Column(name = "codigo_municipio", nullable = false, unique = true, length = 5)
    private String codigoMunicipio;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "departamento", nullable = false)
    private String departamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoMunicipio estado;

    @Column(name = "fecha_incorporacion", nullable = false)
    private LocalDateTime fechaIncorporacion;

    protected Municipio() {
    }

    /** Experto en información: el municipio sabe si admite usuarios nuevos. */
    public boolean estaActivo() {
        return estado == EstadoMunicipio.ACTIVO;
    }

    public void habilitar() {
        this.estado = EstadoMunicipio.ACTIVO;
    }

    public void deshabilitar() {
        this.estado = EstadoMunicipio.INACTIVO;
    }

    public Integer getIdMunicipio() { return idMunicipio; }
    public String getCodigoMunicipio() { return codigoMunicipio; }
    public String getNombre() { return nombre; }
    public String getDepartamento() { return departamento; }
    public EstadoMunicipio getEstado() { return estado; }
    public LocalDateTime getFechaIncorporacion() { return fechaIncorporacion; }
}
