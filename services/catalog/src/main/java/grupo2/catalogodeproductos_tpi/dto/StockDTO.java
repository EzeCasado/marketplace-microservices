package grupo2.catalogodeproductos_tpi.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Data Transfer Object (DTO) para deserializar la respuesta JSON
 * del Módulo 3 (Inventario).
 * [cite_start]Ejemplo: {"sku":"BK-001","disponible":10} [cite: 178]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDTO {

    private String sku;
    private Integer disponibilidad;

}
