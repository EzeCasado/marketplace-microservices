package grupo2.catalogodeproductos_tpi.exception;

/**
 * Excepción personalizada para errores 404 (No Encontrado).
 *
 * Hereda de RuntimeException, lo que significa que es una "unchecked exception".
 * No necesitamos poner "throws" en la firma de nuestros métodos.
 *
 * Se lanza cuando se busca un recurso (ej. Producto por SKU o Categoría por ID)
 * y este no existe en la base de datos.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje de error.
     * @param message El mensaje que describe el error (ej. "Producto no encontrado...")
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}