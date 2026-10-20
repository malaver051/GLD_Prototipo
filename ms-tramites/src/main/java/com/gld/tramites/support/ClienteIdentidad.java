package com.gld.tramites.support;

import com.gld.tramites.exception.ReglaNegocioException;
import com.gld.tramites.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Consulta el perfil del usuario en ms-identidad reenviando su propio token.
 * GRASP · Indirección: los servicios no conocen la URL ni el formato de
 * ms-identidad.
 */
@Component
public class ClienteIdentidad {

    private final RestClient identidad;

    public ClienteIdentidad(RestClient restClientIdentidad) {
        this.identidad = restClientIdentidad;
    }

    public PerfilIdentidad perfil(UsuarioAutenticado usuario) {
        try {
            PerfilIdentidad perfil = identidad.get()
                    .uri("/usuarios/me")
                    .header("Authorization", "Bearer " + usuario.token())
                    .retrieve()
                    .body(PerfilIdentidad.class);
            if (perfil == null) {
                throw new RestClientException("Respuesta vacía");
            }
            return perfil;
        } catch (RestClientException e) {
            throw new ReglaNegocioException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No fue posible consultar tus datos en el servicio de identidad. Verifica que ms-identidad esté en ejecución.");
        }
    }

    /**
     * Respuesta de GET /usuarios/me de ms-identidad (solo los campos que se usan
     * aquí).
     */
    public record PerfilIdentidad(Integer id, String nombreCompleto, String tipoDocumento, String numeroDocumento,
            String correo, String rol, Municipio municipio) {

        public String documentoLegible() {
            String tipo = switch (tipoDocumento == null ? "" : tipoDocumento) {
                case "INE" -> "credencial INE";
                case "PASAPORTE" -> "pasaporte";
                default -> tipoDocumento;
            };
            return tipo + " " + numeroDocumento;
        }
    }

    public record Municipio(Integer id, String codigo, String nombre, String estado) {
    }
}
