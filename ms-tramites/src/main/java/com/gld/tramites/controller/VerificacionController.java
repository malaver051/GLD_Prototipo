package com.gld.tramites.controller;

import com.gld.tramites.dto.Verificacion;
import com.gld.tramites.service.DocumentoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Verificación pública de documentos oficiales · /api/v1/tramites/verificacion/{codigo} */
@RestController
public class VerificacionController {

    private final DocumentoService documentos;

    public VerificacionController(DocumentoService documentos) {
        this.documentos = documentos;
    }

    @GetMapping("/verificacion/{codigo}")
    public Verificacion verificar(@PathVariable("codigo") String codigo) {
        return documentos.verificar(codigo);
    }
}
