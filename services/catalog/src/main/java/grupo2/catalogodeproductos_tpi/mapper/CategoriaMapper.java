package grupo2.catalogodeproductos_tpi.mapper;

import grupo2.catalogodeproductos_tpi.dto.CategoriaDTO;
import grupo2.catalogodeproductos_tpi.model.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

/**
 * Interfaz de Mapper para MapStruct.
 * Define las reglas de conversión entre la Entidad Categoria y su DTO CategoriaDTO.
 *
 * @Mapper(componentModel = "spring") le dice a MapStruct que genere una implementación
 * de esta interfaz y la marque como un @Component de Spring, para poder inyectarla.
 */
@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    /**
     * Convierte una entidad Categoria a un CategoriaDTO.
     * @param categoria La entidad JPA.
     * @return El DTO.
     */
    CategoriaDTO toCategoriaDTO(Categoria categoria);

    /**
     * Convierte un CategoriaDTO a una entidad Categoria.
     * @param dto El Data Transfer Object.
     * @return La entidad JPA.
     */
    @Mapping(target = "id", ignore = true) // Ignoramos el ID al crear una nueva entidad desde un DTO.
    @Mapping(target = "productos", ignore = true) // No mapeamos la lista de productos al crear/actualizar una categoría.
    Categoria toCategoria(CategoriaDTO dto);

    /**
     * Convierte una lista de entidades Categoria a una lista de CategoriaDTO.
     * MapStruct genera automáticamente el código para iterar la lista.
     * @param categorias La lista de entidades.
     * @return La lista de DTOs.
     */
    List<CategoriaDTO> toCategoriaDTOList(List<Categoria> categorias);
}