package grupo2.catalogodeproductos_tpi;

import com.jayway.jsonpath.JsonPath;
import grupo2.catalogodeproductos_tpi.model.Categoria;
import grupo2.catalogodeproductos_tpi.repository.CategoriaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Levanta la aplicación completa, con Tomcat en un puerto libre y MySQL
 * en un contenedor, y le pega por HTTP como lo haría un cliente.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class CatalogoDeProductosTpiApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

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
        libros.setNombre("Libros");
        libros = categoriaRepository.save(libros);

        ResponseEntity<String> respuesta = restTemplate.getForEntity("/categories", String.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        // "$..nombre" busca el campo en cualquier nivel: sirve con el array de hoy
        // y con el { "items": [...] } que pide el contrato.
        List<String> nombres = JsonPath.read(respuesta.getBody(), "$..nombre");
        assertThat(nombres).contains("Libros");
    }
}
