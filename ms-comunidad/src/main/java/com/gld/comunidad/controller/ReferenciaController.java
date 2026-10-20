package com.gld.comunidad.controller;

import com.gld.comunidad.dto.DependenciaDto;
import com.gld.comunidad.dto.MapaMunicipio;
import com.gld.comunidad.security.UsuarioAutenticado;
import com.gld.comunidad.service.GestorIncidenciasService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** MVC · Controlador de datos de referencia: dependencias y centro del mapa del municipio. */
@RestController
public class ReferenciaController {

    private final GestorIncidenciasService gestor;

    public ReferenciaController(GestorIncidenciasService gestor) {
        this.gestor = gestor;
    }

    @GetMapping("/dependencias")
    public List<DependenciaDto> dependencias() {
        return gestor.dependencias();
    }

    @GetMapping("/mapa/municipio")
    public MapaMunicipio mapa(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return gestor.mapa(usuario);
    }
}
