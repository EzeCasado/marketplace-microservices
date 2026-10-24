package grupo2.catalogodeproductos_tpi.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO para la actualización parcial (PATCH) de un Producto.
 * Usamos un 'record'. Todos los campos son opcionales (pueden ser nulos)
 * para permitir que el cliente envíe solo lo que desea cambiar.
 * * Las validaciones (@Positive, @Size, @Digits) solo se aplican SI el campo es enviado.
 *
 * Los campos son los de ProductoCambio (docs/api/catalog.yaml), menos categoriaId. El body es cerrado:
 * un campo de más (ej. stock, tipo o sku) lo rechaza el conversor de WebConfig, no este DTO.
 * Hoy el servicio aplica nombre, descripcion y precio. peso y volumen se validan pero no se guardan:
 * aplicarlos, junto con categoriaId, es del #19.
 */
public record UpdateProductoDTO(

    @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
    String nombre, // Si es null, no se actualiza

    String descripcion, // Si es null, no se actualiza

    @Positive(message = "El precio debe ser un valor positivo")
    BigDecimal precio, // Si es null, no se actualiza

    // En kilogramos, con los mismos límites que en el alta.
    @Positive(message = "El peso debe ser un valor positivo (en KGs)")
    @Digits(integer = 7, fraction = 3, message = "puede tener hasta 3 decimales")
    BigDecimal peso, // Todavía no se guarda (#19)

    // En metros cúbicos, con los mismos límites que en el alta.
    @Positive(message = "El volumen debe ser mayor a 0 m3")
    @Digits(integer = 4, fraction = 6, message = "puede tener hasta 6 decimales")
    BigDecimal volumen // Todavía no se guarda (#19)
) {
}
