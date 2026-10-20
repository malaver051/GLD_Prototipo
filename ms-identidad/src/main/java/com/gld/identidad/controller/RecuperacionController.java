package com.gld.identidad.controller;

import com.gld.identidad.dto.*;
import com.gld.identidad.service.RecuperacionClaveService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** "¿Olvidaste tu contraseña?" · Ruta base: /api/v1/identidad/auth/recuperacion */
@RestController
@RequestMapping("/auth/recuperacion")
public class RecuperacionController {

    private final RecuperacionClaveService recuperacion;

    public RecuperacionController(RecuperacionClaveService recuperacion) {
        this.recuperacion = recuperacion;
    }

    @PostMapping
    public RespuestaRecuperacion solicitar(@Valid @RequestBody SolicitudRecuperacion solicitud) {
        return recuperacion.solicitar(solicitud.correo());
    }

    /** Permite al frontend saber si el enlace sigue vigente antes de mostrar el formulario. */
    @GetMapping("/enlace")
    public EstadoEnlace consultarEnlace(@RequestParam("token") String token) {
        return recuperacion.consultarEnlace(token);
    }

    @PostMapping("/restablecer")
    public Mensaje restablecer(@Valid @RequestBody RestablecimientoClave datos) {
        return recuperacion.restablecer(datos);
    }
}
