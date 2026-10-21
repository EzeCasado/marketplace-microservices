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
 * JpaRepository<Categoria, Long>:
 * 1. 'Categoria': Es la clase de la Entidad que va a manejar.
 * 2. 'Long': Es el tipo de dato de la Clave Primaria (@Id) de esa entidad.
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    // ¡No necesitamos nada más! Spring Data JPA se encarga del resto.
}