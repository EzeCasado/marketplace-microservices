package grupo2.catalogodeproductos_tpi.exception;

/**
 * Excepción personalizada para errores 409 (Conflicto) de categorías.
 *
 * Se lanza cuando se intenta crear una categoría con un id o un nombre
 * que ya existen.
 * (Ej. POST /categories con un id que ya está en la BD).
 */
public class CategoriaDuplicadaException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje de error.
     * @param message El mensaje que describe el conflicto (ej. "Ya existe una categoría con el id cat-libros")
     */
    public CategoriaDuplicadaException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta además la causa (ej. el error de la base cuando
     * dos altas chocan al mismo tiempo). La causa no llega a la respuesta.
     */
    public CategoriaDuplicadaException(String message, Throwable cause) {
        super(message, cause);
    }
}
