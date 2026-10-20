package com.gld.tramites.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import com.gld.tramites.model.TransicionInvalidaException;

import java.util.List;

/**
 * Convierte las excepciones en respuestas RFC 7807 (application/problem+json).
 * El frontend muestra "detail" y, si existe, la lista "errores".
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail reglaNegocio(ReglaNegocioException ex) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(ex.getEstado(), ex.getMessage());
        if (!ex.getDetalles().isEmpty()) {
            p.setProperty("errores", ex.getDetalles());
        }
        return p;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacion(MethodArgumentNotValidException ex) {
        List<String> errores = ex.getBindingResult().getAllErrors().stream()
                .map(e -> e.getDefaultMessage())
                .distinct()
                .toList();
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Revisa los datos del formulario.");
        p.setProperty("errores", errores);
        return p;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail cuerpoInvalido(HttpMessageNotReadableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "La solicitud tiene un formato inválido.");
    }

    /** Patrón State: la acción no está permitida en el estado actual de la solicitud. */
    @ExceptionHandler(TransicionInvalidaException.class)
    public ProblemDetail transicionInvalida(TransicionInvalidaException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail archivoGrande(MaxUploadSizeExceededException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE,
                "Los archivos superan el tamaño permitido: máximo 5 MB por archivo.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El valor de «" + ex.getName() + "» no es válido.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception ex) {
        // Errores estándar de Spring MVC (ruta inexistente, método no permitido, parámetro faltante...)
        if (ex instanceof ErrorResponse respuesta) {
            return respuesta.getBody();
        }
        log.error("Error no controlado", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intenta de nuevo en unos minutos.");
    }
}
