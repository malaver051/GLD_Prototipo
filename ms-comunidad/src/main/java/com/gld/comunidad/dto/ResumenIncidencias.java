package com.gld.comunidad.dto;

import java.util.Map;

/** Cifras del municipio para la cabecera del mapa del funcionario. */
public record ResumenIncidencias(Map<String, Long> porEstado, long abiertas, Double promedioCalificacion,
                                 long calificadas) {
}
