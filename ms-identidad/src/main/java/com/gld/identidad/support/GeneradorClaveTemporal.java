package com.gld.identidad.support;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Genera contraseñas temporales que cumplen la política (12 caracteres).
 * Se omiten caracteres que se confunden al leerlos: 0/O, 1/l/I.
 * GRASP · Fabricación pura.
 */
@Component
public class GeneradorClaveTemporal {

    private static final String MAYUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String MINUSCULAS = "abcdefghijkmnopqrstuvwxyz";
    private static final String NUMEROS = "23456789";
    private static final String SIMBOLOS = "!#$%&*?@";
    private static final String TODOS = MAYUSCULAS + MINUSCULAS + NUMEROS + SIMBOLOS;
    private static final int LONGITUD = 12;

    private final SecureRandom aleatorio = new SecureRandom();

    public String generar() {
        List<Character> caracteres = new ArrayList<>();
        caracteres.add(tomar(MAYUSCULAS));
        caracteres.add(tomar(MINUSCULAS));
        caracteres.add(tomar(NUMEROS));
        caracteres.add(tomar(SIMBOLOS));
        while (caracteres.size() < LONGITUD) {
            caracteres.add(tomar(TODOS));
        }
        Collections.shuffle(caracteres, aleatorio);
        StringBuilder sb = new StringBuilder(LONGITUD);
        caracteres.forEach(sb::append);
        return sb.toString();
    }

    private char tomar(String conjunto) {
        return conjunto.charAt(aleatorio.nextInt(conjunto.length()));
    }
}
