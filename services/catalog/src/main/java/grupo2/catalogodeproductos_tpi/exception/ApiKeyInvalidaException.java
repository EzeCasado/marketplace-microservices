package grupo2.catalogodeproductos_tpi.exception;

/**
 * Excepción para errores 401 (No Autorizado).
 *
 * La lanza ApiKeyAuthInterceptor cuando una escritura llega sin X-API-Key
 * o cuando Usuarios responde que la key es inválida (401).
 *
 * El mensaje es fijo y es el del contrato (docs/api/catalog.yaml).
 */
public class ApiKeyInvalidaException extends RuntimeException {

    public ApiKeyInvalidaException() {
        super("Se requiere una X-API-Key válida para esta operación");
    }
}
