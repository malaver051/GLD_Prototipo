package com.gld.tramites.support;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalendarioHabilTest {

    private final CalendarioHabil calendario = new CalendarioHabil(Clock.systemDefaultZone());

    @Test
    void saltaLosFinesDeSemana() {
        LocalDate jueves = LocalDate.of(2026, 10, 15);
        assertEquals(LocalDate.of(2026, 10, 20), calendario.sumarDiasHabiles(jueves, 3)); // vie, lun, mar
        LocalDate viernes = LocalDate.of(2026, 10, 16);
        assertEquals(LocalDate.of(2026, 10, 19), calendario.sumarDiasHabiles(viernes, 1));
    }
}
