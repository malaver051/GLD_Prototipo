package com.gld.comunidad.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Centro aproximado de un municipio, para abrir el mapa y validar reportes. */
@Entity
@Table(name = "centro_municipio")
public class CentroMunicipio {

    @Id
    @Column(name = "codigo_municipio")
    private String codigoMunicipio;

    @Column(name = "latitud", nullable = false)
    private BigDecimal latitud;

    @Column(name = "longitud", nullable = false)
    private BigDecimal longitud;

    protected CentroMunicipio() {
    }

    public String getCodigoMunicipio() { return codigoMunicipio; }

    public Ubicacion ubicacion() {
        return new Ubicacion(latitud.doubleValue(), longitud.doubleValue());
    }
}
