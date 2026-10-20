package com.gld.tramites.repository;

import com.gld.tramites.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentoRepository extends JpaRepository<Documento, Integer> {

    Optional<Documento> findByCodigoVerificacion(String codigo);
}
