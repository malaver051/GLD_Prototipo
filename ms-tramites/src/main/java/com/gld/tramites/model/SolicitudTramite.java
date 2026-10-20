package com.gld.tramites.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * SOLICITUD_TRAMITE del modelo relacional.
 *
 * GRASP · Experto en información: la solicitud conoce su estado, a quién pertenece y qué documentos
 * y pagos tiene. GRASP · Creador: crea su propio historial, sus pagos y sus documentos.
 * Patrón State: delega en EstadoSolicitud qué transición es válida.
 */
@Entity
@Table(name = "solicitud_tramite")
public class SolicitudTramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Integer idSolicitud;

    @Column(name = "numero_radicado", nullable = false, unique = true)
    private String numeroRadicado;

    @Column(name = "fecha_radicacion", nullable = false)
    private LocalDateTime fechaRadicacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoSolicitud estado;

    @Column(name = "observaciones")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_tramite_id_tramite")
    private TipoTramite tipoTramite;

    @Column(name = "usuario_id_usuario", nullable = false)
    private Integer idCiudadano;

    @Column(name = "municipio_id_municipio", nullable = false)
    private Integer idMunicipio;

    @Column(name = "municipio_nombre", nullable = false)
    private String municipioNombre;

    @Column(name = "municipio_estado", nullable = false)
    private String municipioEstado;

    @Column(name = "nombre_ciudadano", nullable = false)
    private String nombreCiudadano;

    @Column(name = "correo_ciudadano", nullable = false)
    private String correoCiudadano;

    @Column(name = "documento_ciudadano", nullable = false)
    private String documentoCiudadano;

    @Column(name = "costo", nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL)
    private List<Historial> historial = new ArrayList<>();

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL)
    private List<Documento> documentos = new ArrayList<>();

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL)
    private List<Pago> pagos = new ArrayList<>();

    protected SolicitudTramite() {
    }

    // ------------------------------------------------------------------ radicación

    /**
     * Radica la solicitud. Si el trámite no tiene costo pasa directo a revisión y el plazo empieza a correr;
     * si tiene costo, queda pendiente de pago (BPMN: «¿Tiene costo?»).
     */
    public static SolicitudTramite radicar(String numeroRadicado, TipoTramite tipo, DatosCiudadano ciudadano,
                                           String observaciones, LocalDate fechaLimiteSiNoTieneCosto) {
        SolicitudTramite s = new SolicitudTramite();
        s.numeroRadicado = numeroRadicado;
        s.fechaRadicacion = LocalDateTime.now();
        s.tipoTramite = tipo;
        s.observaciones = observaciones;
        s.costo = tipo.getCosto();
        s.idCiudadano = ciudadano.idUsuario();
        s.nombreCiudadano = ciudadano.nombre();
        s.correoCiudadano = ciudadano.correo();
        s.documentoCiudadano = ciudadano.documento();
        s.idMunicipio = ciudadano.idMunicipio();
        s.municipioNombre = ciudadano.municipioNombre();
        s.municipioEstado = ciudadano.municipioEstado();
        s.estado = EstadoSolicitud.inicial(tipo.requierePago());
        if (s.estado == EstadoSolicitud.EN_REVISION) {
            s.fechaLimite = fechaLimiteSiNoTieneCosto;
        }
        String motivo = tipo.requierePago()
                ? "Solicitud radicada. Pendiente el pago de $" + s.costo.toPlainString() + " MXN."
                : "Solicitud radicada. El trámite no tiene costo y pasa a revisión.";
        s.registrar(null, s.estado, motivo, OrigenCambio.CIUDADANO, ciudadano.idUsuario(), ciudadano.nombre());
        return s;
    }

    public Documento adjuntar(RequisitoTramite requisito, String nombreArchivo, String ruta, String tipoContenido,
                              long tamano, Integer idUsuario) {
        Documento d = Documento.adjunto(this, requisito, nombreArchivo, ruta, tipoContenido, tamano, idUsuario);
        documentos.add(d);
        return d;
    }

    // ------------------------------------------------------------------ pago

    /** Crea una orden de pago; si había otra pendiente, la cancela. */
    public Pago iniciarPago(String referencia) {
        if (!estado.admitePago()) {
            throw new TransicionInvalidaException("Esta solicitud no tiene pagos pendientes.");
        }
        pagos.forEach(Pago::cancelar);
        Pago pago = new Pago(this, referencia, costo);
        pagos.add(pago);
        return pago;
    }

    /** BPMN: «¿Confirmado? → Sí». La solicitud pasa a revisión y empieza a correr el plazo. */
    public void registrarPagoConfirmado(Pago pago, String banco, LocalDate fechaLimite) {
        pago.confirmar(banco);
        EstadoSolicitud anterior = estado;
        estado = estado.confirmarPago();
        this.fechaLimite = fechaLimite;
        registrar(anterior, estado, "Pago confirmado por " + banco + ". Referencia " + pago.getReferenciaOrden() + ".",
                OrigenCambio.PASARELA, null, "Pasarela de pagos");
    }

    /** BPMN: «¿Confirmado? → No». La solicitud sigue pendiente y el ciudadano puede reintentar. */
    public void registrarPagoNoConfirmado(Pago pago, String banco) {
        pago.rechazar(banco);
        registrar(estado, estado, "El banco " + banco + " no confirmó el pago de la referencia "
                + pago.getReferenciaOrden() + ". Puedes intentarlo de nuevo.", OrigenCambio.PASARELA, null, "Pasarela de pagos");
    }

    // ------------------------------------------------------------------ revisión

    /** BPMN: «¿Información adicional? → Sí». */
    public void solicitarCorreccion(String motivo, Integer idFuncionario, String nombreFuncionario) {
        EstadoSolicitud anterior = estado;
        estado = estado.solicitarInformacion();
        registrar(anterior, estado, motivo, OrigenCambio.FUNCIONARIO, idFuncionario, nombreFuncionario);
    }

    /** El ciudadano responde con la información o los documentos que se le pidieron. */
    public void reenviarCorreccion(String comentario, Integer idCiudadano) {
        EstadoSolicitud anterior = estado;
        estado = estado.reenviarCorreccion();
        registrar(anterior, estado, comentario, OrigenCambio.CIUDADANO, idCiudadano, nombreCiudadano);
    }

    /** BPMN: «¿Aprueba? → Sí». El documento oficial se agrega con emitirDocumentoOficial. */
    public void aprobar(String comentario, Integer idFuncionario, String nombreFuncionario) {
        EstadoSolicitud anterior = estado;
        estado = estado.aprobar();
        fechaResolucion = LocalDateTime.now();
        registrar(anterior, estado, comentario, OrigenCambio.FUNCIONARIO, idFuncionario, nombreFuncionario);
    }

    /** BPMN: «¿Aprueba? → No»: se registra el rechazo y su motivo. */
    public void rechazar(String motivo, Integer idFuncionario, String nombreFuncionario) {
        EstadoSolicitud anterior = estado;
        estado = estado.rechazar();
        fechaResolucion = LocalDateTime.now();
        registrar(anterior, estado, motivo, OrigenCambio.FUNCIONARIO, idFuncionario, nombreFuncionario);
    }

    public Documento emitirDocumentoOficial(String nombreArchivo, String ruta, long tamano, Integer idFuncionario,
                                           String codigoVerificacion, String sello) {
        if (estado != EstadoSolicitud.APROBADA) {
            throw new TransicionInvalidaException("Solo se emite el documento oficial de una solicitud aprobada.");
        }
        Documento d = Documento.oficial(this, nombreArchivo, ruta, tamano, idFuncionario, codigoVerificacion, sello);
        documentos.add(d);
        return d;
    }

    private void registrar(EstadoSolicitud anterior, EstadoSolicitud nuevo, String motivo, OrigenCambio origen,
                           Integer idUsuario, String nombreUsuario) {
        historial.add(new Historial(this, anterior, nuevo, motivo, origen, idUsuario, nombreUsuario));
    }

    // ------------------------------------------------------------------ consultas (Experto)

    public boolean perteneceACiudadano(Integer idUsuario) {
        return idCiudadano.equals(idUsuario);
    }

    public boolean perteneceAMunicipio(Integer municipio) {
        return idMunicipio.equals(municipio);
    }

    public boolean estaVencida(LocalDate hoy) {
        return fechaLimite != null && !estado.esFinal() && hoy.isAfter(fechaLimite);
    }

    public Optional<Pago> pagoPendiente() {
        return pagos.stream().filter(Pago::estaPendiente).reduce((a, b) -> b);
    }

    public Optional<Pago> pagoAprobado() {
        return pagos.stream().filter(p -> p.getEstado() == EstadoPago.APROBADO).findFirst();
    }

    public Optional<Documento> documentoOficial() {
        return documentos.stream().filter(Documento::esOficial).findFirst();
    }

    /** Último mensaje con el que el funcionario pidió información. */
    public Optional<String> informacionSolicitada() {
        if (estado != EstadoSolicitud.REQUIERE_INFORMACION) {
            return Optional.empty();
        }
        return getHistorial().stream()
                .filter(h -> h.getEstadoNuevo() == EstadoSolicitud.REQUIERE_INFORMACION)
                .reduce((a, b) -> b)
                .map(Historial::getMotivo);
    }

    /** Motivo del rechazo o comentario de la aprobación. */
    public Optional<String> motivoResolucion() {
        if (!estado.esFinal()) {
            return Optional.empty();
        }
        return getHistorial().stream().reduce((a, b) -> b).map(Historial::getMotivo);
    }

    public Integer getIdSolicitud() { return idSolicitud; }
    public String getNumeroRadicado() { return numeroRadicado; }
    public LocalDateTime getFechaRadicacion() { return fechaRadicacion; }
    public EstadoSolicitud getEstado() { return estado; }
    public String getObservaciones() { return observaciones; }
    public TipoTramite getTipoTramite() { return tipoTramite; }
    public Integer getIdCiudadano() { return idCiudadano; }
    public Integer getIdMunicipio() { return idMunicipio; }
    public String getMunicipioNombre() { return municipioNombre; }
    public String getMunicipioEstado() { return municipioEstado; }
    public String getNombreCiudadano() { return nombreCiudadano; }
    public String getCorreoCiudadano() { return correoCiudadano; }
    public String getDocumentoCiudadano() { return documentoCiudadano; }
    public BigDecimal getCosto() { return costo; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public LocalDateTime getFechaResolucion() { return fechaResolucion; }

    public List<Historial> getHistorial() {
        return historial.stream()
                .sorted(Comparator.comparing(Historial::getFechaCambio)
                        .thenComparing(h -> h.getIdHistorial() == null ? Integer.MAX_VALUE : h.getIdHistorial()))
                .toList();
    }

    public List<Documento> getDocumentos() {
        return documentos.stream().sorted(Comparator.comparing(Documento::getFechaCarga)).toList();
    }

    public List<Pago> getPagos() {
        return pagos.stream().sorted(Comparator.comparing(Pago::getFechaCreacion)).toList();
    }
}
