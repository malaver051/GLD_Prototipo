package com.gld.tramites.support;

import com.gld.tramites.config.PropiedadesGld;
import com.gld.tramites.exception.ReglaNegocioException;
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
 * Guarda adjuntos y documentos oficiales en una carpeta local (simula el almacenamiento de objetos
 * de la arquitectura). En la base de datos solo se guarda la ruta relativa.
 * GRASP · Indirección / Variaciones protegidas: cambiar a S3 solo afecta a esta clase.
 */
@Component
public class AlmacenamientoArchivos {

    public static final long TAMANO_MAXIMO = 5L * 1024 * 1024;
    private static final Map<String, String> TIPOS_PERMITIDOS = Map.of(
            "application/pdf", "pdf",
            "image/jpeg", "jpg",
            "image/png", "png");

    private final Path raiz;

    public AlmacenamientoArchivos(PropiedadesGld propiedades) {
        this.raiz = Path.of(propiedades.almacenamiento().ruta()).toAbsolutePath().normalize();
    }

    /** Valida tipo y tamaño antes de guardar. */
    public void validar(MultipartFile archivo, String requisito) {
        if (archivo.isEmpty()) {
            throw ReglaNegocioException.invalido("El archivo de «" + requisito + "» está vacío.");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw ReglaNegocioException.invalido("El archivo de «" + requisito + "» supera los 5 MB.");
        }
        if (!TIPOS_PERMITIDOS.containsKey(tipoDe(archivo))) {
            throw ReglaNegocioException.invalido("El archivo de «" + requisito + "» debe ser PDF, JPG o PNG.");
        }
    }

    public String tipoDe(MultipartFile archivo) {
        String tipo = archivo.getContentType() == null ? "" : archivo.getContentType().toLowerCase(Locale.ROOT);
        return "image/jpg".equals(tipo) ? "image/jpeg" : tipo;
    }

    /** Guarda un adjunto y devuelve la ruta relativa. */
    public String guardar(Integer idSolicitud, MultipartFile archivo) {
        String extension = TIPOS_PERMITIDOS.get(tipoDe(archivo));
        String relativa = "solicitud-" + idSolicitud + "/" + UUID.randomUUID() + "." + extension;
        try {
            Path destino = resolver(relativa);
            Files.createDirectories(destino.getParent());
            archivo.transferTo(destino);
            return relativa;
        } catch (IOException e) {
            throw new UncheckedIOException("No fue posible guardar el archivo", e);
        }
    }

    public String guardar(Integer idSolicitud, String nombre, byte[] contenido) {
        String relativa = "solicitud-" + idSolicitud + "/" + nombre;
        try {
            Path destino = resolver(relativa);
            Files.createDirectories(destino.getParent());
            Files.write(destino, contenido);
            return relativa;
        } catch (IOException e) {
            throw new UncheckedIOException("No fue posible guardar el documento", e);
        }
    }

    public byte[] leer(String relativa) {
        try {
            return Files.readAllBytes(resolver(relativa));
        } catch (IOException e) {
            throw ReglaNegocioException.noEncontrado("El archivo ya no está disponible en el almacenamiento.");
        }
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
