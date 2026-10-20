package com.gld.tramites.repository;

import com.gld.tramites.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    Optional<Pago> findByReferenciaOrden(String referenciaOrden);
}
