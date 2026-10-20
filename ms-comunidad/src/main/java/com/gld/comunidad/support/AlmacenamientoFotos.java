package com.gld.comunidad.support;

import com.gld.comunidad.config.PropiedadesGld;
import com.gld.comunidad.exception.ReglaNegocioException;
import com.gld.comunidad.model.FotoAdjunta;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Guarda las fotos de las incidencias en una carpeta local (simula el almacenamiento S3 de la arquitectura).
 * GRASP · Indirección / Variaciones protegidas: cambiar a S3 solo afecta a esta clase.
 */
@Component
public class AlmacenamientoFotos {

    public static final long TAMANO_MAXIMO = 5L * 1024 * 1024;
    private static final Map<String, String> TIPOS_PERMITIDOS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png");

    private final Path raiz;

    public AlmacenamientoFotos(PropiedadesGld propiedades) {
        this.raiz = Path.of(propiedades.almacenamiento().ruta()).toAbsolutePath().normalize();
    }

    public void validar(MultipartFile foto) {
        if (foto.isEmpty()) {
            throw ReglaNegocioException.invalido("La foto está vacía. Elige otra imagen.");
        }
        if (foto.getSize() > TAMANO_MAXIMO) {
            throw ReglaNegocioException.invalido("La foto supera los 5 MB.");
        }
        if (!TIPOS_PERMITIDOS.containsKey(tipoDe(foto))) {
            throw ReglaNegocioException.invalido("La foto debe ser JPG o PNG.");
        }
    }

    public FotoAdjunta guardar(Integer idIncidencia, MultipartFile foto) {
        String tipo = tipoDe(foto);
        String relativa = "incidencia-" + idIncidencia + "/" + UUID.randomUUID() + "." + TIPOS_PERMITIDOS.get(tipo);
        try {
            Path destino = resolver(relativa);
            Files.createDirectories(destino.getParent());
            foto.transferTo(destino);
        } catch (IOException e) {
            throw new UncheckedIOException("No fue posible guardar la foto", e);
        }
        String nombre = foto.getOriginalFilename() == null || foto.getOriginalFilename().isBlank()
                ? "foto." + TIPOS_PERMITIDOS.get(tipo)
                : Path.of(foto.getOriginalFilename()).getFileName().toString();
        return new FotoAdjunta(relativa, nombre, tipo, foto.getSize());
    }

    public byte[] leer(String relativa) {
        try {
            return Files.readAllBytes(resolver(relativa));
        } catch (IOException e) {
            throw ReglaNegocioException.noEncontrado("La foto ya no está disponible en el almacenamiento.");
        }
    }

    private String tipoDe(MultipartFile archivo) {
        String tipo = archivo.getContentType() == null ? "" : archivo.getContentType().toLowerCase(Locale.ROOT);
        return "image/jpg".equals(tipo) ? "image/jpeg" : tipo;
    }

    /** Evita rutas fuera de la carpeta de almacenamiento. */
    private Path resolver(String relativa) {
        Path ruta = raiz.resolve(relativa).normalize();
        if (!ruta.startsWith(raiz)) {
            throw ReglaNegocioException.invalido("Ruta de archivo inválida.");
        }
        return ruta;
    }
}
