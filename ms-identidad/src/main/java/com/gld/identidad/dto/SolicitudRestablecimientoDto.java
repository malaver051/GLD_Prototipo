package com.gld.identidad.dto;

import com.gld.identidad.model.SolicitudRestablecimiento;

import java.time.LocalDateTime;

public record SolicitudRestablecimientoDto(
        Integer id,
        Integer idUsuario,
        String nombreCompleto,
        String correo,
        String municipio,
        String estado,
        LocalDateTime fechaSolicitud) {

    public static SolicitudRestablecimientoDto de(SolicitudRestablecimiento s) {
        var u = s.getUsuario();
        return new SolicitudRestablecimientoDto(
                s.getIdSolicitud(),
                u.getIdUsuario(),
                u.getNombreCompleto(),
                u.getCorreo(),
                u.getMunicipio() == null ? null : u.getMunicipio().getNombre(),
                s.getEstado().name(),
                s.getFechaSolicitud());
    }
}
