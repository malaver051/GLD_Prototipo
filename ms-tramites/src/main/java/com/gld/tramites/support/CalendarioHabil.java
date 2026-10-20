package com.gld.tramites.support;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Calcula fechas límite en días hábiles (lunes a viernes), como se expresan los plazos de los trámites.
 * No considera días festivos: es una simplificación del prototipo.
 */
@Component
public class CalendarioHabil {

    private final Clock reloj;

    public CalendarioHabil(Clock reloj) {
        this.reloj = reloj;
    }

    public LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    public LocalDate sumarDiasHabiles(LocalDate desde, int dias) {
        LocalDate fecha = desde;
        int contados = 0;
        while (contados < dias) {
            fecha = fecha.plusDays(1);
            if (fecha.getDayOfWeek() != DayOfWeek.SATURDAY && fecha.getDayOfWeek() != DayOfWeek.SUNDAY) {
                contados++;
            }
        }
        return fecha;
    }

    public LocalDate fechaLimiteDesdeHoy(int dias) {
        return sumarDiasHabiles(hoy(), dias);
    }
}
