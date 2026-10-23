package grupo2.catalogodeproductos_tpi.exception;

/**
 * Excepción para errores 403 (Sin Permiso).
 *
 * La lanza ApiKeyAuthInterceptor cuando Usuarios responde que la X-API-Key
 * es válida pero no tiene permiso para la operación (403).
 *
 * El mensaje es fijo y es el del contrato (docs/api/catalog.yaml).
 */
public class ApiKeySinPermisoException extends RuntimeException {

    public ApiKeySinPermisoException() {
        super("La X-API-Key no tiene permiso para esta operación");
    }
}
