package com.gld.comunidad.controller;

import com.gld.comunidad.dto.*;
import com.gld.comunidad.security.UsuarioAutenticado;
import com.gld.comunidad.service.GestorIncidenciasService;
import com.gld.comunidad.service.GestorIncidenciasService.FotoDescarga;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * MVC · Controlador de incidencias · /api/v1/comunidad/incidencias
 * GRASP · Controlador: traduce HTTP a llamadas del gestor, sin reglas de negocio.
 */
@RestController
@RequestMapping("/incidencias")
public class IncidenciaController {

    private final GestorIncidenciasService gestor;

    public IncidenciaController(GestorIncidenciasService gestor) {
        this.gestor = gestor;
    }

    /** HU-08 · Ciudadano: multipart con categoria, descripcion, latitud, longitud, referencia y foto (opcional). */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public IncidenciaDetalle reportar(@AuthenticationPrincipal UsuarioAutenticado ciudadano,
                                      @RequestParam(name = "categoria", required = false) String categoria,
                                      @RequestParam(name = "descripcion", required = false) String descripcion,
                                      @RequestParam(name = "referencia", required = false) String referencia,
                                      @RequestParam(name = "latitud", required = false) Double latitud,
                                      @RequestParam(name = "longitud", required = false) Double longitud,
                                      @RequestParam(name = "foto", required = false) MultipartFile foto) {
        return gestor.reportar(ciudadano, categoria, descripcion, referencia, latitud, longitud, foto);
    }

    /** HU-09 / HU-10 · Ciudadano: las suyas. Funcionario: las del municipio, con filtros opcionales. */
    @GetMapping
    public List<IncidenciaResumen> listar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                          @RequestParam(name = "estado", required = false) String estado,
                                          @RequestParam(name = "categoria", required = false) String categoria,
                                          @RequestParam(name = "idDependencia", required = false) Integer idDependencia) {
        return gestor.listar(usuario, estado, categoria, idDependencia);
    }

    /** Funcionario: cifras del municipio (por estado y calificación promedio). */
    @GetMapping("/resumen")
    public ResumenIncidencias resumen(@AuthenticationPrincipal UsuarioAutenticado funcionario) {
        return gestor.resumen(funcionario);
    }

    @GetMapping("/{id}")
    public IncidenciaDetalle consultar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                       @PathVariable("id") Integer id) {
        return gestor.consultar(usuario, id);
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> foto(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                       @PathVariable("id") Integer id) {
        FotoDescarga foto = gestor.foto(usuario, id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(foto.tipo()))
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(foto.nombre(), StandardCharsets.UTF_8).build().toString())
                .body(foto.contenido());
    }

    /** HU-10 · Funcionario: asignar o reasignar la dependencia. */
    @PutMapping("/{id}/asignacion")
    public IncidenciaDetalle asignar(@AuthenticationPrincipal UsuarioAutenticado funcionario,
                                     @PathVariable("id") Integer id,
                                     @Valid @RequestBody AsignacionEntrada entrada) {
        return gestor.asignar(funcionario, id, entrada.idDependencia(), entrada.comentario());
    }

    /** HU-11 · Funcionario: EN_ATENCION o RESUELTA (la solución va en el comentario). */
    @PutMapping("/{id}/estado")
    public IncidenciaDetalle cambiarEstado(@AuthenticationPrincipal UsuarioAutenticado funcionario,
                                           @PathVariable("id") Integer id,
                                           @Valid @RequestBody CambioEstadoEntrada entrada) {
        return gestor.cambiarEstado(funcionario, id, entrada.estado(), entrada.comentario());
    }

    /** HU-12 · Ciudadano: calificar de 0 a 10; la incidencia queda cerrada. */
    @PostMapping("/{id}/calificacion")
    public IncidenciaDetalle calificar(@AuthenticationPrincipal UsuarioAutenticado ciudadano,
                                       @PathVariable("id") Integer id,
                                       @Valid @RequestBody CalificacionEntrada entrada) {
        return gestor.calificar(ciudadano, id, entrada.calificacion(), entrada.comentario());
    }

    /** Ciudadano: reabrir una incidencia resuelta (una sola vez). */
    @PostMapping("/{id}/reapertura")
    public IncidenciaDetalle reabrir(@AuthenticationPrincipal UsuarioAutenticado ciudadano,
                                     @PathVariable("id") Integer id,
                                     @Valid @RequestBody ReaperturaEntrada entrada) {
        return gestor.reabrir(ciudadano, id, entrada.motivo());
    }
}
