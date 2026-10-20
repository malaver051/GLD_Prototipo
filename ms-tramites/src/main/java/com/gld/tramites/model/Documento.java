package com.gld.tramites.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** DOCUMENTO del modelo relacional: adjunto del ciudadano o documento oficial emitido. */
@Entity
@Table(name = "documento")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private Integer idDocumento;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoArchivo tipoDocumento;

    @Column(name = "ruta_archivo", nullable = false)
    private String rutaArchivo;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_firma", nullable = false)
    private EstadoFirma estadoFirma;

    @Column(name = "codigo_verificacion", unique = true)
    private String codigoVerificacion;

    @Column(name = "usuario_id_usuario", nullable = false)
    private Integer idUsuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_tramite_id_solicitud")
    private SolicitudTramite solicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisito_id_requisito")
    private RequisitoTramite requisito;

    @Column(name = "tipo_contenido", nullable = false)
    private String tipoContenido;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(name = "sello_sha256")
    private String selloSha256;

    protected Documento() {
    }

    private Documento(SolicitudTramite solicitud, TipoArchivo tipo, String nombreArchivo, String ruta,
                      String tipoContenido, long tamano, Integer idUsuario) {
        this.solicitud = solicitud;
        this.tipoDocumento = tipo;
        this.nombreArchivo = nombreArchivo;
        this.rutaArchivo = ruta;
        this.tipoContenido = tipoContenido;
        this.tamanoBytes = tamano;
        this.idUsuario = idUsuario;
        this.fechaCarga = LocalDateTime.now();
        this.estadoFirma = EstadoFirma.NO_APLICA;
    }

    static Documento adjunto(SolicitudTramite solicitud, RequisitoTramite requisito, String nombreArchivo,
                             String ruta, String tipoContenido, long tamano, Integer idUsuario) {
        Documento d = new Documento(solicitud, TipoArchivo.ADJUNTO, nombreArchivo, ruta, tipoContenido, tamano, idUsuario);
        d.requisito = requisito;
        return d;
    }

    static Documento oficial(SolicitudTramite solicitud, String nombreArchivo, String ruta, long tamano,
                             Integer idFuncionario, String codigoVerificacion, String selloSha256) {
        Documento d = new Documento(solicitud, TipoArchivo.OFICIAL, nombreArchivo, ruta, "application/pdf", tamano, idFuncionario);
        d.codigoVerificacion = codigoVerificacion;
        d.selloSha256 = selloSha256;
        d.estadoFirma = EstadoFirma.FIRMADO;
        return d;
    }

    public boolean esOficial() {
        return tipoDocumento == TipoArchivo.OFICIAL;
    }

    public Integer getIdDocumento() { return idDocumento; }
    public String getNombreArchivo() { return nombreArchivo; }
    public TipoArchivo getTipoDocumento() { return tipoDocumento; }
    public String getRutaArchivo() { return rutaArchivo; }
    public LocalDateTime getFechaCarga() { return fechaCarga; }
    public EstadoFirma getEstadoFirma() { return estadoFirma; }
    public String getCodigoVerificacion() { return codigoVerificacion; }
    public Integer getIdUsuario() { return idUsuario; }
    public SolicitudTramite getSolicitud() { return solicitud; }
    public RequisitoTramite getRequisito() { return requisito; }
    public String getTipoContenido() { return tipoContenido; }
    public Long getTamanoBytes() { return tamanoBytes; }
    public String getSelloSha256() { return selloSha256; }
}
