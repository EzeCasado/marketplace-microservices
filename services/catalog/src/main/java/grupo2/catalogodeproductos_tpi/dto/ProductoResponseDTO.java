package grupo2.catalogodeproductos_tpi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoResponseDTO(

    String sku,
    String nombre,
    String descripcion,
    BigDecimal precio,

    // --- NUEVOS CAMPOS ---
    BigDecimal peso,
    Boolean activo, // true = visible, false = dado de baja
    // ---------------------

    CategoriaDTO categoria,
    LocalDateTime fechaCreacion,

    Integer stock,
    Double ratingPromedio,
    Integer cantidadResenas
) {
}