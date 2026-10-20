package com.gld.tramites.service;

import com.gld.tramites.config.PropiedadesGld;
import com.gld.tramites.model.SolicitudTramite;
import com.gld.tramites.support.notificacion.NotificadorCorreo;
import org.springframework.stereotype.Component;

/**
 * Redacta los avisos al ciudadano en cada etapa (BPMN: «Notificar resultado»).
 * GRASP · Fabricación pura / Alta cohesión: los textos no ensucian la lógica de negocio.
 */
@Component
public class NotificacionesTramite {

    private final NotificadorCorreo correo;
    private final String urlFrontend;

    public NotificacionesTramite(NotificadorCorreo correo, PropiedadesGld propiedades) {
        this.correo = correo;
        this.urlFrontend = propiedades.frontend().url();
    }

    public void radicada(SolicitudTramite s) {
        String siguiente = s.getTipoTramite().requierePago()
                ? "Para que el municipio la revise, realiza el pago de $" + s.getCosto().toPlainString() + " MXN desde el portal."
                : "El trámite no tiene costo y ya está en revisión.";
        enviar(s, "Radicamos tu solicitud " + s.getNumeroRadicado(),
                "Recibimos tu solicitud de «" + s.getTipoTramite().getNombre() + "».\n" + siguiente);
    }

    public void pagoConfirmado(SolicitudTramite s, String referencia) {
        enviar(s, "Pago confirmado · " + s.getNumeroRadicado(),
                "Confirmamos tu pago (referencia " + referencia + "). Tu solicitud está en revisión y la respuesta"
                        + " llegará a más tardar el " + s.getFechaLimite() + ".");
    }

    public void pagoNoConfirmado(SolicitudTramite s, String referencia) {
        enviar(s, "Tu pago no se completó · " + s.getNumeroRadicado(),
                "El banco no confirmó el pago de la referencia " + referencia + ". Puedes intentarlo de nuevo desde el portal.");
    }

    public void informacionSolicitada(SolicitudTramite s, String motivo) {
        enviar(s, "Necesitamos información adicional · " + s.getNumeroRadicado(),
                "El funcionario que revisa tu solicitud escribió:\n«" + motivo + "»\nResponde desde el portal para continuar.");
    }

    public void aprobada(SolicitudTramite s, String codigoVerificacion) {
        enviar(s, "Tu solicitud fue aprobada · " + s.getNumeroRadicado(),
                "Tu trámite «" + s.getTipoTramite().getNombre() + "» fue aprobado. Descarga el documento oficial desde el portal.\n"
                        + "Código de verificación: " + codigoVerificacion);
    }

    public void rechazada(SolicitudTramite s, String motivo) {
        enviar(s, "Tu solicitud fue rechazada · " + s.getNumeroRadicado(),
                "Tu trámite «" + s.getTipoTramite().getNombre() + "» fue rechazado por el siguiente motivo:\n«" + motivo + "»");
    }

    private void enviar(SolicitudTramite s, String asunto, String mensaje) {
        correo.enviar(s.getCorreoCiudadano(), asunto,
                "Hola, " + s.getNombreCiudadano() + ".\n\n" + mensaje + "\n\n"
                        + "Consulta el detalle en: " + urlFrontend + "/portal/solicitudes/" + s.getIdSolicitud());
    }
}
