package com.gld.comunidad.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * INCIDENCIA del modelo relacional: un reporte ciudadano georreferenciado.
 *
 * GRASP · Experto en información: la incidencia conoce su estado, su dueño y su municipio,
 * así que decide si una acción procede y registra su propio historial.
 * GRASP · Creador: crea sus registros de {@link HistorialIncidencia}.
 */
@Entity
@Table(name = "incidencia")
public class Incidencia {

    public static final int MAXIMO_REAPERTURAS = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incidencia")
    private Integer idIncidencia;

    @Column(name = "numero_seguimiento", nullable = false, unique = true)
    private String numeroSeguimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private Categoria categoria;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "latitud", nullable = false)
    private BigDecimal latitud;

    @Column(name = "longitud", nullable = false)
    private BigDecimal longitud;

    @Column(name = "referencia")
    private String referencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoIncidencia estado;

    @Column(name = "fecha_reporte", nullable = false)
    private LocalDateTime fechaReporte;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    // --- Ciudadano y municipio (copiados de ms-identidad)
    @Column(name = "usuario_id_usuario", nullable = false)
    private Integer idCiudadano;

    @Column(name = "municipio_id_municipio", nullable = false)
    private Integer idMunicipio;

    @Column(name = "codigo_municipio", nullable = false)
    private String codigoMunicipio;

    @Column(name = "municipio_nombre", nullable = false)
    private String municipioNombre;

    @Column(name = "municipio_estado", nullable = false)
    private String municipioEstado;

    @Column(name = "nombre_ciudadano", nullable = false)
    private String nombreCiudadano;

    @Column(name = "correo_ciudadano", nullable = false)
    private String correoCiudadano;

    // --- Foto opcional
    @Column(name = "foto_ruta")
    private String fotoRuta;

    @Column(name = "foto_nombre")
    private String fotoNombre;

    @Column(name = "foto_tipo")
    private String fotoTipo;

    @Column(name = "foto_tamano_bytes")
    private Long fotoTamanoBytes;

    // --- Atención
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dependencia_id_dependencia")
    private Dependencia dependencia;

    @Column(name = "solucion")
    private String solucion;

    @Column(name = "reaperturas", nullable = false)
    private int reaperturas;

    // --- Calificación (HU-12)
    @Column(name = "calificacion")
    private Integer calificacion;

    @Column(name = "comentario_calificacion")
    private String comentarioCalificacion;

    @OneToMany(mappedBy = "incidencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaCambio ASC, idHistorial ASC")
    private List<HistorialIncidencia> historial = new ArrayList<>();

    protected Incidencia() {
    }

    /** Crea la incidencia en estado REPORTADA con su primer registro de historial. */
    public static Incidencia reportar(String numeroSeguimiento, Categoria categoria, String descripcion,
                                      String referencia, Ubicacion ubicacion, DatosCiudadano ciudadano,
                                      LocalDateTime ahora) {
        Incidencia i = new Incidencia();
        i.numeroSeguimiento = numeroSeguimiento;
        i.categoria = categoria;
        i.descripcion = descripcion;
        i.referencia = referencia;
        i.latitud = coordenada(ubicacion.latitud());
        i.longitud = coordenada(ubicacion.longitud());
        i.estado = EstadoIncidencia.REPORTADA;
        i.fechaReporte = ahora;
        i.fechaActualizacion = ahora;
        i.idCiudadano = ciudadano.idUsuario();
        i.nombreCiudadano = ciudadano.nombre();
        i.correoCiudadano = ciudadano.correo();
        i.idMunicipio = ciudadano.idMunicipio();
        i.codigoMunicipio = ciudadano.codigoMunicipio();
        i.municipioNombre = ciudadano.municipioNombre();
        i.municipioEstado = ciudadano.municipioEstado();
        i.registrar(null, "Incidencia reportada. Número de seguimiento " + numeroSeguimiento + ".",
                OrigenCambio.CIUDADANO, ciudadano.idUsuario(), ciudadano.nombre(), ahora);
        return i;
    }

    public void adjuntarFoto(FotoAdjunta foto) {
        this.fotoRuta = foto.ruta();
        this.fotoNombre = foto.nombre();
        this.fotoTipo = foto.tipo();
        this.fotoTamanoBytes = foto.tamanoBytes();
    }

    // ------------------------------------------------------------------ funcionario

    /** Asigna o cambia la dependencia responsable (HU-10). */
    public void asignar(Dependencia nueva, String comentario, Integer idFuncionario, String nombreFuncionario,
                        LocalDateTime ahora) {
        if (dependencia != null && dependencia.getIdDependencia().equals(nueva.getIdDependencia())) {
            throw new TransicionInvalidaException("La incidencia ya está asignada a " + nueva.getNombre() + ".");
        }
        EstadoIncidencia anterior = estado;
        estado = estado.asignar();
        String texto = (dependencia == null ? "Asignada a " : "Reasignada de " + dependencia.getNombre() + " a ")
                + nueva.getNombre() + "." + (vacio(comentario) ? "" : " " + comentario.trim());
        dependencia = nueva;
        registrar(anterior, texto, OrigenCambio.FUNCIONARIO, idFuncionario, nombreFuncionario, ahora);
    }

    public void iniciarAtencion(String comentario, Integer idFuncionario, String nombreFuncionario,
                                LocalDateTime ahora) {
        EstadoIncidencia anterior = estado;
        estado = estado.iniciarAtencion();
        registrar(anterior, vacio(comentario) ? dependencia.getNombre() + " inició la atención." : comentario.trim(),
                OrigenCambio.FUNCIONARIO, idFuncionario, nombreFuncionario, ahora);
    }

    /** Marca la incidencia como resuelta; la solución es obligatoria (criterio de HU-11). */
    public void resolver(String solucion, Integer idFuncionario, String nombreFuncionario, LocalDateTime ahora) {
        if (vacio(solucion)) {
            throw new DatoInvalidoException("Registra la solución para marcar la incidencia como resuelta.");
        }
        EstadoIncidencia anterior = estado;
        estado = estado.resolver();
        this.solucion = solucion.trim();
        this.fechaResolucion = ahora;
        registrar(anterior, this.solucion, OrigenCambio.FUNCIONARIO, idFuncionario, nombreFuncionario, ahora);
    }

    // ------------------------------------------------------------------ ciudadano

    /** El ciudadano califica la atención de 0 a 10 y la incidencia queda cerrada (HU-12). */
    public void calificar(int puntaje, String comentario, LocalDateTime ahora) {
        if (puntaje < 0 || puntaje > 10) {
            throw new DatoInvalidoException("La calificación debe estar entre 0 y 10.");
        }
        EstadoIncidencia anterior = estado;
        estado = estado.calificar();
        this.calificacion = puntaje;
        this.comentarioCalificacion = vacio(comentario) ? null : comentario.trim();
        this.fechaCierre = ahora;
        registrar(anterior, "El ciudadano calificó la atención con " + puntaje + " de 10."
                        + (this.comentarioCalificacion == null ? "" : " «" + this.comentarioCalificacion + "»"),
                OrigenCambio.CIUDADANO, idCiudadano, nombreCiudadano, ahora);
    }

    /** El ciudadano no está conforme con la solución: vuelve a la dependencia (una sola vez). */
    public void reabrir(String motivo, LocalDateTime ahora) {
        if (!puedeReabrirse()) {
            throw new TransicionInvalidaException(
                    "Esta incidencia ya se reabrió una vez. Califica la atención para cerrarla.");
        }
        if (vacio(motivo)) {
            throw new DatoInvalidoException("Cuéntanos por qué el problema no quedó resuelto.");
        }
        EstadoIncidencia anterior = estado;
        estado = estado.reabrir();
        reaperturas++;
        this.solucion = null;
        this.fechaResolucion = null;
        registrar(anterior, "Reabierta por el ciudadano: " + motivo.trim(), OrigenCambio.CIUDADANO,
                idCiudadano, nombreCiudadano, ahora);
    }

    // ------------------------------------------------------------------ consultas

    public boolean perteneceACiudadano(Integer idUsuario) {
        return idCiudadano.equals(idUsuario);
    }

    public boolean perteneceAMunicipio(Integer municipio) {
        return idMunicipio.equals(municipio);
    }

    public boolean puedeReabrirse() {
        return estado == EstadoIncidencia.RESUELTA && reaperturas < MAXIMO_REAPERTURAS;
    }

    public boolean tieneFoto() {
        return fotoRuta != null;
    }

    public Ubicacion ubicacion() {
        return new Ubicacion(latitud.doubleValue(), longitud.doubleValue());
    }

    private void registrar(EstadoIncidencia anterior, String comentario, OrigenCambio origen, Integer idUsuario,
                           String nombreUsuario, LocalDateTime ahora) {
        historial.add(new HistorialIncidencia(this, anterior, estado, comentario, origen, idUsuario, nombreUsuario, ahora));
        fechaActualizacion = ahora;
    }

    private static BigDecimal coordenada(double valor) {
        return BigDecimal.valueOf(valor).setScale(6, RoundingMode.HALF_UP);
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    // ------------------------------------------------------------------ getters

    public Integer getIdIncidencia() { return idIncidencia; }
    public String getNumeroSeguimiento() { return numeroSeguimiento; }
    public Categoria getCategoria() { return categoria; }
    public String getDescripcion() { return descripcion; }
    public String getReferencia() { return referencia; }
    public EstadoIncidencia getEstado() { return estado; }
    public LocalDateTime getFechaReporte() { return fechaReporte; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public Integer getIdCiudadano() { return idCiudadano; }
    public Integer getIdMunicipio() { return idMunicipio; }
    public String getCodigoMunicipio() { return codigoMunicipio; }
    public String getMunicipioNombre() { return municipioNombre; }
    public String getMunicipioEstado() { return municipioEstado; }
    public String getNombreCiudadano() { return nombreCiudadano; }
    public String getCorreoCiudadano() { return correoCiudadano; }
    public String getFotoRuta() { return fotoRuta; }
    public String getFotoNombre() { return fotoNombre; }
    public String getFotoTipo() { return fotoTipo; }
    public Long getFotoTamanoBytes() { return fotoTamanoBytes; }
    public Dependencia getDependencia() { return dependencia; }
    public String getSolucion() { return solucion; }
    public int getReaperturas() { return reaperturas; }
    public Integer getCalificacion() { return calificacion; }
    public String getComentarioCalificacion() { return comentarioCalificacion; }
    public List<HistorialIncidencia> getHistorial() { return Collections.unmodifiableList(historial); }
}
