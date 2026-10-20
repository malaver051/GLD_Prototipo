package com.gld.identidad.repository;

import com.gld.identidad.model.EstadoSolicitud;
import com.gld.identidad.model.SolicitudRestablecimiento;
import com.gld.identidad.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SolicitudRestablecimientoRepository extends JpaRepository<SolicitudRestablecimiento, Integer> {

    boolean existsByUsuarioAndEstado(Usuario usuario, EstadoSolicitud estado);

    List<SolicitudRestablecimiento> findByUsuarioAndEstado(Usuario usuario, EstadoSolicitud estado);

    @Query("""
            select s from SolicitudRestablecimiento s
              join fetch s.usuario u
              left join fetch u.municipio
             where s.estado = :estado
             order by s.fechaSolicitud asc
            """)
    List<SolicitudRestablecimiento> listarPorEstado(@Param("estado") EstadoSolicitud estado);

    long countByEstado(EstadoSolicitud estado);
}
