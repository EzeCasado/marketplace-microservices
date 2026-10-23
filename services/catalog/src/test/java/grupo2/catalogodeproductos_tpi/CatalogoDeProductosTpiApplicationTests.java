package grupo2.catalogodeproductos_tpi;

import com.jayway.jsonpath.JsonPath;
import grupo2.catalogodeproductos_tpi.model.Categoria;
import grupo2.catalogodeproductos_tpi.repository.CategoriaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Levanta la aplicación completa contra el MySQL del contenedor (ver
 * TestDeIntegracion) y le pega por HTTP como lo haría un cliente.
 */
class CatalogoDeProductosTpiApplicationTests extends TestDeIntegracion {

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Categoria libros;

    @AfterEach
    void borrarDatosDePrueba() {
        if (libros != null) {
            categoriaRepository.delete(libros);
        }
    }

    @Test
    void listaLasCategoriasGuardadasEnLaBase() {
        libros = new Categoria();
        libros.setId("cat-libros");
        libros.setNombre("Libros");
        libros = categoriaRepository.save(libros);

        ResponseEntity<String> respuesta = restTemplate.getForEntity("/categories", String.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<String> nombres = JsonPath.read(respuesta.getBody(), "$.items[*].nombre");
        assertThat(nombres).contains("Libros");
    }
}
