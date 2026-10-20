package com.gld.tramites.service;

import com.gld.tramites.dto.OrdenPago;
import com.gld.tramites.dto.ResultadoPago;
import com.gld.tramites.exception.ReglaNegocioException;
import com.gld.tramites.model.Pago;
import com.gld.tramites.model.SolicitudTramite;
import com.gld.tramites.repository.PagoRepository;
import com.gld.tramites.security.UsuarioAutenticado;
import com.gld.tramites.support.CalendarioHabil;
import com.gld.tramites.support.Codigos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pagos con la pasarela simulada (BPMN: «Crear orden de pago», «Resultado del pago», «¿Confirmado?»).
 */
@Service
@Transactional
public class PagoService {

    private final PagoRepository pagos;
    private final GestorTramitesService gestor;
    private final CalendarioHabil calendario;
    private final NotificacionesTramite notificaciones;

    public PagoService(PagoRepository pagos, GestorTramitesService gestor, CalendarioHabil calendario,
                       NotificacionesTramite notificaciones) {
        this.pagos = pagos;
        this.gestor = gestor;
        this.calendario = calendario;
        this.notificaciones = notificaciones;
    }

    /** Método iniciarPago del diagrama de clases: crea la orden y devuelve los datos para la pasarela. */
    public OrdenPago iniciarPago(UsuarioAutenticado ciudadano, Integer idSolicitud) {
        SolicitudTramite s = gestor.solicitudDelCiudadano(ciudadano, idSolicitud);
        Pago pago = s.iniciarPago(Codigos.referenciaPago());
        return OrdenPago.de(pagos.saveAndFlush(pago));
    }

    @Transactional(readOnly = true)
    public OrdenPago consultarOrden(UsuarioAutenticado ciudadano, String referencia) {
        return OrdenPago.de(pagoDelCiudadano(ciudadano, referencia));
    }

    /** Método registrarResultadoPago del diagrama de clases. */
    public OrdenPago registrarResultadoPago(UsuarioAutenticado ciudadano, String referencia, ResultadoPago resultado) {
        Pago pago = pagoDelCiudadano(ciudadano, referencia);
        SolicitudTramite s = pago.getSolicitud();
        if (resultado.aprobado()) {
            s.registrarPagoConfirmado(pago, resultado.banco(),
                    calendario.fechaLimiteDesdeHoy(s.getTipoTramite().getPlazoDias()));
            notificaciones.pagoConfirmado(s, referencia);
        } else {
            s.registrarPagoNoConfirmado(pago, resultado.banco());
            notificaciones.pagoNoConfirmado(s, referencia);
        }
        return OrdenPago.de(pago);
    }

    private Pago pagoDelCiudadano(UsuarioAutenticado ciudadano, String referencia) {
        return pagos.findByReferenciaOrden(referencia)
                .filter(p -> p.getSolicitud().perteneceACiudadano(ciudadano.idUsuario()))
                .orElseThrow(() -> ReglaNegocioException.noEncontrado("La orden de pago no existe."));
    }
}
