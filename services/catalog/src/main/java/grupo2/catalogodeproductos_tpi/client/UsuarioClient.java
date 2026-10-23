package grupo2.catalogodeproductos_tpi.client;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Cliente HTTP para comunicarse con el Módulo 1 (Usuarios y Autenticación).
 * Se encarga de validar los tokens X-API-Key.
 */
@Component
public class UsuarioClient {

    /**
     * Lo que se sabe de una X-API-Key después de consultar a Usuarios.
     */
    public enum ResultadoValidacion {
        /** Usuarios respondió 2xx. */
        VALIDA,
        /** Usuarios respondió 401. */
        INVALIDA,
        /** Usuarios respondió 403: la key es válida pero no tiene permiso. */
        SIN_PERMISO,
        /** Cualquier otra cosa: otro código, timeout, conexión cortada o rechazada. */
        NO_DISPONIBLE
    }


    @Autowired
    private RestTemplate restTemplate;

    // Lee la URL desde application.properties
    @Value("${api.usuarios.url}")
    private String USUARIO_API_URL;

    private static final Logger log = LoggerFactory.getLogger(UsuarioClient.class);




    /**
     * Valida un token X-API-Key contra el Módulo 1 (Usuarios y Autenticación).
     *
     * Una key inválida o sin permiso no es una falla de Usuarios, así que no se
     * loguea como WARN. Que Usuarios no esté disponible sí.
     *
     * @param apiKey El token (String) leído desde el header de la petición.
     * @return VALIDA (2xx), INVALIDA (401), SIN_PERMISO (403) o NO_DISPONIBLE (cualquier otro caso).
     */
    public ResultadoValidacion validarToken(String apiKey){

        //Headers de la petición
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey);
        headers.set("Content-Type", "application/json");

        Map<String,String> body = new HashMap<>();
        body.put("servicio","catalogo");

        HttpEntity<Map<String,String>> request = new HttpEntity<>(body, headers);

        try{

            ResponseEntity  <String> response = restTemplate.postForEntity(USUARIO_API_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return ResultadoValidacion.VALIDA;
            }

            // El RestTemplate no lanza excepción para un 3xx.
            log.warn("API Usuarios: No se pudo validar el token. Respondió {}", response.getStatusCode().value());

            return ResultadoValidacion.NO_DISPONIBLE;


        } catch(HttpClientErrorException e){

            if (e.getStatusCode().isSameCodeAs(HttpStatus.UNAUTHORIZED)) {
                log.info("API Usuarios: rechazó la X-API-Key (401)");
                return ResultadoValidacion.INVALIDA;
            }

            if (e.getStatusCode().isSameCodeAs(HttpStatus.FORBIDDEN)) {
                log.info("API Usuarios: la X-API-Key no tiene permiso (403)");
                return ResultadoValidacion.SIN_PERMISO;
            }

            return noDisponible(e);

        } catch(Exception e){

            return noDisponible(e);

        }

    }

    /**
     * Loguea la falla con la clase de la excepción, para distinguir un timeout
     * (ResourceAccessException) de un error de Usuarios (HttpServerErrorException).
     */
    private ResultadoValidacion noDisponible(Exception e) {

        log.warn("API Usuarios: No se pudo validar el token. Causa: {}: {}", e.getClass().getSimpleName(), e.getMessage());

        return ResultadoValidacion.NO_DISPONIBLE;

    }

}
