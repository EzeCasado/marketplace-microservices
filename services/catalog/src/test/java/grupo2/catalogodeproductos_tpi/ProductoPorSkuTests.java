package grupo2.catalogodeproductos_tpi;

import com.github.tomakehurst.wiremock.http.Fault;
import com.jayway.jsonpath.JsonPath;
import grupo2.catalogodeproductos_tpi.model.Categoria;
import grupo2.catalogodeproductos_tpi.model.Producto;
import grupo2.catalogodeproductos_tpi.repository.CategoriaRepository;
import grupo2.catalogodeproductos_tpi.repository.ProductoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable;
import static com.github.tomakehurst.wiremock.client.WireMock.unauthorized;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de /products/{sku}. El GET consulta a Inventario y a Reseñas, y el
 * DELETE valida la X-API-Key contra Usuarios; los tres están simulados con
 * WireMock (ver TestDeIntegracion).
 *
 * Es el molde para los tests de cada arreglo: los datos se guardan con los
 * repositorios en la misma clase, cada test arma los stubs que necesita y lo
 * creado se borra al final. Solo prueba lo que el contrato no cambia.
 */
class ProductoPorSkuTests extends TestDeIntegracion {

    private static final String SKU = "BK-001";

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private Categoria categoria;
    private Producto producto;

    @AfterEach
    void borrarDatosDePrueba() {
        if (producto != null) {
            productoRepository.delete(producto);
        }
        if (categoria != null) {
            categoriaRepository.delete(categoria);
        }
    }

    @Test
    void unSkuQueNoExisteDa404() {
        ResponseEntity<String> respuesta = restTemplate.getForEntity("/products/{sku}", String.class, "NO-EXISTE");

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        String error = JsonPath.read(respuesta.getBody(), "$.error");
        String mensaje = JsonPath.read(respuesta.getBody(), "$.message");
        assertThat(error).isEqualTo("RecursoNoEncontrado");
        assertThat(mensaje).isNotBlank();
    }

    @Test
    void conInventarioCaidoDevuelveElDetalleIgual() {
        guardarProducto();
        inventario.stubFor(get("/stock/" + SKU).willReturn(serviceUnavailable()));
        resenas.stubFor(get("/reviews/rating/" + SKU).willReturn(okJson("""
                {"sku": "BK-001", "promedio": 4.6, "cantidad": 18}
                """)));

        ResponseEntity<String> respuesta = restTemplate.getForEntity("/products/{sku}", String.class, SKU);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        String sku = JsonPath.read(respuesta.getBody(), "$.sku");
        String nombre = JsonPath.read(respuesta.getBody(), "$.nombre");
        assertThat(sku).isEqualTo(SKU);
        assertThat(nombre).isEqualTo("Libro POO");
        // El stock no se revisa: el #15 le cambia el nombre al campo y el #16 cambia lo que se lee de Inventario.
    }

    @Test
    void conInventarioLentoRespondeSinEsperarlo() {
        guardarProducto();
        // Más que los 3 segundos de timeout del RestTemplate de la app.
        Duration demora = Duration.ofSeconds(6);
        inventario.stubFor(get("/stock/" + SKU).willReturn(okJson("""
                {"sku": "BK-001", "disponible": 10}
                """).withFixedDelay((int) demora.toMillis())));
        resenas.stubFor(get("/reviews/rating/" + SKU).willReturn(okJson("""
                {"sku": "BK-001", "promedio": 4.6, "cantidad": 18}
                """)));

        long inicio = System.nanoTime();
        ResponseEntity<String> respuesta = restTemplate.getForEntity("/products/{sku}", String.class, SKU);
        Duration duracion = Duration.ofNanos(System.nanoTime() - inicio);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        String sku = JsonPath.read(respuesta.getBody(), "$.sku");
        assertThat(sku).isEqualTo(SKU);
        assertThat(duracion).isLessThan(demora);
    }

    @Test
    void unaKeyQueUsuariosAceptaDejaDarDeBaja() {
        guardarProducto();
        usuarios.stubFor(post("/usuarios/validar").willReturn(ok()));

        ResponseEntity<String> respuesta = restTemplate.exchange("/products/{sku}", HttpMethod.DELETE,
                conApiKey("clave-de-prueba"), String.class, SKU);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        usuarios.verify(postRequestedFor(urlEqualTo("/usuarios/validar"))
                .withHeader("X-API-Key", equalTo("clave-de-prueba")));
    }

    @Test
    void sinApiKeyDa401SinLlamarAUsuarios() {
        guardarProducto();

        ResponseEntity<String> respuesta = restTemplate.exchange("/products/{sku}", HttpMethod.DELETE,
                HttpEntity.EMPTY, String.class, SKU);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        String error = JsonPath.read(respuesta.getBody(), "$.error");
        String mensaje = JsonPath.read(respuesta.getBody(), "$.message");
        assertThat(error).isEqualTo("NoAutorizado");
        assertThat(mensaje).isNotBlank();
        usuarios.verify(0, anyRequestedFor(anyUrl()));
        assertThat(productoRepository.findBySku(SKU)).get().extracting(Producto::getActivo).isEqualTo(true);
    }

    @Test
    void laTestKeyQueUsuariosRechazaNoDejaDarDeBaja() {
        guardarProducto();
        usuarios.stubFor(post("/usuarios/validar").willReturn(unauthorized()));

        ResponseEntity<String> respuesta = restTemplate.exchange("/products/{sku}", HttpMethod.DELETE,
                conApiKey("TEST-KEY"), String.class, SKU);

        // El código exacto no se revisa: hoy es 403 y el #18 lo pasa a 401.
        assertThat(respuesta.getStatusCode()).matches(HttpStatusCode::is4xxClientError, "es un 4xx");
        usuarios.verify(postRequestedFor(urlEqualTo("/usuarios/validar"))
                .withHeader("X-API-Key", equalTo("TEST-KEY")));
        assertThat(productoRepository.findBySku(SKU)).get().extracting(Producto::getActivo).isEqualTo(true);
    }

    @Test
    void laTestKeyConUsuariosCaidoNoDejaDarDeBaja() {
        guardarProducto();
        // Usuarios cierra la conexión sin responder. CONNECTION_RESET_BY_PEER no sirve:
        // en Windows deja la conexión colgada en vez de cortarla.
        usuarios.stubFor(post("/usuarios/validar").willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        ResponseEntity<String> respuesta = restTemplate.exchange("/products/{sku}", HttpMethod.DELETE,
                conApiKey("TEST-KEY"), String.class, SKU);

        // Solo se revisa que no pase: el #18 decide si con Usuarios caído da 401 o 503.
        assertThat(respuesta.getStatusCode()).matches(HttpStatusCode::isError, "es un 4xx o un 5xx");
        assertThat(productoRepository.findBySku(SKU)).get().extracting(Producto::getActivo).isEqualTo(true);
    }

    private void guardarProducto() {
        categoria = new Categoria();
        categoria.setNombre("Libros");
        categoria = categoriaRepository.save(categoria);

        producto = new Producto();
        producto.setSku(SKU);
        producto.setNombre("Libro POO");
        producto.setPrecio(new BigDecimal("15000"));
        producto.setPeso(new BigDecimal("0.45"));
        producto.setCategoria(categoria);
        producto = productoRepository.save(producto);
    }

    private static HttpEntity<Void> conApiKey(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiKey);
        return new HttpEntity<>(headers);
    }
}
