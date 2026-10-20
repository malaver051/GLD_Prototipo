package com.gld.comunidad.service;

import com.gld.comunidad.config.PropiedadesGld;
import com.gld.comunidad.model.Incidencia;
import com.gld.comunidad.support.notificacion.NotificadorCorreo;
import org.springframework.stereotype.Component;

/**
 * Redacta los avisos al ciudadano en cada etapa de la incidencia.
 * GRASP · Fabricación pura / Alta cohesión: los textos no ensucian la lógica de negocio.
 */
@Component
public class NotificacionesIncidencia {

    private final NotificadorCorreo correo;
    private final String urlFrontend;

    public NotificacionesIncidencia(NotificadorCorreo correo, PropiedadesGld propiedades) {
        this.correo = correo;
        this.urlFrontend = propiedades.frontend().url();
    }

    public void reportada(Incidencia i) {
        enviar(i, "Recibimos tu reporte " + i.getNumeroSeguimiento(),
                "Registramos tu incidencia de «" + i.getCategoria().etiqueta() + "». Guarda el número de seguimiento "
                        + i.getNumeroSeguimiento() + " para consultar su estado.");
    }

    public void asignada(Incidencia i) {
        enviar(i, "Tu reporte fue asignado · " + i.getNumeroSeguimiento(),
                "Tu incidencia quedó a cargo de " + i.getDependencia().getNombre() + ".");
    }

    public void enAtencion(Incidencia i) {
        enviar(i, "Estamos atendiendo tu reporte · " + i.getNumeroSeguimiento(),
                i.getDependencia().getNombre() + " comenzó a atender tu incidencia.");
    }

    public void resuelta(Incidencia i) {
        enviar(i, "Tu reporte fue resuelto · " + i.getNumeroSeguimiento(),
                "La dependencia registró esta solución:\n«" + i.getSolucion() + "»\n"
                        + "Califica la atención de 0 a 10 desde el portal. Si el problema sigue, puedes reabrir el reporte.");
    }

    private void enviar(Incidencia i, String asunto, String mensaje) {
        correo.enviar(i.getCorreoCiudadano(), asunto,
                "Hola, " + i.getNombreCiudadano() + ".\n\n" + mensaje + "\n\n"
                        + "Consulta el detalle en: " + urlFrontend + "/portal/incidencias/" + i.getIdIncidencia());
    }
}
