package com.gld.identidad.support;

import com.gld.identidad.exception.ReglaNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Regla de contraseñas del proyecto: mínimo 8 caracteres, al menos una mayúscula,
 * un número y un símbolo. El máximo de 72 es un límite técnico de BCrypt.
 *
 * GRASP · Fabricación pura: la regla no pertenece a ninguna entidad del dominio y se
 * reutiliza en registro, cambio inicial y recuperación.
 */
@Component
public class PoliticaContrasena {

    public static final int LONGITUD_MINIMA = 8;
    public static final int LONGITUD_MAXIMA = 72;

    /** Devuelve los requisitos que no se cumplen; lista vacía si la contraseña es válida. */
    public List<String> incumplimientos(String contrasena) {
        List<String> faltantes = new ArrayList<>();
        String valor = contrasena == null ? "" : contrasena;
        if (valor.length() < LONGITUD_MINIMA) {
            faltantes.add("Debe tener al menos " + LONGITUD_MINIMA + " caracteres.");
        }
        if (valor.length() > LONGITUD_MAXIMA) {
            faltantes.add("Debe tener como máximo " + LONGITUD_MAXIMA + " caracteres.");
        }
        if (valor.codePoints().noneMatch(Character::isUpperCase)) {
            faltantes.add("Debe incluir al menos una letra mayúscula.");
        }
        if (valor.codePoints().noneMatch(Character::isDigit)) {
            faltantes.add("Debe incluir al menos un número.");
        }
        if (valor.codePoints().noneMatch(PoliticaContrasena::esSimbolo)) {
            faltantes.add("Debe incluir al menos un símbolo, por ejemplo ! # $ % & * ?");
        }
        return faltantes;
    }

    public boolean esValida(String contrasena) {
        return incumplimientos(contrasena).isEmpty();
    }

    /** Valida una contraseña nueva y su confirmación; lanza un error con los requisitos faltantes. */
    public void exigir(String nueva, String confirmacion) {
        List<String> faltantes = incumplimientos(nueva);
        if (!faltantes.isEmpty()) {
            throw new ReglaNegocioException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "La contraseña no cumple los requisitos.", faltantes);
        }
        if (!nueva.equals(confirmacion)) {
            throw ReglaNegocioException.invalido("Las contraseñas no coinciden.");
        }
    }

    static boolean esSimbolo(int c) {
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
    }
}
