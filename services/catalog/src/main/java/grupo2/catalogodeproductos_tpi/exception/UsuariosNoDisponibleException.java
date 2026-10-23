package grupo2.catalogodeproductos_tpi.exception;

/**
 * Excepción para errores 503 (Servicio No Disponible).
 *
 * La lanza ApiKeyAuthInterceptor cuando no se pudo validar la X-API-Key
 * porque Usuarios no respondió como se esperaba: otro código que no sea
 * 2xx, 401 o 403, un timeout o la conexión cortada o rechazada.
 *
 * El mensaje es fijo y es el del contrato (docs/api/catalog.yaml).
 */
public class UsuariosNoDisponibleException extends RuntimeException {

    public UsuariosNoDisponibleException() {
        super("No se pudo validar la X-API-Key en este momento");
    }
}
