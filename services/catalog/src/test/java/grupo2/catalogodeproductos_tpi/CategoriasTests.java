package grupo2.catalogodeproductos_tpi;

import com.jayway.jsonpath.JsonPath;
import grupo2.catalogodeproductos_tpi.model.Categoria;
import grupo2.catalogodeproductos_tpi.repository.CategoriaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Las categorías con id de texto (docs/api/catalog.yaml: CategoriaId, Categoria,
 * /categories y /categories/{categoryId}): el alta con el id que manda el cliente,
 * el 409 por id o por nombre repetido, la validación del id y la lectura por id.
 *
 * Las altas pasan por el interceptor: cada POST va con X-API-Key y con Usuarios
 * respondiendo 200 en /usuarios/validar.
 *
 * Cada test borra las categorías que creó: el @AfterEach borra por id todos los
 * que usa esta clase, existan o no (deleteAllById ignora los que no están). Así
 * también se limpia si un alta que debía rechazarse llegó a guardar.
 */
class CategoriasTests extends TestDeIntegracion {

    private static final String API_KEY = "clave-de-prueba";

    private static final String ID_DE_50 = "a".repeat(50);
    private static final String ID_DE_51 = "a".repeat(51);

    // Los ids válidos que crea o intenta crear algún test de esta clase. Los inválidos no van:
    // nunca llegan a la base, y la intercalación de MySQL no distingue mayúsculas, así que
    // "Cat-Libros" encontraría la fila de "cat-libros" y el borrado fallaría.
    private static final List<String> IDS_DE_ESTA_CLASE = List.of("cat-libros", "cat-otra", "abc", ID_DE_50);

    @Autowired
    private CategoriaRepository categoriaRepository;

    @AfterEach
    void borrarDatosDePrueba() {
        categoriaRepository.deleteAllById(IDS_DE_ESTA_CLASE);
    }

    @Test
    void crearUnaCategoriaDevuelve201YSePuedeLeerPorId() {
        usuariosAceptaLaKey();

        ResponseEntity<String> respuesta = postConKey("/categories", """
                {"id": "cat-libros", "nombre": "Libros"}
                """);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(201);
        comprobarCategoria(respuesta.getBody(), "cat-libros", "Libros");

        ResponseEntity<String> lectura = restTemplate.getForEntity("/categories/{categoryId}", String.class,
                "cat-libros");

        assertThat(lectura.getStatusCode().value()).isEqualTo(200);
        comprobarCategoria(lectura.getBody(), "cat-libros", "Libros");
    }

    @Test
    void unIdRepetidoDa409YNoPisaLaCategoriaQueEstaba() {
        usuariosAceptaLaKey();
        categoriaRepository.save(categoria("cat-libros", "Libros"));
        long categoriasAntes = categoriaRepository.count();

        ResponseEntity<String> respuesta = postConKey("/categories", """
                {"id": "cat-libros", "nombre": "Otra"}
                """);

        String mensaje = mensajeDe(respuesta, 409, "Conflicto");
        assertThat(mensaje).isEqualTo("Ya existe una categoría con el id cat-libros");
        // Si el alta hiciera un merge, la categoría que estaba pasaría a llamarse "Otra".
        assertThat(categoriaRepository.findById("cat-libros")).get()
                .extracting(Categoria::getNombre).isEqualTo("Libros");
        assertThat(categoriaRepository.count()).isEqualTo(categoriasAntes);
    }

    @Test
    void unNombreRepetidoDa409YNoCreaLaCategoria() {
        usuariosAceptaLaKey();
        categoriaRepository.save(categoria("cat-libros", "Libros"));

        ResponseEntity<String> respuesta = postConKey("/categories", """
                {"id": "cat-otra", "nombre": "Libros"}
                """);

        String mensaje = mensajeDe(respuesta, 409, "Conflicto");
        assertThat(mensaje).isEqualTo("Ya existe una categoría con el nombre Libros");
        assertThat(categoriaRepository.existsById("cat-otra")).isFalse();
    }

    @Test
    void unNombreQueSoloCambiaLasMayusculasTambienDa409() {
        usuariosAceptaLaKey();
        categoriaRepository.save(categoria("cat-libros", "Libros"));

        // Que "libros" choque con "Libros" sale de la intercalación de MySQL
        // (utf8mb4_0900_ai_ci, que no distingue mayúsculas ni tildes).
        ResponseEntity<String> respuesta = postConKey("/categories", """
                {"id": "cat-otra", "nombre": "libros"}
                """);

        String mensaje = mensajeDe(respuesta, 409, "Conflicto");
        assertThat(mensaje).startsWith("Ya existe una categoría con el nombre ");
        assertThat(categoriaRepository.existsById("cat-otra")).isFalse();
    }

