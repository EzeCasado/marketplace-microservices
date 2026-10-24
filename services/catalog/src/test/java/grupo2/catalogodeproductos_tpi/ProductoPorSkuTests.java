package grupo2.catalogodeproductos_tpi;

import com.github.tomakehurst.wiremock.http.Fault;
import com.jayway.jsonpath.JsonPath;
import grupo2.catalogodeproductos_tpi.model.Categoria;
import grupo2.catalogodeproductos_tpi.model.Producto;
import grupo2.catalogodeproductos_tpi.model.TipoProducto;
import grupo2.catalogodeproductos_tpi.repository.CategoriaRepository;
import grupo2.catalogodeproductos_tpi.repository.ProductoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.forbidden;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable;
import static com.github.tomakehurst.wiremock.client.WireMock.unauthorized;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de /products/{sku}. El GET consulta a Inventario y a Reseñas, y el
 * PATCH y el DELETE validan la X-API-Key contra Usuarios; los tres están simulados con
 * WireMock (ver TestDeIntegracion).
 *
 * Es el molde para los tests de cada arreglo: los datos se guardan con los
 * repositorios en la misma clase, cada test arma los stubs que necesita y lo
 * creado se borra al final.
 *
 * El detalle tiene los campos del schema Producto (docs/api/catalog.yaml), pero todavía
 * trae ratingPromedio y cantidadResenas, que saca el #29. Por eso se revisan las claves
 * que tiene y las que no, y no que tenga solo esas.
 */
class ProductoPorSkuTests extends TestDeIntegracion {

    private static final String SKU = "BK-001";
    private static final String SKU_EBOOK = "EB-001";

