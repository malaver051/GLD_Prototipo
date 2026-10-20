package com.gld.comunidad.repository;

import com.gld.comunidad.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Integer> {

    List<Incidencia> findByIdCiudadanoOrderByFechaReporteDesc(Integer idCiudadano);

    List<Incidencia> findByIdMunicipioOrderByFechaReporteDesc(Integer idMunicipio);

    @Query(value = "select nextval('seguimiento_seq')", nativeQuery = true)
    long siguienteConsecutivo();
}
