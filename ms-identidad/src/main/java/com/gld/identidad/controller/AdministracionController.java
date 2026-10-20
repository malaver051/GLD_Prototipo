package com.gld.identidad.controller;

import com.gld.identidad.dto.*;
import com.gld.identidad.model.NombreRol;
import com.gld.identidad.security.UsuarioAutenticado;
import com.gld.identidad.service.GestionUsuariosService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Consola del administrador (solo rol ADMIN) · Ruta base: /api/v1/identidad/admin */
@RestController
@RequestMapping("/admin")
public class AdministracionController {

    private final GestionUsuariosService gestion;

    public AdministracionController(GestionUsuariosService gestion) {
        this.gestion = gestion;
    }

    @GetMapping("/usuarios")
    public List<UsuarioDetalle> listar(@RequestParam(name = "rol", defaultValue = "FUNCIONARIO") NombreRol rol,
                                       @RequestParam(name = "q", required = false) String q) {
        return gestion.listar(rol, q);
    }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDetalle crearFuncionario(@Valid @RequestBody NuevoFuncionario datos) {
        return gestion.crearFuncionario(datos);
    }

    @PatchMapping("/usuarios/{id}/estado")
    public UsuarioDetalle cambiarEstado(@PathVariable("id") Integer id, @Valid @RequestBody CambioEstadoUsuario cambio) {
        return gestion.cambiarEstado(id, cambio.activo());
    }

    @PostMapping("/usuarios/{id}/restablecimiento-clave")
    public Mensaje restablecerClave(@PathVariable("id") Integer id,
                                    @AuthenticationPrincipal UsuarioAutenticado admin) {
        return gestion.restablecerClave(id, admin.idUsuario());
    }

    @GetMapping("/roles")
    public List<RolDto> rolesAsignables() {
        return gestion.rolesAsignables();
    }

    @GetMapping("/solicitudes-restablecimiento")
    public List<SolicitudRestablecimientoDto> solicitudesPendientes() {
        return gestion.solicitudesPendientes();
    }

    @PostMapping("/solicitudes-restablecimiento/{id}/atencion")
    public Mensaje atenderSolicitud(@PathVariable("id") Integer id,
                                    @AuthenticationPrincipal UsuarioAutenticado admin) {
        return gestion.atenderSolicitud(id, admin.idUsuario());
    }
}
