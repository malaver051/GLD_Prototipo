package com.gld.tramites.repository;

import com.gld.tramites.model.EstadoSolicitud;
import com.gld.tramites.model.SolicitudTramite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SolicitudTramiteRepository extends JpaRepository<SolicitudTramite, Integer> {

    @Query("""
            select s from SolicitudTramite s join fetch s.tipoTramite
             where s.idCiudadano = :idCiudadano
             order by s.fechaRadicacion desc
            """)
    List<SolicitudTramite> delCiudadano(@Param("idCiudadano") Integer idCiudadano);

    @Query("""
            select s from SolicitudTramite s join fetch s.tipoTramite
             where s.idMunicipio = :idMunicipio and s.estado in :estados
             order by s.fechaRadicacion asc
            """)
    List<SolicitudTramite> delMunicipio(@Param("idMunicipio") Integer idMunicipio,
                                        @Param("estados") Collection<EstadoSolicitud> estados);

    @Query(value = "select nextval('radicado_seq')", nativeQuery = true)
    long siguienteRadicado();
}
