package grupo2.catalogodeproductos_tpi;

import com.jayway.jsonpath.JsonPath;
import grupo2.catalogodeproductos_tpi.model.Categoria;
import grupo2.catalogodeproductos_tpi.model.Producto;
import grupo2.catalogodeproductos_tpi.model.TipoProducto;
import grupo2.catalogodeproductos_tpi.repository.CategoriaRepository;
import grupo2.catalogodeproductos_tpi.repository.ProductoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * El categoriaId de texto en los productos (docs/api/catalog.yaml: POST /products,
 * ProductoAlta y el filtro categoriaId de GET /products): el alta con una
 * categoría que existe, el 400 para una que no existe o que está mal formada, el
 * 422 si falta, y el filtro del listado.
 *
 * Las altas pasan por el interceptor: cada POST va con X-API-Key y con Usuarios
 * respondiendo 200 en /usuarios/validar.
 *
 * Cada test borra lo que creó: el @AfterEach borra primero los productos por SKU
 * (la categoría no se puede borrar mientras tenga productos) y después las
 * categorías por id, existan o no.
 */
class AltaDeProductoTests extends TestDeIntegracion {

    private static final String API_KEY = "clave-de-prueba";

    private static final String SKU_LIBRO = "BK-001";
    private static final String SKU_DISCO = "MU-001";

    // Solo ids válidos: la intercalación de MySQL no distingue mayúsculas, y un id como
    // "Cat_Libros" podría encontrar otra fila y hacer fallar el borrado (ver CategoriasTests).
    private static final List<String> CATEGORIAS_DE_ESTA_CLASE = List.of("cat-libros", "cat-musica");

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @AfterEach
    void borrarDatosDePrueba() {
        for (String sku : List.of(SKU_LIBRO, SKU_DISCO)) {
            productoRepository.findBySku(sku).ifPresent(productoRepository::delete);
        }
        categoriaRepository.deleteAllById(CATEGORIAS_DE_ESTA_CLASE);
    }

    @Test
    void unAltaConUnaCategoriaQueExisteDa201YTraeLaCategoria() {
        usuariosAceptaLaKey();
        guardarCategoria("cat-libros", "Libros");

        ResponseEntity<String> respuesta = postConKey("/products", altaDeLibro("cat-libros"));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(201);
        String sku = JsonPath.read(respuesta.getBody(), "$.sku");
        Map<String, Object> categoria = JsonPath.read(respuesta.getBody(), "$.categoria");
        assertThat(sku).isEqualTo(SKU_LIBRO);
        // El contrato: la categoría tiene solo `id` y `nombre`, sin `descripcion`.
        assertThat(categoria).containsOnlyKeys("id", "nombre")
                .containsEntry("id", "cat-libros")
                .containsEntry("nombre", "Libros");
        assertThat(productoRepository.findBySku(SKU_LIBRO)).isPresent();
    }

    @Test
    void unaCategoriaQueNoExisteDa400YNoCreaElProducto() {
        usuariosAceptaLaKey();
        long productosAntes = productoRepository.count();

        ResponseEntity<String> respuesta = postConKey("/products", altaDeLibro("cat-no-existe"));

        // Es un valor inválido del body (400), no un recurso que falta (404).
        String mensaje = mensajeDe(respuesta, 400, "PeticionInvalida");
        assertThat(mensaje).isEqualTo("No existe una categoría con el id cat-no-existe");
        assertThat(productoRepository.count()).isEqualTo(productosAntes);
    }

    @Test
    void unaCategoriaMalFormadaDa400NombrandoElCampo() {
        usuariosAceptaLaKey();
        long productosAntes = productoRepository.count();

        ResponseEntity<String> respuesta = postConKey("/products", altaDeLibro("Cat_Libros"));

        String mensaje = mensajeDe(respuesta, 400, "PeticionInvalida");
        assertThat(mensaje).startsWith("categoriaId: ");
        assertThat(productoRepository.count()).isEqualTo(productosAntes);
    }

