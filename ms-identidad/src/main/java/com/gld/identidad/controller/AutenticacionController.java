package com.gld.identidad.controller;

import com.gld.identidad.dto.*;
import com.gld.identidad.security.UsuarioAutenticado;
import com.gld.identidad.service.AutenticacionService;
import com.gld.identidad.service.RegistroCiudadanoService;
import com.gld.identidad.service.SegundoFactorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * MVC · Controlador: recibe las peticiones de autenticación y delega en los servicios.
 * GRASP · Controlador: no contiene reglas de negocio, solo coordina la respuesta.
 * Ruta base: /api/v1/identidad/auth
 */
@RestController
@RequestMapping("/auth")
public class AutenticacionController {

    private final AutenticacionService autenticacion;
    private final RegistroCiudadanoService registro;
    private final SegundoFactorService segundoFactor;

    public AutenticacionController(AutenticacionService autenticacion, RegistroCiudadanoService registro,
                                   SegundoFactorService segundoFactor) {
        this.autenticacion = autenticacion;
        this.registro = registro;
        this.segundoFactor = segundoFactor;
    }

    /** Paso 1 para todos: correo (o "admin") y contraseña. */
    @PostMapping("/ingreso")
    public RespuestaIngreso ingresar(@Valid @RequestBody SolicitudIngreso solicitud) {
        return autenticacion.ingresar(solicitud);
    }

    /** Registro público de ciudadanos; responde con el paso CONFIGURAR_MFA. */
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestaIngreso registrar(@Valid @RequestBody SolicitudRegistro solicitud) {
        return registro.registrar(solicitud);
    }

    /** Datos para el QR de Microsoft Authenticator (token MFA_CONFIGURACION). */
    @GetMapping("/mfa/configuracion")
    public ConfiguracionMfa configuracionMfa(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return segundoFactor.obtenerConfiguracion(usuario.idUsuario());
    }

    /** Primer código: vincula el celular y concede el acceso (token MFA_CONFIGURACION). */
    @PostMapping("/mfa/activacion")
    public RespuestaIngreso activarMfa(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                       @Valid @RequestBody CodigoMfa codigo) {
        return segundoFactor.activar(usuario.idUsuario(), codigo.codigo());
    }

    /** Código en cada ingreso del ciudadano (token MFA). */
    @PostMapping("/mfa/verificacion")
    public RespuestaIngreso verificarMfa(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                         @Valid @RequestBody CodigoMfa codigo) {
        return segundoFactor.verificar(usuario.idUsuario(), codigo.codigo());
    }

    /** Cambio obligatorio de la clave temporal del funcionario (token CAMBIO_CLAVE). */
    @PostMapping("/clave-inicial")
    public RespuestaIngreso cambiarClaveInicial(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                @Valid @RequestBody CambioClave cambio) {
        return autenticacion.cambiarClaveInicial(usuario.idUsuario(), cambio);
    }
}
