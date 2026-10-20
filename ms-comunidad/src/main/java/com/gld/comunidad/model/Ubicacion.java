package com.gld.comunidad.model;

/**
 * Punto geográfico (WGS84) de una incidencia.
 * GRASP · Experto en información: sabe calcular su distancia a otro punto.
 */
public record Ubicacion(double latitud, double longitud) {

    private static final double RADIO_TIERRA_KM = 6371.0;

    public Ubicacion {
        if (latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
            throw new DatoInvalidoException("La ubicación marcada no es válida.");
        }
    }

    /** Distancia en kilómetros (fórmula del haversine). */
    public double distanciaKm(Ubicacion otra) {
        double dLat = Math.toRadians(otra.latitud - latitud);
        double dLon = Math.toRadians(otra.longitud - longitud);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitud)) * Math.cos(Math.toRadians(otra.latitud))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * RADIO_TIERRA_KM * Math.asin(Math.sqrt(a));
    }
}
