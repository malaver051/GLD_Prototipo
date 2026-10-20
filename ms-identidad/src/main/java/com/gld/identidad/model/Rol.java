package com.gld.identidad.model;

import jakarta.persistence.*;

/** ROL del modelo relacional. */
@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    @Enumerated(EnumType.STRING)
    @Column(name = "nombre", nullable = false, unique = true)
    private NombreRol nombre;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "ambito", nullable = false)
    private AmbitoRol ambito;

    protected Rol() {
    }

    /** Un administrador solo puede asignar roles del ámbito de gobierno. */
    public boolean esAsignablePorAdministrador() {
        return ambito == AmbitoRol.GOBIERNO;
    }

    public Integer getIdRol() { return idRol; }
    public NombreRol getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public AmbitoRol getAmbito() { return ambito; }
}
