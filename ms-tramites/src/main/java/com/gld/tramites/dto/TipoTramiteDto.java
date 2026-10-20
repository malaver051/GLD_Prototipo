package com.gld.tramites.dto;

import com.gld.tramites.model.TipoTramite;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TipoTramiteDto(Integer id, String nombre, String descripcion, String dependencia, BigDecimal costo,
                             int plazoDias, boolean requierePago, String estado, List<RequisitoDto> requisitos,
                             LocalDateTime fechaActualizacion) {

    public static TipoTramiteDto de(TipoTramite t) {
        return new TipoTramiteDto(t.getIdTramite(), t.getNombre(), t.getDescripcion(), t.getDependencia(), t.getCosto(),
                t.getPlazoDias(), t.requierePago(), t.getEstado().name(),
                t.getRequisitos().stream().map(RequisitoDto::de).toList(), t.getFechaActualizacion());
    }
}
