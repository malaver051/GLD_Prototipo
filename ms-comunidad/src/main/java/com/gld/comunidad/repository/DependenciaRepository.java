package com.gld.comunidad.repository;

import com.gld.comunidad.model.Dependencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DependenciaRepository extends JpaRepository<Dependencia, Integer> {

    List<Dependencia> findAllByOrderByNombreAsc();
}
