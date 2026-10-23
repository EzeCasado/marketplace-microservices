package grupo2.catalogodeproductos_tpi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para devolver la lista de categorías (ListaCategorias en docs/api/catalog.yaml).
 * Sale como { "items": [...] }, sin paginar.
 * @Data (Lombok) Genera getters, setters, toString, etc.
 * @NoArgsConstructor (Lombok) Genera un constructor vacío (necesario para JSON).
 * @AllArgsConstructor (Lombok) Genera un constructor con todos los campos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListaCategoriasDTO {

    /**
     * Las categorías, ordenadas por nombre. Si no hay ninguna, viene vacía (nunca null).
     */
    private List<CategoriaDTO> items;
}
