package com.gld.identidad.service;

import com.gld.identidad.dto.PasoIngreso;
import com.gld.identidad.dto.RespuestaIngreso;
import com.gld.identidad.dto.SolicitudRegistro;
import com.gld.identidad.dto.UsuarioResumen;
import com.gld.identidad.exception.ReglaNegocioException;
import com.gld.identidad.model.Municipio;
import com.gld.identidad.model.NombreRol;
import com.gld.identidad.model.Rol;
import com.gld.identidad.model.Usuario;
import com.gld.identidad.repository.MunicipioRepository;
import com.gld.identidad.repository.RolRepository;
import com.gld.identidad.repository.UsuarioRepository;
import com.gld.identidad.security.JwtService;
import com.gld.identidad.security.PropositoToken;
import com.gld.identidad.support.GeneradorTotp;
import com.gld.identidad.support.PoliticaContrasena;
import com.gld.identidad.support.ValidadorDocumento;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro público de ciudadanos. La cuenta queda PENDIENTE_MFA hasta vincular Authenticator.
 */
@Service
@Transactional
public class RegistroCiudadanoService {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final MunicipioRepository municipios;
    private final PasswordEncoder codificador;
    private final PoliticaContrasena politica;
    private final ValidadorDocumento validador;
    private final JwtService jwt;
    private final GeneradorTotp totp;

    public RegistroCiudadanoService(UsuarioRepository usuarios, RolRepository roles, MunicipioRepository municipios,
                                    PasswordEncoder codificador, PoliticaContrasena politica,
                                    ValidadorDocumento validador, JwtService jwt, GeneradorTotp totp) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.municipios = municipios;
        this.codificador = codificador;
        this.politica = politica;
        this.validador = validador;
        this.jwt = jwt;
        this.totp = totp;
    }

    public RespuestaIngreso registrar(SolicitudRegistro datos) {
        String correo = datos.correo().trim().toLowerCase();
        if (Usuario.esCorreoDeGobierno(correo)) {
            throw ReglaNegocioException.invalido(
                    "Los correos @gobierno.com son exclusivos para funcionarios. Usa tu correo personal.");
        }
        if (usuarios.existsByCorreoIgnoreCase(correo)) {
            throw ReglaNegocioException.conflicto("Ya existe una cuenta con este correo. Ingresa o recupera tu contraseña.");
        }
        String documento = validador.normalizar(datos.tipoDocumento(), datos.numeroDocumento());
        if (usuarios.existsByTipoDocumentoAndNumeroDocumentoIgnoreCase(datos.tipoDocumento(), documento)) {
            throw ReglaNegocioException.conflicto("Ya existe una cuenta con este documento.");
        }
        Municipio municipio = municipios.findById(datos.idMunicipio())
                .filter(Municipio::estaActivo)
                .orElseThrow(() -> ReglaNegocioException.invalido("El municipio elegido no está disponible."));
        politica.exigir(datos.contrasena(), datos.confirmacion());

        Rol rolCiudadano = roles.findByNombre(NombreRol.CIUDADANO)
                .orElseThrow(() -> new IllegalStateException("No existe el rol CIUDADANO."));

        Usuario ciudadano = Usuario.crearCiudadano(
                datos.nombreCompleto().trim(),
                datos.tipoDocumento(),
                documento,
                correo,
                validador.normalizarTelefono(datos.telefono()),
                municipio,
                codificador.encode(datos.contrasena()),
                rolCiudadano);
        // El secreto se genera aquí para que el QR sea siempre el mismo hasta vincular el celular
        ciudadano.prepararMfa(totp.generarSecreto());
        usuarios.save(ciudadano);

        return new RespuestaIngreso(PasoIngreso.CONFIGURAR_MFA,
                jwt.emitir(ciudadano, PropositoToken.MFA_CONFIGURACION),
                UsuarioResumen.de(ciudadano));
    }
}
