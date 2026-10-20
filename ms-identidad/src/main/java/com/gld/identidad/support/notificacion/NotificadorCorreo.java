package com.gld.identidad.support.notificacion;

/**
 * Envío de correos.
 * GRASP · Polimorfismo / Variaciones protegidas: hoy se usa la implementación de consola;
 * mañana puede añadirse una con SMTP o Amazon SES sin tocar los servicios.
 */
public interface NotificadorCorreo {

    void enviar(String destinatario, String asunto, String cuerpo);
}
