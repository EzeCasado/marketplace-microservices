package grupo2.catalogodeproductos_tpi.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Data Transfer Object (DTO) para deserializar la respuesta JSON
 * del Módulo 7 (Reseñas).
 * [cite_start]Ejemplo: {"sku":"BK-001","promedio":4.6,"cantidad":18} [cite: 329]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingDTO {

    private String sku;
    private Double promedio;
    private Integer cantidad;

}
