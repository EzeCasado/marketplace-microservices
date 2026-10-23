package grupo2.catalogodeproductos_tpi;

import com.jayway.jsonpath.JsonPath;
import grupo2.catalogodeproductos_tpi.repository.CategoriaRepository;
import grupo2.catalogodeproductos_tpi.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Los códigos de error del contrato (docs/api/catalog.yaml, tabla "Códigos de
 * error"): cada test provoca un error y revisa el código HTTP, el campo `error`
 * y que haya un `message` sin detalles internos.
 *
 * Las escrituras pasan por el interceptor antes que por el controlador. Un POST
 * que tiene que llegar hasta ahí necesita la X-API-Key y a Usuarios respondiendo
 * 200; si no, sale 401 antes de llegar al error que se quiere probar.
 *
 * Ningún caso crea datos: todas las altas se rechazan antes de guardar. Los
 * POST lo comprueban contando lo que hay en la base antes y después.
 */
class CodigosDeErrorTests extends TestDeIntegracion {

    private static final String API_KEY = "clave-de-prueba";

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Test
    void unJsonRotoDa400SinMostrarElDetalleDeJackson() {
        usuariosAceptaLaKey();
        long categoriasAntes = categoriaRepository.count();

        // Le falta la llave de cierre.
        ResponseEntity<String> respuesta = postConKey("/categories", MediaType.APPLICATION_JSON,
                "{\"nombre\": \"Libros\"");

        String mensaje = mensajeDe(respuesta, 400, "PeticionInvalida");
        assertThat(mensaje).doesNotContain("jackson", "JSON parse");
        assertThat(categoriaRepository.count()).isEqualTo(categoriasAntes);
    }

    @Test
    void unIdMalFormadoDa400() {
        // "abc" ya es un id válido (daría 404): el 400 sale de un id fuera del patrón.
        ResponseEntity<String> respuesta = restTemplate.getForEntity("/categories/{id}", String.class, "Cat_Libros");

        mensajeDe(respuesta, 400, "PeticionInvalida");
    }

    @Test
    void unaRutaQueNoExisteDa404() {
        ResponseEntity<String> respuesta = restTemplate.getForEntity("/no-existe", String.class);

        mensajeDe(respuesta, 404, "RecursoNoEncontrado");
    }

    @Test
    void unMetodoQueLaRutaNoAceptaDa405SinPedirLaKey() {
        // Sin X-API-Key y sin stub de Usuarios: el 405 sale antes del interceptor.
        ResponseEntity<String> respuesta = restTemplate.exchange("/categories", HttpMethod.PUT,
                HttpEntity.EMPTY, String.class);

        mensajeDe(respuesta, 405, "MetodoNoPermitido");
    }

    @Test
    void unBodyQueNoEsJsonDa415() {
        usuariosAceptaLaKey();
        long categoriasAntes = categoriaRepository.count();

        ResponseEntity<String> respuesta = postConKey("/categories", MediaType.TEXT_PLAIN, "hola");

        mensajeDe(respuesta, 415, "TipoDeContenidoNoSoportado");
        assertThat(categoriaRepository.count()).isEqualTo(categoriasAntes);
    }

    @Test
    void pedirXmlDa406() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_XML));

        ResponseEntity<String> respuesta = restTemplate.exchange("/categories", HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        mensajeDe(respuesta, 406, "FormatoNoAceptable");
    }

    @Test
    void unAltaDeCategoriaSinNombreDa422() {
        usuariosAceptaLaKey();
        long categoriasAntes = categoriaRepository.count();

        ResponseEntity<String> respuesta = postConKey("/categories", MediaType.APPLICATION_JSON, "{}");

        String mensaje = mensajeDe(respuesta, 422, "FaltanCamposObligatorios");
        assertThat(mensaje).startsWith("Faltan campos obligatorios: ").contains("nombre");
        assertThat(categoriaRepository.count()).isEqualTo(categoriasAntes);
    }

    @Test
    void unNombreConSoloEspaciosDa422() {
        usuariosAceptaLaKey();
        long categoriasAntes = categoriaRepository.count();

        ResponseEntity<String> respuesta = postConKey("/categories", MediaType.APPLICATION_JSON,
                "{\"nombre\": \"   \"}");

        String mensaje = mensajeDe(respuesta, 422, "FaltanCamposObligatorios");
        assertThat(mensaje).startsWith("Faltan campos obligatorios: ").contains("nombre");
        assertThat(categoriaRepository.count()).isEqualTo(categoriasAntes);
    }

    @Test
    void unNombreVacioDa422ConElCampoUnaSolaVez() {
        usuariosAceptaLaKey();
        long categoriasAntes = categoriaRepository.count();

        // El texto vacío rompe @NotBlank y @Size a la vez: el campo se nombra una sola vez.
        ResponseEntity<String> respuesta = postConKey("/categories", MediaType.APPLICATION_JSON,
                "{\"nombre\": \"\"}");

        String mensaje = mensajeDe(respuesta, 422, "FaltanCamposObligatorios");
        assertThat(mensaje).startsWith("Faltan campos obligatorios: ").containsOnlyOnce("nombre");
        assertThat(categoriaRepository.count()).isEqualTo(categoriasAntes);
    }

    @Test
    void unNombreCortoDa400() {
        usuariosAceptaLaKey();
        long categoriasAntes = categoriaRepository.count();

        ResponseEntity<String> respuesta = postConKey("/categories", MediaType.APPLICATION_JSON,
                "{\"id\": \"cat-libros\", \"nombre\": \"ab\"}");

        String mensaje = mensajeDe(respuesta, 400, "PeticionInvalida");
        // El formato es "campo: mensaje", sin prefijo.
        assertThat(mensaje).startsWith("nombre: ").doesNotContain("Faltan campos");
        assertThat(categoriaRepository.count()).isEqualTo(categoriasAntes);
    }

    @Test
    void dosValoresInvalidosDan400ConLosErroresSeparadosPorComa() {
        usuariosAceptaLaKey();
        long productosAntes = productoRepository.count();

        // No falta ningún campo: el sku es muy corto (@Size) y el precio es negativo (@Positive).
        ResponseEntity<String> respuesta = postConKey("/products", MediaType.APPLICATION_JSON,
                "{\"sku\": \"ab\", \"nombre\": \"abc\", \"precio\": -5, \"peso\": 1, \"categoriaId\": \"cat-libros\"}");

        String mensaje = mensajeDe(respuesta, 400, "PeticionInvalida");
        // "campo: mensaje" por cada error, separados por coma y en orden alfabético de campo.
        assertThat(mensaje).startsWith("precio: ")
                .containsSubsequence("precio: ", ", sku: ")
                .doesNotContain("Faltan campos");
        assertThat(productoRepository.count()).isEqualTo(productosAntes);
    }

    @Test
    void unAltaDeProductoConFaltantesYUnPrecioInvalidoDa422SinNombrarElPrecio() {
        usuariosAceptaLaKey();
        long productosAntes = productoRepository.count();

        // Faltan nombre, peso y categoriaId, y el precio es inválido: gana el 422 con los que faltan.
        ResponseEntity<String> respuesta = postConKey("/products", MediaType.APPLICATION_JSON,
                "{\"sku\": \"BK-001\", \"precio\": -5}");

        String mensaje = mensajeDe(respuesta, 422, "FaltanCamposObligatorios");
        // Los campos van en orden alfabético, para que el texto no cambie entre corridas.
        assertThat(mensaje).startsWith("Faltan campos obligatorios: ")
                .containsSubsequence("categoriaId", "nombre", "peso")
                .doesNotContain("precio");
        assertThat(productoRepository.count()).isEqualTo(productosAntes);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void unErrorInesperadoDa500SinMostrarElDetalle(CapturedOutput salida) {
        ResponseEntity<String> respuesta = restTemplate.getForEntity("/test/error-inesperado", String.class);

        String mensaje = mensajeDe(respuesta, 500, "ErrorInternoDelServidor");
        assertThat(mensaje).isEqualTo("Ocurrió un error inesperado");
        assertThat(respuesta.getBody()).doesNotContain("detalle interno", "IllegalStateException");
        // Lo que se esconde en la respuesta tiene que quedar en el log.
        assertThat(salida.getAll()).contains("detalle interno");
    }

    private void usuariosAceptaLaKey() {
        usuarios.stubFor(post("/usuarios/validar").willReturn(ok()));
    }

    private ResponseEntity<String> postConKey(String ruta, MediaType contentType, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
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
