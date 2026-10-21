package grupo2.catalogodeproductos_tpi.mapper;

import grupo2.catalogodeproductos_tpi.dto.CreateProductoDTO;
import grupo2.catalogodeproductos_tpi.dto.ProductoResponseDTO;
import grupo2.catalogodeproductos_tpi.dto.RatingDTO;
import grupo2.catalogodeproductos_tpi.dto.StockDTO;
import grupo2.catalogodeproductos_tpi.model.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = CategoriaMapper.class)
public interface ProductoMapper {

    // --- AL CREAR (DTO -> Entidad) ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    // IMPORTANTE: Forzamos que el producto nazca como 'activo = true'
    @Mapping(target = "activo", constant = "true") 
    // 'peso' se mapea automáticamente porque se llama igual en ambos lados
    @Mapping(target = "dimensiones", ignore = true) // Ignoramos dimensiones si no está en el DTO aún
    Producto toProducto(CreateProductoDTO createDTO);


    // --- AL RESPONDER (Entidad -> DTO) ---
    @Mapping(target = "sku", source = "producto.sku")
    @Mapping(target = "nombre", source = "producto.nombre")
    @Mapping(target = "descripcion", source = "producto.descripcion")
    @Mapping(target = "precio", source = "producto.precio")
    // IMPORTANTE: Mapeamos los nuevos campos
    @Mapping(target = "peso", source = "producto.peso") 
    @Mapping(target = "activo", source = "producto.activo")
    // ---------------------------------------
    @Mapping(target = "categoria", source = "producto.categoria")
    @Mapping(target = "fechaCreacion", source = "producto.fechaCreacion")
    @Mapping(target = "stock", source = "stockDTO.disponibilidad")
    @Mapping(target = "ratingPromedio", source = "ratingDTO.promedio")
    @Mapping(target = "cantidadResenas", source = "ratingDTO.cantidad")
    ProductoResponseDTO toProductoResponseDTO(Producto producto, StockDTO stockDTO, RatingDTO ratingDTO);
}