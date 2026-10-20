package com.gld.identidad.repository;

import com.gld.identidad.model.EstadoMunicipio;
import com.gld.identidad.model.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MunicipioRepository extends JpaRepository<Municipio, Integer> {

    List<Municipio> findByEstadoOrderByDepartamentoAscNombreAsc(EstadoMunicipio estado);
}