    private static final String CAMPO_NO_SE_ACEPTA = "El campo %s no se acepta en esta operación";

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
        // Sin respuesta de Inventario, `cantidad` viene en null, pero la clave está: el contrato la pide siempre.
        Map<String, Object> detalle = JsonPath.read(respuesta.getBody(), "$");
        assertThat(detalle).containsEntry("cantidad", null);
    }

    @Test
    void elDetalleDeUnFisicoTraeLosCamposDelContrato() {
        guardarProducto();
        // `reservado` no está en StockDTO. Si el cliente de Inventario leyera con el mapper estricto
        // de WebConfig, la lectura fallaría y `cantidad` vendría en null.
        inventario.stubFor(get("/stock/" + SKU).willReturn(okJson("""
                {"sku": "BK-001", "disponibilidad": 10, "reservado": 3}
                """)));
        resenas.stubFor(get("/reviews/rating/" + SKU).willReturn(okJson("""
                {"sku": "BK-001", "promedio": 4.6, "cantidad": 18}
                """)));

        ResponseEntity<String> respuesta = restTemplate.getForEntity("/products/{sku}", String.class, SKU);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> detalle = JsonPath.read(respuesta.getBody(), "$");
        assertThat(detalle)
                .containsKeys("sku", "nombre", "tipo", "precio", "peso", "volumen", "categoria", "cantidad")
                .doesNotContainKeys("activo", "fechaCreacion", "stock", "dimensiones")
                .containsEntry("tipo", "FISICO")
                .containsEntry("cantidad", 10);
    }

    @Test
    void elDetalleDeUnDigitalSinDescripcionNoTraePesoVolumenNiDescripcion() {
        guardarEbook();
        inventario.stubFor(get("/stock/" + SKU_EBOOK).willReturn(serviceUnavailable()));

        ResponseEntity<String> respuesta = restTemplate.getForEntity("/products/{sku}", String.class, SKU_EBOOK);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> detalle = JsonPath.read(respuesta.getBody(), "$");
        // El contrato no deja que un DIGITAL traiga peso ni volumen, y descripcion no puede ser null:
        // las claves no vienen. `cantidad`, en cambio, sale siempre, aunque sea en null.
        assertThat(detalle)
                .doesNotContainKeys("peso", "volumen", "descripcion")
                .containsEntry("tipo", "DIGITAL")
                .containsEntry("cantidad", null);
    }

    @Test
    void unPatchConPrecioCambiaElPrecio() {
        guardarProducto();
        usuarios.stubFor(post("/usuarios/validar").willReturn(ok()));

        ResponseEntity<String> respuesta = patchConKey("""
                {"precio": 16500}
                """);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        // BigDecimal.equals mira también la escala: 16500.00 es la de la columna, decimal(10,2).
        assertThat(precioEnLaBase()).isEqualTo(new BigDecimal("16500.00"));
    }

    // El stock es de Inventario y ni el tipo ni el sku se cambian (ProductoCambio en docs/api/catalog.yaml,
    // con additionalProperties: false); `dimensiones` ya no existe. Junto con el campo va un precio, para
    // comprobar que se rechaza el body entero y no solo el campo de más.
    @ParameterizedTest(name = "un PATCH con {0} da 400")
    @CsvSource(delimiterString = "|", value = {
            "stock | 5",
            "dimensiones | \"10x10\"",
            "cantidad | 5",
            "tipo | \"DIGITAL\"",
            "sku | \"BK-002\""
    })
    void unPatchConUnCampoQueNoAceptaDa400YNoCambiaElPrecio(String campo, String valor) {
        guardarProducto();
        usuarios.stubFor(post("/usuarios/validar").willReturn(ok()));

        ResponseEntity<String> respuesta = patchConKey("{\"precio\": 16500, \"" + campo + "\": " + valor + "}");

        comprobarError(respuesta, HttpStatus.BAD_REQUEST, "PeticionInvalida", CAMPO_NO_SE_ACEPTA.formatted(campo));
        assertThat(precioEnLaBase()).isEqualTo(new BigDecimal("15000.00"));
    }

    // Los mismos límites que en el alta: más decimales de los que entran en la columna dan 400 en vez de
    // redondearse. Aunque el PATCH todavía no guarda peso ni volumen (#19), ya los valida.
    @ParameterizedTest(name = "un PATCH con {0} da 400")
    @CsvSource(delimiterString = "|", value = {
            "\"volumen\": 0.0000004 | volumen: puede tener hasta 6 decimales",
            "\"peso\": 0.4505 | peso: puede tener hasta 3 decimales"
    })
    void unPatchConDecimalesDeMasDa400YNoCambiaElPrecio(String campo, String mensajeEsperado) {
        guardarProducto();
        usuarios.stubFor(post("/usuarios/validar").willReturn(ok()));

        ResponseEntity<String> respuesta = patchConKey("{\"precio\": 16500, " + campo + "}");

        comprobarError(respuesta, HttpStatus.BAD_REQUEST, "PeticionInvalida", mensajeEsperado);
        assertThat(precioEnLaBase()).isEqualTo(new BigDecimal("15000.00"));
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

        comprobarError(respuesta, HttpStatus.UNAUTHORIZED, "NoAutorizado",
                "Se requiere una X-API-Key válida para esta operación");
        usuarios.verify(postRequestedFor(urlEqualTo("/usuarios/validar"))
                .withHeader("X-API-Key", equalTo("TEST-KEY")));
        comprobarQueSigueActivo();
    }

    @Test
    void unaKeySinPermisoDa403() {
        guardarProducto();
        usuarios.stubFor(post("/usuarios/validar").willReturn(forbidden()));

        ResponseEntity<String> respuesta = restTemplate.exchange("/products/{sku}", HttpMethod.DELETE,
                conApiKey("clave-de-prueba"), String.class, SKU);

        comprobarError(respuesta, HttpStatus.FORBIDDEN, "SinPermiso",
                "La X-API-Key no tiene permiso para esta operación");
        comprobarQueSigueActivo();
    }

    @Test
    void laTestKeyConUsuariosCaidoNoDejaDarDeBaja() {
        guardarProducto();
        // Usuarios cierra la conexión sin responder. CONNECTION_RESET_BY_PEER no sirve:
        // en Windows deja la conexión colgada en vez de cortarla.
        usuarios.stubFor(post("/usuarios/validar").willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        ResponseEntity<String> respuesta = restTemplate.exchange("/products/{sku}", HttpMethod.DELETE,
                conApiKey("TEST-KEY"), String.class, SKU);

        comprobarServicioNoDisponible(respuesta);
        comprobarQueSigueActivo();
    }

    @Test
    void conUsuariosRespondiendo500Da503() {
        guardarProducto();
        usuarios.stubFor(post("/usuarios/validar").willReturn(serverError()));

        ResponseEntity<String> respuesta = restTemplate.exchange("/products/{sku}", HttpMethod.DELETE,
                conApiKey("clave-de-prueba"), String.class, SKU);

        comprobarServicioNoDisponible(respuesta);
        comprobarQueSigueActivo();
    }

    @Test
    void conLaRutaDeUsuariosMalConfiguradaDa503() {
        guardarProducto();
        // Un 404 de Usuarios no dice nada de la key: es la ruta la que no existe.
        usuarios.stubFor(post("/usuarios/validar").willReturn(notFound()));

        ResponseEntity<String> respuesta = restTemplate.exchange("/products/{sku}", HttpMethod.DELETE,
                conApiKey("clave-de-prueba"), String.class, SKU);

        comprobarServicioNoDisponible(respuesta);
        comprobarQueSigueActivo();
    }

    @Test
    void conUsuariosLentoDa503SinEsperarlo() {
        guardarProducto();
        // Más que los 3 segundos de timeout del RestTemplate de la app.
        Duration demora = Duration.ofSeconds(6);
        usuarios.stubFor(post("/usuarios/validar").willReturn(ok().withFixedDelay((int) demora.toMillis())));

        long inicio = System.nanoTime();
        ResponseEntity<String> respuesta = restTemplate.exchange("/products/{sku}", HttpMethod.DELETE,
                conApiKey("clave-de-prueba"), String.class, SKU);
        Duration duracion = Duration.ofNanos(System.nanoTime() - inicio);

        comprobarServicioNoDisponible(respuesta);
        assertThat(duracion).isLessThan(demora);
        comprobarQueSigueActivo();
    }

    private static void comprobarServicioNoDisponible(ResponseEntity<String> respuesta) {
        comprobarError(respuesta, HttpStatus.SERVICE_UNAVAILABLE, "ServicioNoDisponible",
                "No se pudo validar la X-API-Key en este momento");
    }

    private static void comprobarError(ResponseEntity<String> respuesta, HttpStatus status,
                                       String errorEsperado, String mensajeEsperado) {
        assertThat(respuesta.getStatusCode()).isEqualTo(status);
        String error = JsonPath.read(respuesta.getBody(), "$.error");
        String mensaje = JsonPath.read(respuesta.getBody(), "$.message");
        assertThat(error).isEqualTo(errorEsperado);
        assertThat(mensaje).isEqualTo(mensajeEsperado);
    }

    private void comprobarQueSigueActivo() {
        assertThat(productoRepository.findBySku(SKU)).get().extracting(Producto::getActivo).isEqualTo(true);
    }

    private BigDecimal precioEnLaBase() {
        return productoRepository.findBySku(SKU).orElseThrow().getPrecio();
    }

    private void guardarProducto() {
        guardarCategoria();

        producto = new Producto();
        producto.setSku(SKU);
        producto.setNombre("Libro POO");
        producto.setTipo(TipoProducto.FISICO);
        producto.setPrecio(new BigDecimal("15000"));
        producto.setPeso(new BigDecimal("0.45"));
        producto.setVolumen(new BigDecimal("0.0012"));
        producto.setCategoria(categoria);
        producto = productoRepository.save(producto);
    }

    /**
     * Un DIGITAL sin descripción, como el ejemplo `digital` del alta en el contrato: sin peso ni volumen.
     * Queda en el mismo campo que el FISICO, así el @AfterEach lo borra igual.
     */
    private void guardarEbook() {
        guardarCategoria();

        producto = new Producto();
        producto.setSku(SKU_EBOOK);
        producto.setNombre("Libro POO (e-book)");
        producto.setTipo(TipoProducto.DIGITAL);
        producto.setPrecio(new BigDecimal("9000"));
        producto.setCategoria(categoria);
        producto = productoRepository.save(producto);
    }

    private void guardarCategoria() {
        categoria = new Categoria();
        categoria.setId("cat-libros");
        categoria.setNombre("Libros");
        categoria = categoriaRepository.save(categoria);
    }

    private static HttpEntity<Void> conApiKey(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiKey);
        return new HttpEntity<>(headers);
    }

    /** El PATCH del producto guardado, con un body JSON y con la key que Usuarios acepta en cada test. */
    private ResponseEntity<String> patchConKey(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", "clave-de-prueba");
        return restTemplate.exchange("/products/{sku}", HttpMethod.PATCH, new HttpEntity<>(body, headers),
                String.class, SKU);
    }
}
