package com.gld.tramites.controller;

import com.gld.tramites.dto.TipoTramiteDto;
import com.gld.tramites.security.UsuarioAutenticado;
import com.gld.tramites.service.CatalogoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Catálogo de trámites del municipio del ciudadano · /api/v1/tramites/catalogo */
@RestController
@RequestMapping("/catalogo")
public class CatalogoController {

    private final CatalogoService catalogo;

    public CatalogoController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping
    public List<TipoTramiteDto> consultarCatalogo(@AuthenticationPrincipal UsuarioAutenticado ciudadano) {
        return catalogo.consultarCatalogo(ciudadano);
    }

    @GetMapping("/{id}")
    public TipoTramiteDto consultarTipo(@AuthenticationPrincipal UsuarioAutenticado ciudadano,
                                        @PathVariable("id") Integer id) {
        return catalogo.consultarTipo(ciudadano, id);
    }
}
