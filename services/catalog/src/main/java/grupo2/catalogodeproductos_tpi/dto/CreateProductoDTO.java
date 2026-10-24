package grupo2.catalogodeproductos_tpi.dto;

import grupo2.catalogodeproductos_tpi.model.TipoProducto;
import grupo2.catalogodeproductos_tpi.validation.DigitalSinPesoNiVolumen;
import grupo2.catalogodeproductos_tpi.validation.FisicoConPesoYVolumen;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

// Los campos, en el orden de ProductoAlta (docs/api/catalog.yaml). El body es cerrado: un campo de más
// (ej. cantidad) lo rechaza el conversor de WebConfig, no este DTO.
// Las dos anotaciones de clase piden peso y volumen en un FISICO y los prohíben en un DIGITAL.
@FisicoConPesoYVolumen
@DigitalSinPesoNiVolumen
public record CreateProductoDTO(

    @NotBlank(message = "El SKU no puede estar vacío")
    @Size(min = 3, max = 50, message = "El SKU debe tener entre 3 y 50 caracteres")
    String sku,

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
    String nombre,

    String descripcion,

    @NotNull(message = "El tipo no puede ser nulo")
    TipoProducto tipo,

    @NotNull(message = "El precio no puede ser nulo")
    @Positive(message = "El precio debe ser un valor positivo")
    BigDecimal precio,

    // En kilogramos. Opcional acá: si es obligatorio lo dice @FisicoConPesoYVolumen.
    @Positive(message = "El peso debe ser mayor a 0 kg")
    @Digits(integer = 7, fraction = 3, message = "puede tener hasta 3 decimales")
    BigDecimal peso,

    // En metros cúbicos, con seis decimales (1 cm³). Opcional por lo mismo que el peso.
    @Positive(message = "El volumen debe ser mayor a 0 m3")
    @Digits(integer = 4, fraction = 6, message = "puede tener hasta 6 decimales")
    BigDecimal volumen,

    // Mismo patrón y mismos mensajes que el id de CategoriaDTO (CategoriaId en docs/api/catalog.yaml)
    @NotNull(message = "El ID de la categoría no puede ser nulo")
    @Size(min = 3, max = 50, message = "El id debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = CategoriaDTO.PATRON_ID, message = "El id solo puede tener minúsculas, números y guiones entre medio")
    String categoriaId
) {
}
