package com.gld.comunidad.model;

/** Foto guardada en el almacenamiento de archivos. */
public record FotoAdjunta(String ruta, String nombre, String tipo, long tamanoBytes) {
}
