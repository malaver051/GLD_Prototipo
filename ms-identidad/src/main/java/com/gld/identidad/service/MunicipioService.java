package com.gld.identidad.service;

import com.gld.identidad.dto.MunicipioDto;
import com.gld.identidad.model.EstadoMunicipio;
import com.gld.identidad.repository.MunicipioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MunicipioService {

    private final MunicipioRepository municipios;

    public MunicipioService(MunicipioRepository municipios) {
        this.municipios = municipios;
    }

    public List<MunicipioDto> listarActivos() {
        return municipios.findByEstadoOrderByDepartamentoAscNombreAsc(EstadoMunicipio.ACTIVO).stream()
                .map(MunicipioDto::de)
                .toList();
    }
}
