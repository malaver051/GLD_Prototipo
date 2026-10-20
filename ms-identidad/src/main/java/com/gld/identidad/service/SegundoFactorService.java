package com.gld.identidad.service;

import com.gld.identidad.config.PropiedadesGld;
import com.gld.identidad.dto.ConfiguracionMfa;
import com.gld.identidad.dto.RespuestaIngreso;
import com.gld.identidad.exception.ReglaNegocioException;
import com.gld.identidad.model.Usuario;
import com.gld.identidad.support.GeneradorTotp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.OptionalLong;

/**
 * Segundo factor de los ciudadanos con Microsoft Authenticator (TOTP).
 * GRASP · Alta cohesión: todo lo relativo al segundo factor vive en este servicio.
 */
@Service
@Transactional
public class SegundoFactorService {

    private static final String CODIGO_INCORRECTO =
            "El código no es correcto. Usa el código que aparece ahora en Microsoft Authenticator.";

    private final AutenticacionService autenticacion;
    private final GeneradorTotp totp;
    private final String emisor;

    public SegundoFactorService(AutenticacionService autenticacion, GeneradorTotp totp, PropiedadesGld propiedades) {
        this.autenticacion = autenticacion;
        this.totp = totp;
        this.emisor = propiedades.mfa().emisor();
    }

    /** Datos del QR. Si el ciudadano aún no tiene secreto, se genera en este momento. */
    public ConfiguracionMfa obtenerConfiguracion(Integer idUsuario) {
        Usuario usuario = ciudadanoSinMfa(idUsuario);
        if (usuario.getMfaSecreto() == null) {
            usuario.prepararMfa(totp.generarSecreto());
        }
        String secreto = usuario.getMfaSecreto();
        return new ConfiguracionMfa(
                totp.uriOtpauth(emisor, usuario.getCorreo(), secreto),
                agruparDeACuatro(secreto),
                usuario.getCorreo(),
                emisor);
    }

    /** Primer código correcto: queda vinculado el celular y la cuenta pasa a ACTIVO. */
    public RespuestaIngreso activar(Integer idUsuario, String codigo) {
        Usuario usuario = ciudadanoSinMfa(idUsuario);
        long paso = verificarCodigo(usuario, codigo);
        usuario.activarMfa(paso);
        return autenticacion.concederAcceso(usuario);
    }

    /** Ingreso habitual: contraseña correcta + código vigente. */
    public RespuestaIngreso verificar(Integer idUsuario, String codigo) {
        Usuario usuario = autenticacion.buscarActivo(idUsuario);
        if (!usuario.tieneMfaConfigurado()) {
            throw ReglaNegocioException.conflicto("Primero debes vincular Microsoft Authenticator.");
        }
        long paso = verificarCodigo(usuario, codigo);
        if (!usuario.aceptaPasoMfa(paso)) {
            throw ReglaNegocioException.invalido("Ese código ya se usó. Espera a que Authenticator muestre uno nuevo.");
        }
        usuario.registrarPasoMfa(paso);
        return autenticacion.concederAcceso(usuario);
    }

    private long verificarCodigo(Usuario usuario, String codigo) {
        OptionalLong paso = totp.verificar(usuario.getMfaSecreto(), codigo);
        if (paso.isEmpty()) {
            throw ReglaNegocioException.invalido(CODIGO_INCORRECTO);
        }
        return paso.getAsLong();
    }

    private Usuario ciudadanoSinMfa(Integer idUsuario) {
        Usuario usuario = autenticacion.buscarActivo(idUsuario);
        if (!usuario.requiereMfa()) {
            throw ReglaNegocioException.prohibido("Esta cuenta no usa segundo factor.");
        }
        if (usuario.tieneMfaConfigurado()) {
            throw ReglaNegocioException.conflicto("Microsoft Authenticator ya está vinculado a esta cuenta.");
        }
        return usuario;
    }

    private static String agruparDeACuatro(String secreto) {
        return secreto.replaceAll("(.{4})(?!$)", "$1 ");
    }
}
