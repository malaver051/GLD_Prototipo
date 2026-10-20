package com.gld.tramites.controller;

import com.gld.tramites.dto.CambioEstadoTramite;
import com.gld.tramites.dto.TipoTramiteDto;
import com.gld.tramites.dto.TipoTramiteEntrada;
import com.gld.tramites.security.UsuarioAutenticado;
import com.gld.tramites.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** El funcionario administra el catálogo de su municipio · /api/v1/tramites/admin/tipos-tramite */
@RestController
@RequestMapping("/admin/tipos-tramite")
public class AdministracionCatalogoController {

    private final CatalogoService catalogo;

    public AdministracionCatalogoController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping
    public List<TipoTramiteDto> listar(@AuthenticationPrincipal UsuarioAutenticado funcionario) {
        return catalogo.listarParaAdministrar(funcionario);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TipoTramiteDto crear(@AuthenticationPrincipal UsuarioAutenticado funcionario,
                                @Valid @RequestBody TipoTramiteEntrada datos) {
        return catalogo.crear(funcionario, datos);
    }

    @PutMapping("/{id}")
    public TipoTramiteDto actualizar(@AuthenticationPrincipal UsuarioAutenticado funcionario,
                                     @PathVariable("id") Integer id, @Valid @RequestBody TipoTramiteEntrada datos) {
        return catalogo.actualizar(funcionario, id, datos);
    }

    @PatchMapping("/{id}/estado")
    public TipoTramiteDto cambiarEstado(@AuthenticationPrincipal UsuarioAutenticado funcionario,
                                        @PathVariable("id") Integer id, @Valid @RequestBody CambioEstadoTramite cambio) {
        return catalogo.cambiarEstado(funcionario, id, cambio.habilitado());
    }
}