    @Test
    void guardarUnaCategoriaNuevaConUnIdQueYaExisteFallaYNoPisaLaDeLaBase() {
        categoriaRepository.save(categoria("cat-libros", "Libros"));

        // Sin pasar por el servicio ni por su chequeo: lo único que impide el merge
        // es que Categoria sepa que es nueva (Persistable).
        Categoria repetida = categoria("cat-libros", "Pisada");

        assertThatThrownBy(() -> categoriaRepository.save(repetida))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(categoriaRepository.findById("cat-libros")).get()
                .extracting(Categoria::getNombre).isEqualTo("Libros");
    }

    @Test
    void unAltaSinIdDa422NombrandoElId() {
        usuariosAceptaLaKey();
        long categoriasAntes = categoriaRepository.count();

        ResponseEntity<String> respuesta = postConKey("/categories", """
                {"nombre": "Libros"}
                """);

        String mensaje = mensajeDe(respuesta, 422, "FaltanCamposObligatorios");
        assertThat(mensaje).isEqualTo("Faltan campos obligatorios: id");
        assertThat(categoriaRepository.count()).isEqualTo(categoriasAntes);
    }

    @ParameterizedTest(name = "el id \"{0}\" da 400")
    @MethodSource("idsInvalidos")
    void unIdInvalidoDa400(String id) {
        usuariosAceptaLaKey();
        long categoriasAntes = categoriaRepository.count();

        ResponseEntity<String> respuesta = postConKey("/categories",
                "{\"id\": \"" + id + "\", \"nombre\": \"Libros\"}");

        String mensaje = mensajeDe(respuesta, 400, "PeticionInvalida");
        assertThat(mensaje).startsWith("id: ");
        assertThat(categoriaRepository.count()).isEqualTo(categoriasAntes);
    }

    @ParameterizedTest(name = "el id \"{0}\" es válido")
    @MethodSource("bordesValidos")
    void losBordesValidosDelIdDan201(String id) {
        usuariosAceptaLaKey();

        ResponseEntity<String> respuesta = postConKey("/categories",
                "{\"id\": \"" + id + "\", \"nombre\": \"Libros\"}");

        assertThat(respuesta.getStatusCode().value()).isEqualTo(201);
        comprobarCategoria(respuesta.getBody(), id, "Libros");
        assertThat(categoriaRepository.existsById(id)).isTrue();
    }

    @Test
    void unIdQueNoExisteDa404() {
        ResponseEntity<String> respuesta = restTemplate.getForEntity("/categories/{categoryId}", String.class,
                "no-existe");

        mensajeDe(respuesta, 404, "RecursoNoEncontrado");
    }

    @ParameterizedTest(name = "GET /categories/{0} da 400")
    @ValueSource(strings = {"Cat_Libros", "ab"})
    void unIdMalFormadoEnLaRutaDa400(String id) {
        ResponseEntity<String> respuesta = restTemplate.getForEntity("/categories/{categoryId}", String.class, id);

        String mensaje = mensajeDe(respuesta, 400, "PeticionInvalida");
        assertThat(mensaje).isEqualTo("El parámetro categoryId tiene un valor inválido");
    }

    // @ValueSource no admite "a".repeat(50): el valor de una anotación tiene que ser una constante.
    static Stream<String> idsInvalidos() {
        return Stream.of("Cat-Libros", "cat_libros", "cat--libros", "-cat", "ab", ID_DE_51);
    }

    static Stream<String> bordesValidos() {
        return Stream.of("abc", ID_DE_50);
    }

    private static Categoria categoria(String id, String nombre) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre(nombre);
        return categoria;
    }

    /** El contrato: la categoría tiene solo `id` y `nombre`. */
    private static void comprobarCategoria(String body, String id, String nombre) {
        String idRecibido = JsonPath.read(body, "$.id");
        String nombreRecibido = JsonPath.read(body, "$.nombre");
        assertThat(idRecibido).isEqualTo(id);
        assertThat(nombreRecibido).isEqualTo(nombre);
        assertThat(body).doesNotContain("descripcion");
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
