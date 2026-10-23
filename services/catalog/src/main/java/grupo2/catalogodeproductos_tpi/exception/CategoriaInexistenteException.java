package grupo2.catalogodeproductos_tpi.exception;

/**
 * Excepción personalizada para errores 400 (Petición inválida) del alta de productos.
 *
 * Se lanza cuando el body de un producto trae un categoriaId bien formado
 * pero que no corresponde a ninguna categoría.
 * (Ej. POST /products con "categoriaId": "cat-no-existe").
 *
 * No es un 404: el recurso pedido es el producto, y la categoría es un valor
 * inválido del body (docs/api/catalog.yaml, POST /products).
 */
public class CategoriaInexistenteException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje de error.
     * @param message El mensaje que describe el error (ej. "No existe una categoría con el id cat-no-existe")
     */
    public CategoriaInexistenteException(String message) {
        super(message);
    }
}
