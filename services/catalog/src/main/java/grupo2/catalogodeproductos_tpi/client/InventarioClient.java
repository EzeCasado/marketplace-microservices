package grupo2.catalogodeproductos_tpi.client;

import grupo2.catalogodeproductos_tpi.dto.StockDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

/**
 * Cliente HTTP para comunicarse con el Módulo 3 (Inventario).
 * Se encarga de obtener el stock de los productos.
 * @Component lo marca como un bean de Spring para ser inyectado en otros servicios.
 */
@Component
public class InventarioClient {

    @Autowired
    private RestTemplate restTemplate; // Bean configurado en la clase principal

    // Inyecta la URL desde application.properties
    @Value("${api.inventario.url}")
    private String INVENTARIO_API_URL;

    // Logger para registrar errores sin romper la aplicación
    private static final Logger log = LoggerFactory.getLogger(InventarioClient.class);

    /**
     * Llama al Módulo 3 para obtener el stock disponible de un SKU específico.
     *
     * @param sku El SKU (String) del producto a consultar.
     * @return un Optional<StockDTO>. 
     * - Si tiene éxito, devuelve Optional.of(StockDTO).
     * - Si falla (404, 500, timeout), devuelve Optional.empty().
     * Esto evita NullPointerExceptions en el servicio que lo llama.
     */
    public Optional<StockDTO> getStockPorSku(String sku) {
        String url = INVENTARIO_API_URL + sku;
        try {
            // Intenta obtener el objeto. RestTemplate maneja la deserialización de JSON a StockDTO.
            StockDTO respuesta = restTemplate.getForObject(url, StockDTO.class);
            // Envuelve la respuesta en un Optional. ofNullable es seguro si la API devuelve null.
            return Optional.ofNullable(respuesta);
        } catch (Exception e) {
            // Captura CUALQUIER error (404, 503, conexión, etc.)
            // Registra el error en lugar de lanzarlo, para no "caer" el detalle del producto.
            log.warn("API Inventario: No se pudo obtener stock para SKU {}. Causa: {}", sku, e.getMessage());
            // Devuelve un Optional vacío para indicar que el dato no está disponible.
            return Optional.empty();
        }
    }
}