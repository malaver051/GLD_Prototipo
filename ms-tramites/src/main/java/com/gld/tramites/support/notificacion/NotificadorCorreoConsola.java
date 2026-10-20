package com.gld.tramites.support.notificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Correo simulado: el mensaje se imprime en la consola del backend. */
@Component
public class NotificadorCorreoConsola implements NotificadorCorreo {

    private static final Logger log = LoggerFactory.getLogger(NotificadorCorreoConsola.class);
    private static final String LINEA = "=".repeat(68);

    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) {
        log.info("""

                {}
                 CORREO SIMULADO
                 Para:   {}
                 Asunto: {}
                {}
                {}
                {}
                """, LINEA, destinatario, asunto, "-".repeat(68), cuerpo, LINEA);
    }
}
