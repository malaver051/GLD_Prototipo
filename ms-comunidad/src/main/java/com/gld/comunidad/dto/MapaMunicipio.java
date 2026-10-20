package com.gld.comunidad.dto;

/** Dónde abrir el mapa para el usuario: el centro de su municipio. */
public record MapaMunicipio(String municipio, String estado, double latitud, double longitud, int zoom,
                            double radioKm) {
}
