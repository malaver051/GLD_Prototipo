package com.gld.identidad.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * USUARIO del modelo relacional.
 *
 * GRASP · Experto en información: la entidad conoce sus propias reglas (si es funcionario,
 * si requiere segundo factor, si debe cambiar la clave) y las expone como métodos, en lugar de
 * que cada servicio las vuelva a calcular.
 * GRASP · Creador: los métodos de fábrica crean usuarios ya consistentes para cada tipo de cuenta.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    public static final String DOMINIO_GOBIERNO = "@gobierno.com";
    public static final String IDENTIFICADOR_ADMIN = "admin";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "identificador_autenticacion", nullable = false, unique = true)
    private String identificadorAutenticacion;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento", nullable = false)
    private String numeroDocumento;

    @Column(name = "correo", nullable = false, unique = true)
    private String correo;

    @Column(name = "telefono", nullable = false)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoUsuario estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_municipio")
    private Municipio municipio;

    @Column(name = "contrasena_hash", nullable = false)
    private String contrasenaHash;

    @Column(name = "debe_cambiar_clave", nullable = false)
    private boolean debeCambiarClave;

    @Column(name = "mfa_secreto")
    private String mfaSecreto;

    @Column(name = "mfa_activo", nullable = false)
    private boolean mfaActivo;

    @Column(name = "mfa_ultimo_paso")
    private Long mfaUltimoPaso;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    /** Relation_5 del modelo relacional (tabla usuario_rol). */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_rol",
            joinColumns = @JoinColumn(name = "usuario_id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "rol_id_rol"))
    private Set<Rol> roles = new HashSet<>();

    protected Usuario() {
    }

    // ------------------------------------------------------------------ fábricas (Creador)

    public static Usuario crearCiudadano(String nombreCompleto, TipoDocumento tipoDocumento, String numeroDocumento,
                                         String correo, String telefono, Municipio municipio,
                                         String contrasenaHash, Rol rolCiudadano) {
        Usuario u = base(correo, nombreCompleto, tipoDocumento, numeroDocumento, correo, telefono,
                municipio, contrasenaHash, rolCiudadano);
        u.estado = EstadoUsuario.PENDIENTE_MFA;
        return u;
    }

    public static Usuario crearFuncionario(String nombreCompleto, TipoDocumento tipoDocumento, String numeroDocumento,
                                           String correo, String telefono, Municipio municipio,
                                           String claveTemporalHash, Rol rol) {
        Usuario u = base(correo, nombreCompleto, tipoDocumento, numeroDocumento, correo, telefono,
                municipio, claveTemporalHash, rol);
        u.estado = EstadoUsuario.ACTIVO;
        u.debeCambiarClave = true;
        return u;
    }

    public static Usuario crearAdministrador(String contrasenaHash, Rol rolAdmin) {
        Usuario u = base(IDENTIFICADOR_ADMIN, "Administrador de la plataforma", TipoDocumento.NO_APLICA,
                "ADMIN", "admin" + DOMINIO_GOBIERNO, "+520000000000", null, contrasenaHash, rolAdmin);
        u.estado = EstadoUsuario.ACTIVO;
        return u;
    }

    private static Usuario base(String identificador, String nombreCompleto, TipoDocumento tipoDocumento,
                                String numeroDocumento, String correo, String telefono, Municipio municipio,
                                String contrasenaHash, Rol rol) {
        Usuario u = new Usuario();
        u.identificadorAutenticacion = identificador;
        u.nombreCompleto = nombreCompleto;
        u.tipoDocumento = tipoDocumento;
        u.numeroDocumento = numeroDocumento;
        u.correo = correo;
        u.telefono = telefono;
        u.municipio = municipio;
        u.contrasenaHash = contrasenaHash;
        u.fechaCreacion = LocalDateTime.now();
        u.asignarRol(rol);
        return u;
    }

    // ------------------------------------------------------------------ reglas (Experto)

    public boolean tieneRol(NombreRol nombreRol) {
        return roles.stream().anyMatch(r -> r.getNombre() == nombreRol);
    }

    public boolean esAdministrador() {
        return tieneRol(NombreRol.ADMIN);
    }

    public boolean esFuncionario() {
        return tieneRol(NombreRol.FUNCIONARIO);
    }

    public boolean esCiudadano() {
        return tieneRol(NombreRol.CIUDADANO);
    }

    /** Rol principal: en el prototipo cada usuario tiene un único rol. */
    public NombreRol getRolPrincipal() {
        if (esAdministrador()) return NombreRol.ADMIN;
        if (esFuncionario()) return NombreRol.FUNCIONARIO;
        return NombreRol.CIUDADANO;
    }

    public static boolean esCorreoDeGobierno(String correo) {
        return correo != null && correo.toLowerCase().endsWith(DOMINIO_GOBIERNO);
    }

    public boolean estaInactivo() {
        return estado == EstadoUsuario.INACTIVO;
    }

    /** Los ciudadanos siempre ingresan con segundo factor. */
    public boolean requiereMfa() {
        return esCiudadano();
    }

    public boolean tieneMfaConfigurado() {
        return mfaActivo && mfaSecreto != null;
    }

    // ------------------------------------------------------------------ comportamiento

    public void asignarRol(Rol rol) {
        roles.add(rol);
    }

    public void retirarRol(Rol rol) {
        roles.remove(rol);
    }

    /** Contraseña definida por el propio usuario: deja de ser temporal. */
    public void cambiarContrasena(String nuevoHash) {
        this.contrasenaHash = nuevoHash;
        this.debeCambiarClave = false;
    }

    /** Contraseña generada por el sistema: se debe cambiar en el siguiente ingreso. */
    public void asignarClaveTemporal(String hashTemporal) {
        this.contrasenaHash = hashTemporal;
        this.debeCambiarClave = true;
    }

    public void prepararMfa(String secreto) {
        this.mfaSecreto = secreto;
        this.mfaActivo = false;
        this.mfaUltimoPaso = null;
    }

    /** Vincula Microsoft Authenticator y activa la cuenta del ciudadano. */
    public void activarMfa(long pasoUsado) {
        this.mfaActivo = true;
        this.mfaUltimoPaso = pasoUsado;
        if (estado == EstadoUsuario.PENDIENTE_MFA) {
            this.estado = EstadoUsuario.ACTIVO;
        }
    }

    /** Un código TOTP solo puede usarse una vez: el paso debe ser posterior al último aceptado. */
    public boolean aceptaPasoMfa(long paso) {
        return mfaUltimoPaso == null || paso > mfaUltimoPaso;
    }

    public void registrarPasoMfa(long paso) {
        this.mfaUltimoPaso = paso;
    }

    public void registrarAcceso() {
        this.ultimoAcceso = LocalDateTime.now();
    }

    public void activar() {
        this.estado = tieneMfaConfigurado() || !requiereMfa() ? EstadoUsuario.ACTIVO : EstadoUsuario.PENDIENTE_MFA;
    }

    public void desactivar() {
        this.estado = EstadoUsuario.INACTIVO;
    }

    // ------------------------------------------------------------------ getters / setters

    public Integer getIdUsuario() { return idUsuario; }
    public String getIdentificadorAutenticacion() { return identificadorAutenticacion; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public EstadoUsuario getEstado() { return estado; }
    public Municipio getMunicipio() { return municipio; }
    public String getContrasenaHash() { return contrasenaHash; }
    public boolean isDebeCambiarClave() { return debeCambiarClave; }
    public String getMfaSecreto() { return mfaSecreto; }
    public boolean isMfaActivo() { return mfaActivo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public Set<Rol> getRoles() { return Set.copyOf(roles); }
}
