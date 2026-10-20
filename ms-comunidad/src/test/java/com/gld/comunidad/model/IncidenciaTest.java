package com.gld.comunidad.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Recorre el ciclo de la épica GLD-E2 sobre el modelo (patrón State), sin base de datos. */
class IncidenciaTest {

    private static final DatosCiudadano ANA = new DatosCiudadano(10, "Ana María Gómez Ruiz", "ana@correo.com",
            14, "14039", "Guadalajara", "Jalisco");
    private static final Ubicacion PUNTO = new Ubicacion(20.6767, -103.3475);
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 19, 9, 0);
    private static final Dependencia OBRAS = new Dependencia(1, "Obras Públicas", "Baches", Categoria.VIAS);
    private static final Dependencia ALUMBRADO = new Dependencia(2, "Alumbrado Público", "Luminarias", Categoria.ALUMBRADO);

    private Incidencia nueva() {
        return Incidencia.reportar("INC-14039-2026-000001", Categoria.VIAS, "Bache profundo", "Frente a la escuela",
                PUNTO, ANA, AHORA);
    }

    @Test
    void cicloCompletoHastaCerrarConCalificacion() {
        Incidencia i = nueva();
        assertEquals(EstadoIncidencia.REPORTADA, i.getEstado());

        i.asignar(OBRAS, null, 5, "Sofía Ramos", AHORA.plusHours(1));
        assertEquals(EstadoIncidencia.ASIGNADA, i.getEstado());

        i.iniciarAtencion(null, 5, "Sofía Ramos", AHORA.plusHours(2));
        assertEquals(EstadoIncidencia.EN_ATENCION, i.getEstado());

        i.resolver("Se rellenó el bache con asfalto.", 5, "Sofía Ramos", AHORA.plusDays(1));
        assertEquals(EstadoIncidencia.RESUELTA, i.getEstado());
        assertNotNull(i.getFechaResolucion());

        i.calificar(9, "Muy rápido", AHORA.plusDays(2));
        assertEquals(EstadoIncidencia.CERRADA, i.getEstado());
        assertEquals(Integer.valueOf(9), i.getCalificacion());
        assertEquals(5, i.getHistorial().size());
    }

    @Test
    void resolverSinSolucionNoSePermite() {
        Incidencia i = nueva();
        i.asignar(OBRAS, null, 5, "Sofía", AHORA);
        i.iniciarAtencion(null, 5, "Sofía", AHORA);
        assertThrows(DatoInvalidoException.class, () -> i.resolver("  ", 5, "Sofía", AHORA));
        assertEquals(EstadoIncidencia.EN_ATENCION, i.getEstado());
    }

    @Test
    void transicionesInvalidasLanzanExcepcion() {
        Incidencia i = nueva();
        assertThrows(TransicionInvalidaException.class, () -> i.iniciarAtencion(null, 5, "Sofía", AHORA));
        assertThrows(TransicionInvalidaException.class, () -> i.resolver("Listo", 5, "Sofía", AHORA));
        assertThrows(TransicionInvalidaException.class, () -> i.calificar(8, null, AHORA));
    }

    @Test
    void reasignarDuranteLaAtencionVuelveAAsignada() {
        Incidencia i = nueva();
        i.asignar(OBRAS, null, 5, "Sofía", AHORA);
        i.iniciarAtencion(null, 5, "Sofía", AHORA);
        i.asignar(ALUMBRADO, "Es una luminaria", 5, "Sofía", AHORA);
        assertEquals(EstadoIncidencia.ASIGNADA, i.getEstado());
        assertEquals("Alumbrado Público", i.getDependencia().getNombre());
        assertThrows(TransicionInvalidaException.class, () -> i.asignar(ALUMBRADO, null, 5, "Sofía", AHORA));
    }

    @Test
    void seReabreUnaSolaVez() {
        Incidencia i = nueva();
        i.asignar(OBRAS, null, 5, "Sofía", AHORA);
        i.iniciarAtencion(null, 5, "Sofía", AHORA);
        i.resolver("Bache tapado", 5, "Sofía", AHORA);

        i.reabrir("El bache sigue abierto", AHORA);
        assertEquals(EstadoIncidencia.ASIGNADA, i.getEstado());
        assertNull(i.getSolucion());

        i.iniciarAtencion(null, 5, "Sofía", AHORA);
        i.resolver("Se repavimentó el tramo", 5, "Sofía", AHORA);
        assertFalse(i.puedeReabrirse());
        assertThrows(TransicionInvalidaException.class, () -> i.reabrir("Sigue igual", AHORA));
        i.calificar(6, null, AHORA);
        assertEquals(EstadoIncidencia.CERRADA, i.getEstado());
    }

    @Test
    void calificacionFueraDeRangoSeRechaza() {
        Incidencia i = nueva();
        i.asignar(OBRAS, null, 5, "Sofía", AHORA);
        i.iniciarAtencion(null, 5, "Sofía", AHORA);
        i.resolver("Listo", 5, "Sofía", AHORA);
        assertThrows(DatoInvalidoException.class, () -> i.calificar(11, null, AHORA));
        assertEquals(EstadoIncidencia.RESUELTA, i.getEstado());
    }

    @Test
    void distanciaEntreGuadalajaraYZapopan() {
        double km = new Ubicacion(20.6767, -103.3475).distanciaKm(new Ubicacion(20.7214, -103.3915));
        assertTrue(km > 6 && km < 7, "distancia " + km);
    }
}
