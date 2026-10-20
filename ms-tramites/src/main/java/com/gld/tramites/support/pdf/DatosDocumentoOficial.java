package com.gld.tramites.support.pdf;

import java.time.LocalDateTime;

/** Información que se imprime en el documento oficial de una solicitud aprobada. */
public record DatosDocumentoOficial(
        String municipio,
        String estado,
        String dependencia,
        String tramite,
        String numeroRadicado,
        LocalDateTime fechaRadicacion,
        LocalDateTime fechaEmision,
        String solicitante,
        String documentoSolicitante,
        String correoSolicitante,
        String observacionesSolicitante,
        String resumenPago,
        String comentarioFuncionario,
        String funcionario,
        String codigoVerificacion,
        String urlVerificacion,
        String selloSha256) {
}
