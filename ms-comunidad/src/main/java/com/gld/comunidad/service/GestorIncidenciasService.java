package com.gld.comunidad.service;

import com.gld.comunidad.dto.DependenciaDto;
import com.gld.comunidad.dto.IncidenciaDetalle;
import com.gld.comunidad.dto.IncidenciaResumen;
import com.gld.comunidad.dto.MapaMunicipio;
import com.gld.comunidad.dto.ResumenIncidencias;
import com.gld.comunidad.exception.ReglaNegocioException;
import com.gld.comunidad.model.Categoria;
import com.gld.comunidad.model.DatosCiudadano;
import com.gld.comunidad.model.Dependencia;
import com.gld.comunidad.model.EstadoIncidencia;
import com.gld.comunidad.model.FotoAdjunta;
import com.gld.comunidad.model.Incidencia;
import com.gld.comunidad.model.Ubicacion;
import com.gld.comunidad.repository.DependenciaRepository;
import com.gld.comunidad.repository.IncidenciaRepository;
import com.gld.comunidad.security.UsuarioAutenticado;
import com.gld.comunidad.support.AlmacenamientoFotos;
import com.gld.comunidad.support.ClienteIdentidad;
import com.gld.comunidad.support.ClienteIdentidad.PerfilIdentidad;
import com.gld.comunidad.support.Codigos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Coordina los casos de uso de la épica GLD-E2: reportar (HU-08), consultar (HU-09),
 * asignar en el mapa (HU-10), atender y resolver (HU-11) y calificar (HU-12).
 *
 * GRASP · Controlador: recibe cada operación del sistema y delega las reglas en {@link Incidencia}
 * (Experto) y en {@link EstadoIncidencia} (State). No contiene reglas de transición propias.
 */
@Service
public class GestorIncidenciasService {

    public static final int MAXIMO_DESCRIPCION = 1000;
    public static final int MAXIMO_REFERENCIA = 200;

    private final IncidenciaRepository incidencias;
    private final DependenciaRepository dependencias;
    private final ClienteIdentidad identidad;
    private final MapaService mapa;
    private final AlmacenamientoFotos fotos;
    private final NotificacionesIncidencia notificaciones;
    private final Clock reloj;

    public GestorIncidenciasService(IncidenciaRepository incidencias, DependenciaRepository dependencias,
                                    ClienteIdentidad identidad, MapaService mapa, AlmacenamientoFotos fotos,
                                    NotificacionesIncidencia notificaciones, Clock reloj) {
        this.incidencias = incidencias;
        this.dependencias = dependencias;
        this.identidad = identidad;
        this.mapa = mapa;
        this.fotos = fotos;
        this.notificaciones = notificaciones;
        this.reloj = reloj;
    }

    // ------------------------------------------------------------------ ciudadano

    /** HU-08: reportar una incidencia con categoría, ubicación y foto opcional. */
    @Transactional
    public IncidenciaDetalle reportar(UsuarioAutenticado ciudadano, String categoria, String descripcion,
                                      String referencia, Double latitud, Double longitud, MultipartFile foto) {
        List<String> errores = new ArrayList<>();
        Categoria tipo = categoriaDe(categoria);
        if (tipo == null) errores.add("Elige la categoría de la incidencia.");
        if (vacio(descripcion)) errores.add("Describe el problema.");
        else if (descripcion.trim().length() > MAXIMO_DESCRIPCION) errores.add("La descripción admite máximo 1000 caracteres.");
        if (referencia != null && referencia.trim().length() > MAXIMO_REFERENCIA) errores.add("La referencia admite máximo 200 caracteres.");
        // Criterio de aceptación de HU-08: sin ubicación, el sistema la solicita
        if (latitud == null || longitud == null) errores.add("Marca en el mapa dónde está el problema.");
        if (!errores.isEmpty()) {
            throw new ReglaNegocioException(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "Revisa los datos del reporte.", errores);
        }
        boolean conFoto = foto != null && !foto.isEmpty();
        if (conFoto) fotos.validar(foto);

        PerfilIdentidad perfil = perfilCiudadano(ciudadano);
        Ubicacion ubicacion = new Ubicacion(latitud, longitud);
        mapa.validarDentroDelMunicipio(ubicacion, perfil.municipio());

        DatosCiudadano datos = new DatosCiudadano(perfil.id(), perfil.nombreCompleto(), perfil.correo(),
                perfil.municipio().id(), perfil.municipio().codigo(), perfil.municipio().nombre(),
                perfil.municipio().estado());
        String numero = Codigos.seguimiento(perfil.municipio().codigo(), incidencias.siguienteConsecutivo());
        Incidencia nueva = Incidencia.reportar(numero, tipo, descripcion.trim(), textoOpcional(referencia),
                ubicacion, datos, ahora());
        nueva = incidencias.saveAndFlush(nueva);
        if (conFoto) {
            FotoAdjunta guardada = fotos.guardar(nueva.getIdIncidencia(), foto);
            nueva.adjuntarFoto(guardada);
        }
        notificaciones.reportada(nueva);
        return detalle(nueva, ciudadano);
    }

