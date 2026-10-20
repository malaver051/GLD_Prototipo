package com.gld.tramites.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Recorre el BPMN del trámite sobre el modelo (patrón State), sin base de datos. */
class SolicitudTramiteTest {

    private static final DatosCiudadano ANA = new DatosCiudadano(10, "Ana María Gómez Ruiz", "ana@correo.com",
            "CURP GORA900315MJCMZN05", 14, "14039", "Guadalajara", "Jalisco");
    private static final LocalDate LIMITE = LocalDate.of(2026, 10, 22);

    private TipoTramite tramite(String costo) {
        TipoTramite t = new TipoTramite(14, "Constancia de residencia", "Descripción", "Secretaría", new BigDecimal(costo), 3);
        t.agregarRequisito("Identificación oficial", "INE", true);
        return t;
    }

    @Test
    void conCostoQuedaPendienteDePagoYSinCostoPasaARevision() {
        SolicitudTramite conCosto = SolicitudTramite.radicar("R-1", tramite("226.00"), ANA, null, LIMITE);
        assertEquals(EstadoSolicitud.PENDIENTE_PAGO, conCosto.getEstado());
        assertNull(conCosto.getFechaLimite());

        SolicitudTramite gratis = SolicitudTramite.radicar("R-2", tramite("0.00"), ANA, null, LIMITE);
        assertEquals(EstadoSolicitud.EN_REVISION, gratis.getEstado());
        assertEquals(LIMITE, gratis.getFechaLimite());
    }

    @Test
    void pagoRechazadoPermiteReintentarYConfirmadoPasaARevision() {
        SolicitudTramite s = SolicitudTramite.radicar("R-3", tramite("226.00"), ANA, null, LIMITE);
        Pago primero = s.iniciarPago("PSE-1");
        s.registrarPagoNoConfirmado(primero, "Banco de prueba");
        assertEquals(EstadoSolicitud.PENDIENTE_PAGO, s.getEstado());
        assertEquals(EstadoPago.RECHAZADO, primero.getEstado());

        Pago segundo = s.iniciarPago("PSE-2");
        s.registrarPagoConfirmado(segundo, "Banco de prueba", LIMITE);
        assertEquals(EstadoSolicitud.EN_REVISION, s.getEstado());
        assertEquals(LIMITE, s.getFechaLimite());
        assertThrows(TransicionInvalidaException.class, () -> s.iniciarPago("PSE-3"));
    }

    @Test
    void cicloDeCorreccionYAprobacion() {
        SolicitudTramite s = SolicitudTramite.radicar("R-4", tramite("0.00"), ANA, "Para la escuela", LIMITE);
        s.solicitarCorreccion("El comprobante está vencido.", 20, "Laura Méndez");
        assertEquals(EstadoSolicitud.REQUIERE_INFORMACION, s.getEstado());
        assertEquals("El comprobante está vencido.", s.informacionSolicitada().orElseThrow());

        assertThrows(TransicionInvalidaException.class, () -> s.aprobar("ok", 20, "Laura Méndez"));

        s.reenviarCorreccion("Adjunto el comprobante de octubre.", 10);
        assertEquals(EstadoSolicitud.EN_REVISION, s.getEstado());

        s.aprobar("Documentos completos.", 20, "Laura Méndez");
        assertEquals(EstadoSolicitud.APROBADA, s.getEstado());
        assertNotNull(s.getFechaResolucion());
        s.emitirDocumentoOficial("doc.pdf", "ruta/doc.pdf", 100, 20, "GLD-AAAA-BBBB", "sello");
        assertTrue(s.documentoOficial().isPresent());
        assertEquals(4, s.getHistorial().size()); // radicada, información, corrección, aprobada
    }

    @Test
    void unaSolicitudRechazadaNoAdmiteMasAcciones() {
        SolicitudTramite s = SolicitudTramite.radicar("R-5", tramite("0.00"), ANA, null, LIMITE);
        s.rechazar("El predio no pertenece al municipio.", 20, "Laura Méndez");
        assertTrue(s.getEstado().esFinal());
        assertEquals("El predio no pertenece al municipio.", s.motivoResolucion().orElseThrow());
        assertThrows(TransicionInvalidaException.class, () -> s.solicitarCorreccion("x", 20, "Laura"));
        assertThrows(TransicionInvalidaException.class,
                () -> s.emitirDocumentoOficial("d.pdf", "r", 1, 20, "C", "S"));
    }
}
