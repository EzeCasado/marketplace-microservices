package grupo2.catalogodeproductos_tpi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar una Categoria.
 * Se usa tanto para crear (recibir JSON) como para devolver información (enviar JSON).
 * @Data (Lombok) Genera getters, setters, toString, etc.
 * @NoArgsConstructor (Lombok) Genera un constructor vacío (necesario para JSON).
 * @AllArgsConstructor (Lombok) Genera un constructor con todos los campos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDTO {

    /**
     * Patrón del id de una categoría (CategoriaId en docs/api/catalog.yaml):
     * minúsculas y números, en grupos separados por guiones.
     * Se comparte con el @PathVariable del controlador.
     */
    public static final String PATRON_ID = "^[a-z0-9]+(-[a-z0-9]+)*$";

    /**
     * El ID de la categoría. Lo elige quien la crea (ej. "cat-libros").
     */
    @NotBlank(message = "El id no puede estar vacío")
    @Size(min = 3, max = 50, message = "El id debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = PATRON_ID, message = "El id solo puede tener minúsculas, números y guiones entre medio")
    private String id;

    /**
     * El nombre de la categoría.
     * @NotBlank asegura que el JSON no venga vacío o nulo.
     * @Size define el largo permitido.
     */
    @NotBlank(message = "El nombre de la categoría no puede estar vacío")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;
}