    /** HU-12: calificar la atención; la incidencia queda cerrada. */
    @Transactional
    public IncidenciaDetalle calificar(UsuarioAutenticado ciudadano, Integer id, int puntaje, String comentario) {
        Incidencia i = delCiudadano(ciudadano, id);
        i.calificar(puntaje, comentario, ahora());
        return detalle(i, ciudadano);
    }

    /** El ciudadano no está conforme con la solución (se permite una vez). */
    @Transactional
    public IncidenciaDetalle reabrir(UsuarioAutenticado ciudadano, Integer id, String motivo) {
        Incidencia i = delCiudadano(ciudadano, id);
        i.reabrir(motivo, ahora());
        return detalle(i, ciudadano);
    }

    // ------------------------------------------------------------------ funcionario

    /** HU-10: asignar o reasignar la dependencia responsable. */
    @Transactional
    public IncidenciaDetalle asignar(UsuarioAutenticado funcionario, Integer id, Integer idDependencia,
                                     String comentario) {
        Incidencia i = delMunicipio(funcionario, id);
        Dependencia dependencia = dependencias.findById(idDependencia)
                .orElseThrow(() -> ReglaNegocioException.invalido("La dependencia elegida no existe."));
        PerfilIdentidad perfil = identidad.perfil(funcionario);
        i.asignar(dependencia, comentario, funcionario.idUsuario(), perfil.nombreCompleto(), ahora());
        notificaciones.asignada(i);
        return detalle(i, funcionario);
    }

    /** HU-11: PUT /incidencias/{id}/estado con EN_ATENCION o RESUELTA (la solución va en el comentario). */
    @Transactional
    public IncidenciaDetalle cambiarEstado(UsuarioAutenticado funcionario, Integer id, String estado,
                                           String comentario) {
        Incidencia i = delMunicipio(funcionario, id);
        PerfilIdentidad perfil = identidad.perfil(funcionario);
        String nuevo = estado == null ? "" : estado.trim().toUpperCase(Locale.ROOT);
        switch (nuevo) {
            case "EN_ATENCION" -> {
                i.iniciarAtencion(comentario, funcionario.idUsuario(), perfil.nombreCompleto(), ahora());
                notificaciones.enAtencion(i);
            }
            case "RESUELTA" -> {
                i.resolver(comentario, funcionario.idUsuario(), perfil.nombreCompleto(), ahora());
                notificaciones.resuelta(i);
            }
            case "ASIGNADA" -> throw ReglaNegocioException.invalido(
                    "Para asignar la incidencia usa la opción «Asignar dependencia».");
            case "CERRADA" -> throw ReglaNegocioException.invalido(
                    "La incidencia se cierra cuando el ciudadano califica la atención.");
            default -> throw ReglaNegocioException.invalido("El estado «" + estado + "» no es válido.");
        }
        return detalle(i, funcionario);
    }

    @Transactional(readOnly = true)
    public ResumenIncidencias resumen(UsuarioAutenticado funcionario) {
        List<Incidencia> todas = incidencias.findByIdMunicipioOrderByFechaReporteDesc(funcionario.idMunicipio());
        Map<String, Long> porEstado = new LinkedHashMap<>();
        Arrays.stream(EstadoIncidencia.values()).forEach(e -> porEstado.put(e.name(), 0L));
        todas.forEach(i -> porEstado.merge(i.getEstado().name(), 1L, Long::sum));
        long abiertas = todas.stream().filter(i -> i.getEstado().estaAbierta()).count();
        List<Integer> notas = todas.stream().map(Incidencia::getCalificacion).filter(n -> n != null).toList();
        Double promedio = notas.isEmpty() ? null
                : Math.round(notas.stream().mapToInt(Integer::intValue).average().orElse(0) * 10) / 10.0;
        return new ResumenIncidencias(porEstado, abiertas, promedio, notas.size());
    }

    // ------------------------------------------------------------------ ambos roles

