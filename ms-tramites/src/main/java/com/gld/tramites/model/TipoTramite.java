package com.gld.tramites.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** TIPO_TRAMITE del modelo relacional. Si idMunicipio es null, es la plantilla del catálogo. */
@Entity
@Table(name = "tipo_tramite")
public class TipoTramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tramite")
    private Integer idTramite;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "costo", nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;

    @Column(name = "plazo_dias", nullable = false)
    private Integer plazoDias;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoTipoTramite estado;

    @Column(name = "municipio_id_municipio")
    private Integer idMunicipio;

    @Column(name = "dependencia", nullable = false)
    private String dependencia;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "tipoTramite", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<RequisitoTramite> requisitos = new ArrayList<>();

    protected TipoTramite() {
    }

    public TipoTramite(Integer idMunicipio, String nombre, String descripcion, String dependencia,
                       BigDecimal costo, int plazoDias) {
        this.idMunicipio = idMunicipio;
        this.estado = EstadoTipoTramite.HABILITADO;
        actualizar(nombre, descripcion, dependencia, costo, plazoDias);
    }

    /** Copia este tipo (de la plantilla) para un municipio. GRASP · Creador. */
    public TipoTramite copiarPara(Integer municipio) {
        TipoTramite copia = new TipoTramite(municipio, nombre, descripcion, dependencia, costo, plazoDias);
        requisitos.forEach(r -> copia.agregarRequisito(r.getNombre(), r.getDescripcion(), r.isObligatorio()));
        return copia;
    }

    public void actualizar(String nombre, String descripcion, String dependencia, BigDecimal costo, int plazoDias) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.dependencia = dependencia;
        this.costo = costo;
        this.plazoDias = plazoDias;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public RequisitoTramite agregarRequisito(String nombre, String descripcion, boolean obligatorio) {
        RequisitoTramite r = new RequisitoTramite(this, nombre, descripcion, obligatorio, requisitos.size() + 1);
        requisitos.add(r);
        return r;
    }

    /**
     * Sincroniza los requisitos con los que envía el funcionario: actualiza los que traen id,
     * crea los nuevos y elimina los que ya no están (orphanRemoval).
     */
    public void sincronizarRequisitos(List<DatosRequisito> datos) {
        Map<Integer, RequisitoTramite> actuales = new HashMap<>();
        requisitos.forEach(r -> actuales.put(r.getIdRequisito(), r));
        List<RequisitoTramite> resultado = new ArrayList<>();
        int orden = 1;
        for (DatosRequisito d : datos) {
            RequisitoTramite r = d.id() == null ? null : actuales.get(d.id());
            if (r == null) {
                r = new RequisitoTramite(this, d.nombre(), d.descripcion(), d.obligatorio(), orden);
            } else {
                r.actualizar(d.nombre(), d.descripcion(), d.obligatorio());
                r.ordenar(orden);
            }
            resultado.add(r);
            orden++;
        }
        requisitos.clear();
        requisitos.addAll(resultado);
        this.fechaActualizacion = LocalDateTime.now();
    }

    // ---------- Experto en información (métodos del diagrama de clases)

    public boolean requierePago() {
        return costo.signum() > 0;
    }

    public boolean estaHabilitado() {
        return estado == EstadoTipoTramite.HABILITADO;
    }

    public boolean perteneceA(Integer municipio) {
        return idMunicipio != null && idMunicipio.equals(municipio);
    }

    public void habilitar() {
        this.estado = EstadoTipoTramite.HABILITADO;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void deshabilitar() {
        this.estado = EstadoTipoTramite.DESHABILITADO;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public List<RequisitoTramite> getRequisitosObligatorios() {
        return requisitos.stream().filter(RequisitoTramite::isObligatorio).toList();
    }

    public Integer getIdTramite() { return idTramite; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getCosto() { return costo; }
    public Integer getPlazoDias() { return plazoDias; }
    public EstadoTipoTramite getEstado() { return estado; }
    public Integer getIdMunicipio() { return idMunicipio; }
    public String getDependencia() { return dependencia; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public List<RequisitoTramite> getRequisitos() {
        return requisitos.stream().sorted(Comparator.comparing(RequisitoTramite::getOrden)).toList();
    }
}
