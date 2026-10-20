package com.gld.identidad.controller;

import com.gld.identidad.dto.MunicipioDto;
import com.gld.identidad.dto.UsuarioDetalle;
import com.gld.identidad.security.UsuarioAutenticado;
import com.gld.identidad.service.AutenticacionService;
import com.gld.identidad.service.MunicipioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Consultas generales: municipios (público) y perfil del usuario en sesión. */
@RestController
public class CatalogoController {

    private final MunicipioService municipios;
    private final AutenticacionService autenticacion;

    public CatalogoController(MunicipioService municipios, AutenticacionService autenticacion) {
        this.municipios = municipios;
        this.autenticacion = autenticacion;
    }

    @GetMapping("/municipios")
    public List<MunicipioDto> municipios() {
        return municipios.listarActivos();
    }

    @GetMapping("/usuarios/me")
    public UsuarioDetalle perfil(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return autenticacion.perfil(usuario.idUsuario());
    }
}
