package com.gld.comunidad.service;

import com.gld.comunidad.config.PropiedadesGld;
import com.gld.comunidad.dto.MapaMunicipio;
import com.gld.comunidad.exception.ReglaNegocioException;
import com.gld.comunidad.model.Ubicacion;
import com.gld.comunidad.repository.CentroMunicipioRepository;
import com.gld.comunidad.support.ClienteIdentidad.Municipio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

/**
 * Ubicación de los municipios en el mapa y validación de que un reporte está dentro del municipio.
 * GRASP · Alta cohesión: todo lo geográfico vive aquí.
 */
@Service
public class MapaService {

    /** Centro aproximado de México, para municipios sin coordenadas registradas. */
    private static final Ubicacion CENTRO_MEXICO = new Ubicacion(23.6345, -102.5528);
    private static final int ZOOM_MUNICIPIO = 13;
    private static final int ZOOM_PAIS = 5;

    private final CentroMunicipioRepository centros;
    private final double radioKm;

    public MapaService(CentroMunicipioRepository centros, PropiedadesGld propiedades) {
        this.centros = centros;
        this.radioKm = propiedades.incidencias() == null ? 40 : propiedades.incidencias().radioKm();
    }

    @Transactional(readOnly = true)
    public MapaMunicipio mapaDe(Municipio municipio) {
        Optional<Ubicacion> centro = centroDe(municipio.codigo());
        Ubicacion punto = centro.orElse(CENTRO_MEXICO);
        return new MapaMunicipio(municipio.nombre(), municipio.estado(), punto.latitud(), punto.longitud(),
                centro.isPresent() ? ZOOM_MUNICIPIO : ZOOM_PAIS, radioKm);
    }

    /** Rechaza ubicaciones lejanas al municipio del ciudadano (probablemente marcadas por error). */
    @Transactional(readOnly = true)
    public void validarDentroDelMunicipio(Ubicacion ubicacion, Municipio municipio) {
        centroDe(municipio.codigo()).ifPresent(centro -> {
            double distancia = ubicacion.distanciaKm(centro);
            if (distancia > radioKm) {
                throw ReglaNegocioException.invalido(String.format(Locale.ROOT,
                        "La ubicación marcada está a %.0f km de %s. Marca un punto dentro de tu municipio.",
                        distancia, municipio.nombre()));
            }
        });
    }

    private Optional<Ubicacion> centroDe(String codigoMunicipio) {
        return codigoMunicipio == null ? Optional.empty()
                : centros.findById(codigoMunicipio).map(c -> c.ubicacion());
    }
}
