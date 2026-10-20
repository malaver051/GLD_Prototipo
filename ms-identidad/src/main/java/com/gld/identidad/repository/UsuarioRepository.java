package com.gld.identidad.repository;

import com.gld.identidad.model.NombreRol;
import com.gld.identidad.model.TipoDocumento;
import com.gld.identidad.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** GRASP · Indirección: aísla a los servicios del acceso a la base de datos. */
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByIdentificadorAutenticacionIgnoreCase(String identificador);

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByIdentificadorAutenticacionIgnoreCase(String identificador);

    boolean existsByTipoDocumentoAndNumeroDocumentoIgnoreCase(TipoDocumento tipo, String numero);

    /** Busca por rol; "texto" vacío devuelve todos (filtra por nombre o correo). */
    @Query("""
            select distinct u from Usuario u
              join u.roles r
              left join fetch u.municipio
             where r.nombre = :rol
               and (lower(u.nombreCompleto) like lower(concat('%', :texto, '%'))
                    or lower(u.correo) like lower(concat('%', :texto, '%')))
             order by u.fechaCreacion desc
            """)
    List<Usuario> buscarPorRol(@Param("rol") NombreRol rol, @Param("texto") String texto);
}
