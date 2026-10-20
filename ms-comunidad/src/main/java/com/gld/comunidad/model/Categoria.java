package com.gld.comunidad.model;

/** Categorías de incidencia del contrato OpenAPI (07-comunidad.yaml). */
public enum Categoria {
    VIAS("Vías y baches"),
    ALUMBRADO("Alumbrado público"),
    ASEO("Aseo y basura"),
    SEGURIDAD("Seguridad"),
    OTRO("Otro");

    private final String etiqueta;

    Categoria(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }
}
