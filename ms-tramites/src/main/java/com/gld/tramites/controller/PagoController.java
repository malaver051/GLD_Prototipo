package com.gld.tramites.controller;

import com.gld.tramites.dto.OrdenPago;
import com.gld.tramites.dto.ResultadoPago;
import com.gld.tramites.security.UsuarioAutenticado;
import com.gld.tramites.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Pasarela de pagos simulada · /api/v1/tramites/pagos
 * En producción, el resultado llegaría como webhook firmado del proveedor (PSE).
 */
@RestController
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagos;

    public PagoController(PagoService pagos) {
        this.pagos = pagos;
    }

    @GetMapping("/{referencia}")
    public OrdenPago consultar(@AuthenticationPrincipal UsuarioAutenticado ciudadano,
                               @PathVariable("referencia") String referencia) {
        return pagos.consultarOrden(ciudadano, referencia);
    }

    @PostMapping("/{referencia}/resultado")
    public OrdenPago registrarResultado(@AuthenticationPrincipal UsuarioAutenticado ciudadano,
                                        @PathVariable("referencia") String referencia,
                                        @Valid @RequestBody ResultadoPago resultado) {
        return pagos.registrarResultadoPago(ciudadano, referencia, resultado);
    }
}
