package grupo2.catalogodeproductos_tpi.model;

/**
 * Tipo de producto (Tipo en docs/api/catalog.yaml).
 *
 * FISICO se envía y necesita peso y volumen; DIGITAL no se envía y no los lleva.
 * En la base se guarda el nombre (@Enumerated(EnumType.STRING)), no la posición.
 */
public enum TipoProducto {
    FISICO,
    DIGITAL
}
