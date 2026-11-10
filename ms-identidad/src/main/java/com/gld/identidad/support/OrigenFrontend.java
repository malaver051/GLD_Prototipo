package com.gld.identidad.support;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.regex.Pattern;

/**
 * Dirección del frontend para los enlaces que se envían al ciudadano (correos y documento oficial).
 * El proxy de Vite envía en cada petición el encabezado X-Gld-Frontend con la dirección desde la que
 * se abrió la aplicación (http://localhost:5173 en local o https://...-5173.app.github.dev en Codespaces).
 * Si la petición no lo trae, se usa gld.frontend.url.
 */
public final class OrigenFrontend {

    public static final String ENCABEZADO = "X-Gld-Frontend";
    private static final Pattern ORIGEN_VALIDO = Pattern.compile("https?://[A-Za-z0-9.-]+(:[0-9]{1,5})?");

    private OrigenFrontend() {
    }

    public static String resolver(String porDefecto) {
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes();
        if (atributos instanceof ServletRequestAttributes servlet) {
            String origen = servlet.getRequest().getHeader(ENCABEZADO);
            if (origen != null && ORIGEN_VALIDO.matcher(origen).matches()) {
                return origen;
            }
        }
        return porDefecto;
    }
}
