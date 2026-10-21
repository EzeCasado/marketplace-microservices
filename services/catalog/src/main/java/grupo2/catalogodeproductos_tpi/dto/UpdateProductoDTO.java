package grupo2.catalogodeproductos_tpi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO para la actualización parcial (PATCH) de un Producto.
 * Usamos un 'record'. Todos los campos son opcionales (pueden ser nulos)
 * para permitir que el cliente envíe solo lo que desea cambiar.
 * * Las validaciones (@Positive, @Size) solo se aplican SI el campo es enviado.
 */
public record UpdateProductoDTO(

    @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
    String nombre, // Si es null, no se actualiza

    String descripcion, // Si es null, no se actualiza

    @Positive(message = "El precio debe ser un valor positivo")
    BigDecimal precio, // Si es null, no se actualiza
    
    // --- NUEVOS CAMPOS (OPCIONALES) ---
    @Positive(message = "El peso debe ser un valor positivo (en KGs)")
    BigDecimal peso, // Permite corregir el peso

    @Size(max = 100, message = "Las dimensiones no pueden exceder los 100 caracteres")
    String dimensiones, // Permite corregir dimensiones
    // ----------------------------------

    @Min(value = 0, message = "El stock no puede ser negativo")
    Integer stock // Si es null, no se actualiza
) {
}