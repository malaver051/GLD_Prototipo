package com.gld.identidad.repository;

import com.gld.identidad.model.AmbitoRol;
import com.gld.identidad.model.NombreRol;
import com.gld.identidad.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombre(NombreRol nombre);

    List<Rol> findByAmbito(AmbitoRol ambito);
}
