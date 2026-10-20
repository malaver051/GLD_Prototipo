package com.gld.identidad.dto;

import com.gld.identidad.model.Usuario;

import java.time.LocalDateTime;

/** Vista de un usuario para la consola del administrador. Nunca incluye contraseñas ni secretos. */
public record UsuarioDetalle(
        Integer id,
        String nombreCompleto,
        String tipoDocumento,
        String numeroDocumento,
        String correo,
        String telefono,
        String rol,
        MunicipioDto municipio,
        String estado,
        boolean debeCambiarClave,
        boolean mfaActivo,
        LocalDateTime fechaCreacion,
        LocalDateTime ultimoAcceso) {

    public static UsuarioDetalle de(Usuario u) {
        return new UsuarioDetalle(
                u.getIdUsuario(),
                u.getNombreCompleto(),
                u.getTipoDocumento().name(),
                u.getNumeroDocumento(),
                u.getCorreo(),
                u.getTelefono(),
                u.getRolPrincipal().name(),
                u.getMunicipio() == null ? null : MunicipioDto.de(u.getMunicipio()),
                u.getEstado().name(),
                u.isDebeCambiarClave(),
                u.isMfaActivo(),
                u.getFechaCreacion(),
                u.getUltimoAcceso());
    }
}
