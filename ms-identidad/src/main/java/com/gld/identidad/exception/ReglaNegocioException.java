package com.gld.identidad.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

/** Error de negocio con estado HTTP y, opcionalmente, una lista de detalles para mostrar. */
public class ReglaNegocioException extends RuntimeException {

    private final HttpStatus estado;
    private final List<String> detalles;

    public ReglaNegocioException(HttpStatus estado, String mensaje) {
        this(estado, mensaje, List.of());
    }

    public ReglaNegocioException(HttpStatus estado, String mensaje, List<String> detalles) {
        super(mensaje);
        this.estado = estado;
        this.detalles = detalles;
    }

    public HttpStatus getEstado() { return estado; }
    public List<String> getDetalles() { return detalles; }

    // Fábricas de los errores más frecuentes
    public static ReglaNegocioException noEncontrado(String mensaje) {
        return new ReglaNegocioException(HttpStatus.NOT_FOUND, mensaje);
    }

    public static ReglaNegocioException conflicto(String mensaje) {
        return new ReglaNegocioException(HttpStatus.CONFLICT, mensaje);
    }

    public static ReglaNegocioException invalido(String mensaje) {
        return new ReglaNegocioException(HttpStatus.UNPROCESSABLE_ENTITY, mensaje);
    }

    public static ReglaNegocioException noAutorizado(String mensaje) {
        return new ReglaNegocioException(HttpStatus.UNAUTHORIZED, mensaje);
    }

    public static ReglaNegocioException prohibido(String mensaje) {
        return new ReglaNegocioException(HttpStatus.FORBIDDEN, mensaje);
    }
}
