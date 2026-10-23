package grupo2.catalogodeproductos_tpi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador que existe solo en los tests: tira una excepción que ningún
 * manejador conoce, para comprobar el 500 (ver CodigosDeErrorTests).
 *
 * Está en el mismo paquete que la aplicación para que el escaneo de componentes
 * lo levante en el contexto compartido de TestDeIntegracion, sin armar otro.
 */
@RestController
public class ControladorDeErrorInesperado {

    @GetMapping("/test/error-inesperado")
    public String errorInesperado() {
        throw new IllegalStateException("detalle interno");
    }
}
