package grupo2.catalogodeproductos_tpi.client;

import grupo2.catalogodeproductos_tpi.dto.RatingDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

/**
 * Cliente HTTP para comunicarse con el Módulo 7 (Reseñas y Reputación).
 * Se encarga de obtener el rating promedio de los productos.
 */
@Component
public class ResenasClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.resenas.url}")
    private String RESENAS_API_URL;

    private static final Logger log = LoggerFactory.getLogger(ResenasClient.class);

    /**
     * Llama al Módulo 7 para obtener el rating promedio y la cantidad de reseñas de un SKU.
     *
     * @param sku El SKU (String) del producto a consultar.
     * @return un Optional<RatingDTO>.
     * - Si tiene éxito, devuelve Optional.of(RatingDTO).
     * - Si falla, devuelve Optional.empty().
     */
    public Optional<RatingDTO> getRatingDTO(String sku) { // Corregido a public
        String url = RESENAS_API_URL + sku ;
        try {
            RatingDTO respuesta = restTemplate.getForObject(url, RatingDTO.class);
            return Optional.ofNullable(respuesta);
        } catch (Exception e) {
            // Si falla (ej. no hay reseñas o el servicio está caído), no rompemos.
            log.warn("API Reseñas: No se pudo obtener rating para SKU {}. Causa: {}", sku, e.getMessage());
            return Optional.empty();
        }
    }
}