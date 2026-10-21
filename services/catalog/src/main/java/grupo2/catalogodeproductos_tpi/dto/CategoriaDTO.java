package grupo2.catalogodeproductos_tpi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar una Categoria.
 * Se usa tanto para crear/actualizar (recibir JSON) como para 
 * devolver información (enviar JSON).
 * @Data (Lombok) Genera getters, setters, toString, etc.
 * @NoArgsConstructor (Lombok) Genera un constructor vacío (necesario para JSON).
 * @AllArgsConstructor (Lombok) Genera un constructor con todos los campos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDTO {

    /**
     * El ID de la categoría. Al crear una nueva, este campo será null.
     * Al devolver una categoría, tendrá el valor de la BD.
     */
    private Long id;

    /**
     * El nombre de la categoría.
     * @NotBlank asegura que el JSON no venga vacío o nulo.
     * @Size define el largo permitido.
     */
    @NotBlank(message = "El nombre de la categoría no puede estar vacío")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    /**
     * Descripción opcional de la categoría.
     */
    @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres")
    private String descripcion;
}