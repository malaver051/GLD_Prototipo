package com.gld.comunidad.model;

/** Se lanza cuando una acción no está permitida en el estado actual de la incidencia. */
public class TransicionInvalidaException extends RuntimeException {

    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
