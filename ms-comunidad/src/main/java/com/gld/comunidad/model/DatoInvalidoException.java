package com.gld.comunidad.model;

/** Un dato que el usuario envió no cumple una regla del modelo (la API responde 422). */
public class DatoInvalidoException extends RuntimeException {

    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
