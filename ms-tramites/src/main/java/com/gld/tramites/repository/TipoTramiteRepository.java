package com.gld.tramites.repository;

import com.gld.tramites.model.EstadoTipoTramite;
import com.gld.tramites.model.TipoTramite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TipoTramiteRepository extends JpaRepository<TipoTramite, Integer> {

    List<TipoTramite> findByIdMunicipioOrderByNombreAsc(Integer idMunicipio);

    List<TipoTramite> findByIdMunicipioAndEstadoOrderByNombreAsc(Integer idMunicipio, EstadoTipoTramite estado);

    boolean existsByIdMunicipio(Integer idMunicipio);

    /** Tipos de la plantilla (sin municipio). */
    @Query("select t from TipoTramite t where t.idMunicipio is null order by t.idTramite")
    List<TipoTramite> plantilla();
}