    @Test
    void unAltaSinCategoriaDa422NombrandoElCampo() {
        usuariosAceptaLaKey();
        long productosAntes = productoRepository.count();

        ResponseEntity<String> respuesta = postConKey("/products", """
                {"sku": "BK-001", "nombre": "Libro POO", "tipo": "FISICO", "precio": 15000, "peso": 0.45, "volumen": 0.0012}
                """);

        String mensaje = mensajeDe(respuesta, 422, "FaltanCamposObligatorios");
        assertThat(mensaje).isEqualTo("Faltan campos obligatorios: categoriaId");
        assertThat(productoRepository.count()).isEqualTo(productosAntes);
    }

    @Test
    void unSkuRepetidoDa409AunqueLaCategoriaNoExista() {
        usuariosAceptaLaKey();
        Categoria libros = guardarCategoria("cat-libros", "Libros");
        guardarProducto(SKU_LIBRO, "Libro POO", libros);
        long productosAntes = productoRepository.count();

        // El SKU se revisa antes que la categoría.
        ResponseEntity<String> respuesta = postConKey("/products", altaDeLibro("cat-no-existe"));

        mensajeDe(respuesta, 409, "Conflicto");
        assertThat(productoRepository.count()).isEqualTo(productosAntes);
    }

    @Test
    void elListadoFiltraPorCategoria() {
        Categoria libros = guardarCategoria("cat-libros", "Libros");
        Categoria musica = guardarCategoria("cat-musica", "Música");
        guardarProducto(SKU_LIBRO, "Libro POO", libros);
        guardarProducto(SKU_DISCO, "Disco de vinilo", musica);

        ResponseEntity<String> respuesta = restTemplate.getForEntity("/products?categoriaId={categoriaId}",
                String.class, "cat-libros");

        assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
        List<String> skus = JsonPath.read(respuesta.getBody(), "$[*].sku");
        assertThat(skus).containsExactly(SKU_LIBRO);
    }

    @Test
    void elListadoConUnaCategoriaQueNoExisteDevuelveLaListaVacia() {
        Categoria libros = guardarCategoria("cat-libros", "Libros");
        guardarProducto(SKU_LIBRO, "Libro POO", libros);

        ResponseEntity<String> respuesta = restTemplate.getForEntity("/products?categoriaId={categoriaId}",
                String.class, "cat-no-existe");

        assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
        List<Object> productos = JsonPath.read(respuesta.getBody(), "$");
        assertThat(productos).isEmpty();
    }

    /** Un alta completa de un producto FISICO: con peso y volumen. */
    private static String altaDeLibro(String categoriaId) {
        return """
                {"sku": "BK-001", "nombre": "Libro POO", "tipo": "FISICO", "precio": 15000, "peso": 0.45, "volumen": 0.0012, "categoriaId": "%s"}
                """.formatted(categoriaId);
    }

    private Categoria guardarCategoria(String id, String nombre) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre(nombre);
        return categoriaRepository.save(categoria);
    }

    private void guardarProducto(String sku, String nombre, Categoria categoria) {
        Producto producto = new Producto();
        producto.setSku(sku);
        producto.setNombre(nombre);
        producto.setTipo(TipoProducto.FISICO);
        producto.setPrecio(new BigDecimal("15000"));
        producto.setPeso(new BigDecimal("0.45"));
        producto.setVolumen(new BigDecimal("0.0012"));
        producto.setCategoria(categoria);
        productoRepository.save(producto);
    }

    private void usuariosAceptaLaKey() {
        usuarios.stubFor(post("/usuarios/validar").willReturn(ok()));
    }

    private ResponseEntity<String> postConKey(String ruta, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", API_KEY);
        return restTemplate.exchange(ruta, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
    }

    /**
     * Revisa el código y el `error`, y que haya un `message` sin nombres de clases,
     * de paquetes ni texto de excepciones. Devuelve el `message` para que cada test
     * revise lo suyo.
     */
    private static String mensajeDe(ResponseEntity<String> respuesta, int codigo, String error) {
        assertThat(respuesta.getStatusCode().value()).isEqualTo(codigo);
        assertThat(respuesta.getBody()).as("cuerpo del error").isNotBlank();
        String errorRecibido = JsonPath.read(respuesta.getBody(), "$.error");
        String mensaje = JsonPath.read(respuesta.getBody(), "$.message");
        assertThat(errorRecibido).isEqualTo(error);
        assertThat(mensaje).isNotBlank()
                .doesNotContain("Exception", "org.springframework", "tools.jackson", "java.lang");
        return mensaje;
    }
}
