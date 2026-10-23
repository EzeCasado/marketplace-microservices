package grupo2.catalogodeproductos_tpi.repository;

import grupo2.catalogodeproductos_tpi.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad Categoria.
 *
 * Simplemente extendiendo JpaRepository, Spring nos da "gratis" todos
 * los métodos CRUD (Create, Read, Update, Delete) más comunes:
 * - save(categoria) -> Guarda o actualiza una categoría
 * - findById(id) -> Busca una categoría por su ID
 * - findAll() -> Devuelve todas las categorías
 * - deleteById(id) -> Borra una categoría
 *
 * @Repository le dice a Spring que esta es una interfaz de repositorio
 * y que debe crear un "bean" (un objeto manejado por Spring) a partir de ella.
 *
 * JpaRepository<Categoria, String>:
 * 1. 'Categoria': Es la clase de la Entidad que va a manejar.
 * 2. 'String': Es el tipo de dato de la Clave Primaria (@Id) de esa entidad
 *    (un id de texto, como "cat-libros", que elige quien crea la categoría).
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, String> {

    /**
     * Verifica si ya hay una categoría con ese nombre.
     * La base compara sin distinguir mayúsculas ni tildes (su intercalación),
     * así que "libros" coincide con "Libros".
     */
    boolean existsByNombre(String nombre);
}