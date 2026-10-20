package com.gld.tramites.controller;

import com.gld.tramites.dto.*;
import com.gld.tramites.security.UsuarioAutenticado;
import com.gld.tramites.service.GestorTramitesService;
import com.gld.tramites.service.GestorTramitesService.ArchivoDescarga;
import com.gld.tramites.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * MVC · Controlador de solicitudes · /api/v1/tramites/solicitudes
 * GRASP · Controlador: traduce HTTP a llamadas del Gestor_Tramites, sin reglas de negocio.
 */
@RestController
@RequestMapping("/solicitudes")
public class SolicitudController {

    private final GestorTramitesService gestor;
    private final PagoService pagos;

    public SolicitudController(GestorTramitesService gestor, PagoService pagos) {
        this.gestor = gestor;
        this.pagos = pagos;
    }

    /** Radicar: multipart con idTipoTramite, observaciones y un archivo por requisito ("requisito-{id}"). */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudDetalle radicar(@AuthenticationPrincipal UsuarioAutenticado ciudadano,
                                    @RequestParam("idTipoTramite") Integer idTipoTramite,
                                    @RequestParam(name = "observaciones", required = false) String observaciones,
                                    MultipartHttpServletRequest peticion) {
        return gestor.radicarSolicitud(ciudadano, idTipoTramite, observaciones, peticion.getFileMap());
    }

    /** Ciudadano: sus solicitudes. Funcionario: bandeja del municipio (grupo POR_REVISAR, EN_ESPERA, RESUELTAS o TODAS). */
    @GetMapping
    public List<SolicitudResumen> listar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                         @RequestParam(name = "grupo", required = false) String grupo) {
        return gestor.listar(usuario, grupo);
    }

    @GetMapping("/{id}")
    public SolicitudDetalle consultar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable("id") Integer id) {
        return gestor.consultarSolicitud(usuario, id);
    }

    @GetMapping("/{id}/documentos/{idDocumento}")
    public ResponseEntity<byte[]> descargar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                            @PathVariable("id") Integer id,
                                            @PathVariable("idDocumento") Integer idDocumento) {
        ArchivoDescarga archivo = gestor.descargar(usuario, id, idDocumento);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.tipoContenido()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(archivo.nombre(), StandardCharsets.UTF_8).build().toString())
                .body(archivo.contenido());
    }

    /** Ciudadano: responde a la solicitud de información con un comentario y/o documentos. */
    @PostMapping(value = "/{id}/correccion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SolicitudDetalle corregir(@AuthenticationPrincipal UsuarioAutenticado ciudadano,
                                     @PathVariable("id") Integer id,
                                     @RequestParam(name = "comentario", required = false) String comentario,
                                     MultipartHttpServletRequest peticion) {
        return gestor.reenviarCorreccion(ciudadano, id, comentario, peticion.getFileMap());
    }

    /** Ciudadano: crea la orden de pago y obtiene la referencia para la pasarela. */
    @PostMapping("/{id}/pagos")
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenPago iniciarPago(@AuthenticationPrincipal UsuarioAutenticado ciudadano, @PathVariable("id") Integer id) {
        return pagos.iniciarPago(ciudadano, id);
    }

    /** Funcionario: pide información adicional al ciudadano. */
    @PostMapping("/{id}/solicitud-informacion")
    public SolicitudDetalle solicitarInformacion(@AuthenticationPrincipal UsuarioAutenticado funcionario,
                                                 @PathVariable("id") Integer id, @Valid @RequestBody Motivo motivo) {
        return gestor.solicitarInformacion(funcionario, id, motivo.motivo());
    }

    /** Funcionario: aprueba (emite el documento oficial) o rechaza con motivo. */
    @PostMapping("/{id}/resolucion")
    public SolicitudDetalle resolver(@AuthenticationPrincipal UsuarioAutenticado funcionario,
                                     @PathVariable("id") Integer id, @Valid @RequestBody Resolucion resolucion) {
        return gestor.resolverSolicitud(funcionario, id, resolucion);
    }
}
