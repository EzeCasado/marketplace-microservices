package grupo2.catalogodeproductos_tpi.repository;

import grupo2.catalogodeproductos_tpi.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Producto.
 *
 * Al igual que CategoriaRepository, JpaRepository<Producto, Long> nos da
 * todos los métodos CRUD básicos para la entidad 'Producto' (cuyo @Id es 'Long').
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // --- MÉTODOS DE CONSULTA PERSONALIZADOS ---
    //
    // Spring Data JPA tiene una característica poderosa: "Query Methods".
    // Si nombramos un método siguiendo una convención, Spring
    // ¡automáticamente escribe la consulta SQL por nosotros!

    /**
     * Busca un producto por su SKU (Stock Keeping Unit).
     *
     * Al llamarlo 'findBySku', Spring entiende que debe hacer:
     * "SELECT * FROM productos WHERE sku = ?"
     *
     * Usamos Optional<Producto> porque la búsqueda puede no encontrar nada (ser nula),
     * y Optional es la forma moderna y segura de manejar eso en Java.
     *
     * @param sku El SKU (String) a buscar.
     * @return un Optional que contiene el Producto si se encuentra, o un Optional vacío si no.
     */
    Optional<Producto> findBySku(String sku);

    /**
     * Método de utilidad para verificar la existencia de un producto por su SKU.
     *
     * Al llamarlo 'existsBySku', Spring entiende que debe hacer:
     * "SELECT COUNT(*) FROM productos WHERE sku = ?" y devolver true si es > 0.
     *
     * Es más rápido y eficiente que hacer findBySku().isPresent()
     * porque no necesita traer todos los datos del producto.
     * Lo usaremos en ProductoService para validar antes de crear un producto nuevo.
     *
     * @param sku El SKU (String) a verificar.
     * @return true si un producto con ese SKU ya existe, false de lo contrario.
     */
    boolean existsBySku(String sku);

    /**
     * Busca productos filtrando opcionalmente por nombre (parcial) y/o categoría.
     * Usa una consulta JPQL (lenguaje de consulta de JPA) para manejar los parámetros nulos.
     *
     * @param nombre El texto a buscar en el nombre del producto (parcial, ignora mayúsculas).
     * @param categoriaId El ID de la categoría (exacto).
     * @return Una lista de Productos que coinciden con los filtros.
     */
    @Query("SELECT p FROM Producto p WHERE " +
            "p.activo = true AND " + // <--- ¡ESTA ES LA LÍNEA CLAVE!
            "(:nombre IS NULL OR lower(p.nombre) LIKE lower(concat('%', :nombre, '%'))) AND " +
            "(:categoriaId IS NULL OR p.categoria.id = :categoriaId)")
    List<Producto> searchProducts(@Param("nombre") String nombre,
                                  @Param("categoriaId") Long categoriaId);
}