package com.gld.identidad.config;

import com.gld.identidad.model.NombreRol;
import com.gld.identidad.model.Rol;
import com.gld.identidad.model.Usuario;
import com.gld.identidad.repository.RolRepository;
import com.gld.identidad.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea la cuenta "admin" / "admin" al arrancar si no existe.
 * Es la única cuenta con rol ADMIN y su contraseña no cambia.
 */
@Component
public class InicializadorAdministrador implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InicializadorAdministrador.class);
    private static final String CLAVE_ADMIN = "admin";

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder codificador;

    public InicializadorAdministrador(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder codificador) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.codificador = codificador;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.existsByIdentificadorAutenticacionIgnoreCase(Usuario.IDENTIFICADOR_ADMIN)) {
            return;
        }
        Rol rolAdmin = roles.findByNombre(NombreRol.ADMIN)
                .orElseThrow(() -> new IllegalStateException("No existe el rol ADMIN; revisa las migraciones de Flyway."));
        usuarios.save(Usuario.crearAdministrador(codificador.encode(CLAVE_ADMIN), rolAdmin));
        log.info("Cuenta de administrador creada (usuario: admin).");
    }
}
