package grupo2.catalogodeproductos_tpi.client;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Cliente HTTP para comunicarse con el Módulo 1 (Usuarios y Autenticación).
 * Se encarga de validar los tokens X-API-Key.
 */
@Component
public class UsuarioClient {


    @Autowired
    private RestTemplate restTemplate;

    // Lee la URL desde application.properties
    @Value("${api.usuarios.url}")
    private String USUARIO_API_URL;




    /**
     * Valida un token X-API-Key contra el Módulo 1 (Usuarios y Autenticación).
     *
     * @param apiKey El token (String) leído desde el header de la petición.
     * @return true si el token es válido (Respuesta 200 OK), false en cualquier otro caso (401, 403, 500, o error de conexión).
     */
    public boolean validarToken(String apiKey){

        //Headers de la petición
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey);
        headers.set("Content-Type", "application/json");

        Map<String,String> body = new HashMap<>();
        body.put("servicio","catalogo");

        HttpEntity<Map<String,String>> request = new HttpEntity<>(body, headers);

        try{

            ResponseEntity  <String> response = restTemplate.postForEntity(USUARIO_API_URL, request, String.class);

            return response.getStatusCode().is2xxSuccessful();


        } catch(Exception e){

            return false;

        }

    }

}
