package com.gld.identidad.repository;

import com.gld.identidad.model.TokenRecuperacion;
import com.gld.identidad.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Integer> {

    Optional<TokenRecuperacion> findByTokenHash(String tokenHash);

    /** Invalida los enlaces anteriores cuando se pide uno nuevo. */
    @Modifying
    @Query("update TokenRecuperacion t set t.usado = true where t.usuario = :usuario and t.usado = false")
    int invalidarVigentes(@Param("usuario") Usuario usuario);
}
