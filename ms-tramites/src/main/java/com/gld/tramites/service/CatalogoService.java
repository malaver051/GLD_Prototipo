package com.gld.tramites.service;

import com.gld.tramites.dto.TipoTramiteDto;
import com.gld.tramites.dto.TipoTramiteEntrada;
import com.gld.tramites.exception.ReglaNegocioException;
import com.gld.tramites.model.DatosRequisito;
import com.gld.tramites.model.EstadoTipoTramite;
import com.gld.tramites.model.TipoTramite;
import com.gld.tramites.repository.TipoTramiteRepository;
import com.gld.tramites.security.UsuarioAutenticado;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Catálogo de trámites de cada municipio.
 * El ciudadano ve los habilitados de su municipio; el funcionario lo administra.
 */
@Service
@Transactional
public class CatalogoService {

    private final TipoTramiteRepository tipos;
    private final PlantillaCatalogo plantilla;

    public CatalogoService(TipoTramiteRepository tipos, PlantillaCatalogo plantilla) {
        this.tipos = tipos;
        this.plantilla = plantilla;
    }

    // ------------------------------------------------------------------ ciudadano

    /** Método consultarCatalogo del diagrama de clases. */
    public List<TipoTramiteDto> consultarCatalogo(UsuarioAutenticado ciudadano) {
        Integer municipio = municipioDe(ciudadano);
        plantilla.asegurarCatalogo(municipio);
        return tipos.findByIdMunicipioAndEstadoOrderByNombreAsc(municipio, EstadoTipoTramite.HABILITADO).stream()
                .map(TipoTramiteDto::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public TipoTramiteDto consultarTipo(UsuarioAutenticado ciudadano, Integer idTipo) {
        return TipoTramiteDto.de(tipoDisponible(ciudadano, idTipo));
    }

    /** Tipo de trámite que el ciudadano puede solicitar: de su municipio y habilitado. */
    public TipoTramite tipoDisponible(UsuarioAutenticado ciudadano, Integer idTipo) {
        return tipos.findById(idTipo)
                .filter(t -> t.perteneceA(municipioDe(ciudadano)) && t.estaHabilitado())
                .orElseThrow(() -> ReglaNegocioException.noEncontrado("Este trámite no está disponible en tu municipio."));
    }

    // ------------------------------------------------------------------ funcionario

    public List<TipoTramiteDto> listarParaAdministrar(UsuarioAutenticado funcionario) {
        Integer municipio = municipioDe(funcionario);
        plantilla.asegurarCatalogo(municipio);
        return tipos.findByIdMunicipioOrderByNombreAsc(municipio).stream().map(TipoTramiteDto::de).toList();
    }

    public TipoTramiteDto crear(UsuarioAutenticado funcionario, TipoTramiteEntrada datos) {
        TipoTramite tipo = new TipoTramite(municipioDe(funcionario), datos.nombre().trim(), datos.descripcion().trim(),
                datos.dependencia().trim(), datos.costo(), datos.plazoDias());
        tipo.sincronizarRequisitos(requisitos(datos));
        return guardar(tipo);
    }

    public TipoTramiteDto actualizar(UsuarioAutenticado funcionario, Integer idTipo, TipoTramiteEntrada datos) {
        TipoTramite tipo = delMunicipio(funcionario, idTipo);
        tipo.actualizar(datos.nombre().trim(), datos.descripcion().trim(), datos.dependencia().trim(),
                datos.costo(), datos.plazoDias());
        tipo.sincronizarRequisitos(requisitos(datos));
        return guardar(tipo);
    }

    public TipoTramiteDto cambiarEstado(UsuarioAutenticado funcionario, Integer idTipo, boolean habilitado) {
        TipoTramite tipo = delMunicipio(funcionario, idTipo);
        if (habilitado) {
            tipo.habilitar();
        } else {
            tipo.deshabilitar();
        }
        return TipoTramiteDto.de(tipo);
    }

    // ------------------------------------------------------------------ apoyo

    private TipoTramiteDto guardar(TipoTramite tipo) {
        try {
            return TipoTramiteDto.de(tipos.saveAndFlush(tipo));
        } catch (DataIntegrityViolationException e) {
            throw ReglaNegocioException.conflicto("Ya existe un trámite con ese nombre en tu municipio.");
        }
    }

    private TipoTramite delMunicipio(UsuarioAutenticado funcionario, Integer idTipo) {
        return tipos.findById(idTipo)
                .filter(t -> t.perteneceA(municipioDe(funcionario)))
                .orElseThrow(() -> ReglaNegocioException.noEncontrado("El trámite no existe en tu municipio."));
    }

    private static List<DatosRequisito> requisitos(TipoTramiteEntrada datos) {
        return datos.requisitos().stream()
                .map(r -> new DatosRequisito(r.id(), r.nombre().trim(),
                        r.descripcion() == null ? "" : r.descripcion().trim(), r.obligatorio()))
                .toList();
    }

    static Integer municipioDe(UsuarioAutenticado usuario) {
        if (usuario.idMunicipio() == null) {
            throw ReglaNegocioException.prohibido("Tu cuenta no tiene un municipio asignado.");
        }
        return usuario.idMunicipio();
    }
}