    /** HU-09 y HU-10: el ciudadano ve las suyas; el funcionario, todas las de su municipio. */
    @Transactional(readOnly = true)
    public List<IncidenciaResumen> listar(UsuarioAutenticado usuario, String estado, String categoria,
                                          Integer idDependencia) {
        List<Incidencia> lista = usuario.esCiudadano()
                ? incidencias.findByIdCiudadanoOrderByFechaReporteDesc(usuario.idUsuario())
                : incidencias.findByIdMunicipioOrderByFechaReporteDesc(usuario.idMunicipio());
        return lista.stream()
                .filter(i -> vacio(estado) || i.getEstado().name().equalsIgnoreCase(estado))
                .filter(i -> vacio(categoria) || i.getCategoria().name().equalsIgnoreCase(categoria))
                .filter(i -> idDependencia == null
                        || (i.getDependencia() != null && i.getDependencia().getIdDependencia().equals(idDependencia)))
                .map(IncidenciaResumen::de)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IncidenciaDetalle consultar(UsuarioAutenticado usuario, Integer id) {
        return detalle(visible(usuario, id), usuario);
    }

    @Transactional(readOnly = true)
    public FotoDescarga foto(UsuarioAutenticado usuario, Integer id) {
        Incidencia i = visible(usuario, id);
        if (!i.tieneFoto()) {
            throw ReglaNegocioException.noEncontrado("Esta incidencia no tiene foto.");
        }
        return new FotoDescarga(i.getFotoNombre(), i.getFotoTipo(), fotos.leer(i.getFotoRuta()));
    }

    @Transactional(readOnly = true)
    public List<DependenciaDto> dependencias() {
        return dependencias.findAllByOrderByNombreAsc().stream().map(DependenciaDto::de).toList();
    }

    public MapaMunicipio mapa(UsuarioAutenticado usuario) {
        PerfilIdentidad perfil = identidad.perfil(usuario);
        if (perfil.municipio() == null) {
            throw ReglaNegocioException.conflicto("Tu cuenta no tiene un municipio asignado.");
        }
        return mapa.mapaDe(perfil.municipio());
    }

    public record FotoDescarga(String nombre, String tipo, byte[] contenido) {
    }

    // ------------------------------------------------------------------ apoyo

    private Incidencia visible(UsuarioAutenticado usuario, Integer id) {
        return usuario.esCiudadano() ? delCiudadano(usuario, id) : delMunicipio(usuario, id);
    }

    private Incidencia delCiudadano(UsuarioAutenticado ciudadano, Integer id) {
        return incidencias.findById(id)
                .filter(i -> i.perteneceACiudadano(ciudadano.idUsuario()))
                .orElseThrow(() -> ReglaNegocioException.noEncontrado("La incidencia no existe."));
    }

    private Incidencia delMunicipio(UsuarioAutenticado funcionario, Integer id) {
        return incidencias.findById(id)
                .filter(i -> i.perteneceAMunicipio(funcionario.idMunicipio()))
                .orElseThrow(() -> ReglaNegocioException.noEncontrado("La incidencia no existe en tu municipio."));
    }

    private PerfilIdentidad perfilCiudadano(UsuarioAutenticado ciudadano) {
        PerfilIdentidad perfil = identidad.perfil(ciudadano);
        if (perfil.municipio() == null || !perfil.municipio().id().equals(ciudadano.idMunicipio())) {
            throw ReglaNegocioException.conflicto("Tu cuenta no tiene un municipio válido. Comunícate con soporte.");
        }
        return perfil;
    }

    private IncidenciaDetalle detalle(Incidencia i, UsuarioAutenticado usuario) {
        List<String> acciones = new ArrayList<>();
        if (usuario.esCiudadano()) {
            if (i.getEstado() == EstadoIncidencia.RESUELTA) acciones.add("CALIFICAR");
            if (i.puedeReabrirse()) acciones.add("REABRIR");
        } else {
            if (i.getEstado().admiteAsignacion()) acciones.add("ASIGNAR");
            if (i.getEstado() == EstadoIncidencia.ASIGNADA) acciones.add("INICIAR_ATENCION");
            if (i.getEstado() == EstadoIncidencia.EN_ATENCION) acciones.add("RESOLVER");
        }
        List<IncidenciaDetalle.HistorialDto> historial = i.getHistorial().stream()
                .map(h -> new IncidenciaDetalle.HistorialDto(
                        h.getEstadoAnterior() == null ? null : h.getEstadoAnterior().name(),
                        h.getEstadoNuevo().name(), h.getEstadoNuevo().etiqueta(), h.getFechaCambio(),
                        h.getComentario(), h.getOrigenCambio().name(), h.getNombreUsuario()))
                .toList();
        IncidenciaDetalle.Foto foto = i.tieneFoto()
                ? new IncidenciaDetalle.Foto(i.getFotoNombre(), i.getFotoTipo(), i.getFotoTamanoBytes())
                : null;
        return new IncidenciaDetalle(i.getIdIncidencia(), i.getNumeroSeguimiento(), i.getCategoria().name(),
                i.getCategoria().etiqueta(), i.getDescripcion(), i.getReferencia(), i.ubicacion().latitud(),
                i.ubicacion().longitud(), i.getEstado().name(), i.getEstado().etiqueta(), i.getFechaReporte(),
                i.getFechaActualizacion(), i.getFechaResolucion(), i.getFechaCierre(), i.getMunicipioNombre(),
                i.getMunicipioEstado(), new IncidenciaDetalle.Ciudadano(i.getNombreCiudadano(), i.getCorreoCiudadano()),
                foto, i.getDependencia() == null ? null : DependenciaDto.de(i.getDependencia()), i.getSolucion(),
                i.getCalificacion(), i.getComentarioCalificacion(), i.getReaperturas(), historial, acciones);
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(reloj);
    }

    private static Categoria categoriaDe(String valor) {
        if (vacio(valor)) return null;
        try {
            return Categoria.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String textoOpcional(String texto) {
        return vacio(texto) ? null : texto.trim();
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
