package com.gld.tramites.model;

/** Se lanza cuando una acción no está permitida en el estado actual de la solicitud. */
public class TransicionInvalidaException extends RuntimeException {

    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
