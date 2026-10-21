package grupo2.catalogodeproductos_tpi.exception;

/**
 * Excepción personalizada para errores 409 (Conflicto).
 *
 * Se lanza cuando se intenta realizar una operación que viola
 * una regla de negocio, como crear un recurso que ya existe.
 * (Ej. POST /products con un SKU que ya está en la BD).
 */
public class SkuAlreadyExistsException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje de error.
     * @param message El mensaje que describe el conflicto (ej. "El SKU 'XYZ' ya existe.")
     */
    public SkuAlreadyExistsException(String message) {
        super(message);
    }
}