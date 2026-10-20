package com.gld.comunidad.model;

import jakarta.persistence.*;

/** Dependencia municipal que atiende incidencias (lista fija del prototipo). */
@Entity
@Table(name = "dependencia")
public class Dependencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dependencia")
    private Integer idDependencia;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_sugerida")
    private Categoria categoriaSugerida;

    protected Dependencia() {
    }

    /** Las dependencias se cargan con Flyway; este constructor existe para las pruebas. */
    Dependencia(Integer id, String nombre, String descripcion, Categoria categoriaSugerida) {
        this.idDependencia = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoriaSugerida = categoriaSugerida;
    }

    public Integer getIdDependencia() { return idDependencia; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public Categoria getCategoriaSugerida() { return categoriaSugerida; }
}
