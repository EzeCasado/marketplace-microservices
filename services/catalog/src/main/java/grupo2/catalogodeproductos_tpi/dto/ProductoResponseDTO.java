package grupo2.catalogodeproductos_tpi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import grupo2.catalogodeproductos_tpi.model.TipoProducto;
import java.math.BigDecimal;

// Los campos, en el orden de Producto (docs/api/catalog.yaml). El rating no está en el contrato
// y queda al final hasta el #29.
// Un DIGITAL no lleva peso ni volumen y la descripción no puede salir en null: esos tres se omiten
// cuando vienen en null. Solo esos, para que un campo obligatorio en null se vea y no desaparezca.
public record ProductoResponseDTO(

    String sku,
    String nombre,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String descripcion,

    TipoProducto tipo,
    BigDecimal precio,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    BigDecimal peso,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    BigDecimal volumen,

    CategoriaDTO categoria,

    // El stock disponible, de Inventario. Sale siempre: en null si Inventario no responde o no tiene el producto.
    Integer cantidad,

    Double ratingPromedio,
    Integer cantidadResenas
) {
}
