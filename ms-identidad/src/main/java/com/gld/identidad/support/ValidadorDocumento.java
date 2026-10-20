package com.gld.identidad.support;

import com.gld.identidad.exception.ReglaNegocioException;
import com.gld.identidad.model.TipoDocumento;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normaliza y valida documentos de identidad mexicanos y teléfonos.
 * GRASP · Fabricación pura: la usan el registro ciudadano y el alta de funcionarios.
 */
@Component
public class ValidadorDocumento {

    /** CURP: 4 letras, fecha AAMMDD, sexo (H, M o X), 5 letras, homoclave y dígito verificador. */
    private static final Pattern CURP = Pattern.compile("^[A-Z]{4}\\d{6}[HMX][A-Z]{5}[A-Z0-9]\\d$");
    /** INE: clave de elector (18) u otros identificadores de la credencial (9 a 18 caracteres). */
    private static final Pattern INE = Pattern.compile("^[A-Z0-9]{9,18}$");
    private static final Pattern PASAPORTE = Pattern.compile("^[A-Z0-9]{6,12}$");

    public String normalizar(TipoDocumento tipo, String numero) {
        String valor = numero == null ? "" : numero.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
        Pattern patron = switch (tipo) {
            case CURP -> CURP;
            case INE -> INE;
            case PASAPORTE -> PASAPORTE;
            case NO_APLICA -> throw ReglaNegocioException.invalido("Elige un tipo de documento válido.");
        };
        if (!patron.matcher(valor).matches()) {
            throw ReglaNegocioException.invalido(switch (tipo) {
                case CURP -> "La CURP debe tener 18 caracteres con el formato oficial, por ejemplo GODE561231HDFRRN09.";
                case INE -> "El número de la credencial INE debe tener entre 9 y 18 letras o números.";
                default -> "El número de pasaporte debe tener entre 6 y 12 letras o números.";
            });
        }
        return valor;
    }

    /** Guarda el teléfono con la lada de México: 10 dígitos → +52XXXXXXXXXX. */
    public String normalizarTelefono(String telefono) {
        return "+52" + telefono.replaceAll("\\D", "");
    }
}
