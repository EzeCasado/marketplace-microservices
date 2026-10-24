package grupo2.catalogodeproductos_tpi;

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
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Los campos del alta de un producto (docs/api/catalog.yaml: Tipo, Peso, Volumen,
 * PesoYVolumenSegunTipo, ProductoAlta y POST /products): el `tipo`, el `peso` y el
 * `volumen` según el tipo, el 400 para un campo que el body no admite y la respuesta
 * del alta, con la forma de Producto.
 *
 * Las reglas de peso y volumen dependen del tipo. Un FISICO sin `peso` o sin
 * `volumen` da 422 junto con los demás campos que falten; un DIGITAL con alguno de
 * los dos da 400. Sin `tipo`, ninguna de las dos reglas corre y el 422 dice solo
 * `tipo`.
 *
 * Las altas pasan por el interceptor: cada POST va con X-API-Key y con Usuarios
 * respondiendo 200 en /usuarios/validar.
 *
 * En cada rechazo la categoría existe. Así, si una validación no corre, el alta sale
 * 201 y el test falla por eso, y no porque la categoría no estaba.
 *
 * Cada test borra lo que creó: el @AfterEach borra primero los productos por SKU
 * (la categoría no se puede borrar mientras tenga productos) y después la categoría
 * por id, exista o no.
 */
class CamposDelProductoTests extends TestDeIntegracion {

    private static final String API_KEY = "clave-de-prueba";

    private static final String SKU_LIBRO = "BK-001";
    private static final String SKU_EBOOK = "EB-001";
    private static final String ID_CATEGORIA = "cat-libros";

    // Todos los SKU que usa esta clase, para borrarlos exista o no el producto.
    private static final List<String> SKUS_DE_ESTA_CLASE = List.of(SKU_LIBRO, SKU_EBOOK);

    private static final String PETICION_INVALIDA = "PeticionInvalida";
    private static final String FALTAN_CAMPOS = "FaltanCamposObligatorios";

    private static final String NOMBRE = "\"nombre\": \"Libro POO\"";
    private static final String PRECIO = "\"precio\": 15000";
    private static final String CATEGORIA = "\"categoriaId\": \"" + ID_CATEGORIA + "\"";
    private static final String TIPO_FISICO = "\"tipo\": \"FISICO\"";
    private static final String TIPO_DIGITAL = "\"tipo\": \"DIGITAL\"";

    private static final String CAMPO_NO_SE_ACEPTA = "El campo %s no se acepta en esta operación";

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void borrarDatosDePrueba() {
        for (String sku : SKUS_DE_ESTA_CLASE) {
            productoRepository.findBySku(sku).ifPresent(productoRepository::delete);
        }
        categoriaRepository.deleteAllById(List.of(ID_CATEGORIA));
    }

    // ---------------------------------------------------------------------------------
    // Altas que salen bien
    // ---------------------------------------------------------------------------------

    @Test
    void unFisicoConPesoYVolumenDa201YGuardaTipoPesoYVolumen() {
        usuariosAceptaLaKey();
        guardarCategoria();
        long productosAntes = productoRepository.count();

        ResponseEntity<String> respuesta = postConKey("/products",
                altaDe(SKU_LIBRO, TIPO_FISICO, peso("0.45"), volumen("0.0012")));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(201);
        String sku = JsonPath.read(respuesta.getBody(), "$.sku");
        assertThat(sku).isEqualTo(SKU_LIBRO);
        Producto guardado = productoRepository.findBySku(SKU_LIBRO).orElseThrow();
        assertThat(guardado.getTipo()).isEqualTo(TipoProducto.FISICO);
        // BigDecimal.equals mira también la escala: 0.450 y 0.001200 son las de las columnas.
        assertThat(guardado.getPeso()).isEqualTo(new BigDecimal("0.450"));
        assertThat(guardado.getVolumen()).isEqualTo(new BigDecimal("0.001200"));
        // El texto de la columna, no el número del enum: si alguien reordena el enum, no cambia lo guardado.
        assertThat(tipoEnLaBase(SKU_LIBRO)).isEqualTo("FISICO");
        assertThat(productoRepository.count()).isEqualTo(productosAntes + 1);
    }

    @Test
    void unDigitalSinPesoNiVolumenDa201YLosGuardaEnNull() {
        usuariosAceptaLaKey();
        guardarCategoria();
        long productosAntes = productoRepository.count();

        ResponseEntity<String> respuesta = postConKey("/products", altaDe(SKU_EBOOK, TIPO_DIGITAL));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(201);
        Producto guardado = productoRepository.findBySku(SKU_EBOOK).orElseThrow();
        assertThat(guardado.getTipo()).isEqualTo(TipoProducto.DIGITAL);
        assertThat(guardado.getPeso()).isNull();
        assertThat(guardado.getVolumen()).isNull();
        assertThat(tipoEnLaBase(SKU_EBOOK)).isEqualTo("DIGITAL");
        assertThat(productoRepository.count()).isEqualTo(productosAntes + 1);
    }

    @Test
    void elVolumenMinimoDa201() {
        usuariosAceptaLaKey();
        guardarCategoria();

        // 0.000001 m3 (1 cm3) es el menor volumen que entra en decimal(10,6).
        ResponseEntity<String> respuesta = postConKey("/products",
                altaDe(SKU_LIBRO, TIPO_FISICO, peso("0.45"), volumen("0.000001")));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(201);
        Producto guardado = productoRepository.findBySku(SKU_LIBRO).orElseThrow();
        assertThat(guardado.getVolumen()).isEqualTo(new BigDecimal("0.000001"));
    }

    @Test
    void elPesoYElVolumenMaximosDeLasColumnasDan201() {
        usuariosAceptaLaKey();
        guardarCategoria();

        // 7 enteros y 3 decimales en peso, 4 enteros y 6 en volumen: lo que entra justo en
        // decimal(10,3) y decimal(10,6). Si @Digits dejara pasar más, el guardado daría 500.
        ResponseEntity<String> respuesta = postConKey("/products",
                altaDe(SKU_LIBRO, TIPO_FISICO, peso("9999999.999"), volumen("9999.999999")));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(201);
        Producto guardado = productoRepository.findBySku(SKU_LIBRO).orElseThrow();
        assertThat(guardado.getPeso()).isEqualTo(new BigDecimal("9999999.999"));
        assertThat(guardado.getVolumen()).isEqualTo(new BigDecimal("9999.999999"));
    }

    @Test
    void laRespuestaDelAltaTraeCantidadEnNullYNoTraeActivoNiFechaDeCreacion() {
        usuariosAceptaLaKey();
        guardarCategoria();

        ResponseEntity<String> respuesta = postConKey("/products",
                altaDe(SKU_LIBRO, TIPO_FISICO, peso("0.45"), volumen("0.0012")));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(201);
        Map<String, Object> creado = JsonPath.read(respuesta.getBody(), "$");
        // El stock inicial se carga en Inventario: hasta que lo tenga, `cantidad` viene en null, pero viene.
        assertThat(creado)
                .containsEntry("cantidad", null)
                .containsEntry("tipo", "FISICO")
                .containsKeys("peso", "volumen")
                .doesNotContainKeys("activo", "fechaCreacion", "stock");
    }

    // ---------------------------------------------------------------------------------
    // 422: faltan campos
    // ---------------------------------------------------------------------------------

    @Test
    void unFisicoSinVolumenDa422() {
        comprobarQueSeRechaza(altaDe(SKU_LIBRO, TIPO_FISICO, peso("0.45")),
                422, FALTAN_CAMPOS, "Faltan campos obligatorios: volumen");
    }

    @Test
    void unFisicoSinPesoDa422() {
        comprobarQueSeRechaza(altaDe(SKU_LIBRO, TIPO_FISICO, volumen("0.0012")),
                422, FALTAN_CAMPOS, "Faltan campos obligatorios: peso");
    }

    @Test
    void unFisicoSinPesoNiVolumenDa422ConLosDos() {
        comprobarQueSeRechaza(altaDe(SKU_LIBRO, TIPO_FISICO),
                422, FALTAN_CAMPOS, "Faltan campos obligatorios: peso, volumen");
    }

    @Test
    void unFisicoSinPesoYSinNombreDa422ConLosDosCampos() {
        // La condición de la decisión del 2026-10-23: el error de FISICO tiene que llegar como error
        // del campo `peso`, para salir en el mismo 422 que los demás. Si saliera solo `nombre`
        // (o el alta pasara), la regla estaría llegando como error de la clase entera.
        String sinNombreNiPeso = json(campoSku(SKU_LIBRO), PRECIO, CATEGORIA, TIPO_FISICO, volumen("0.0012"));

        comprobarQueSeRechaza(sinNombreNiPeso, 422, FALTAN_CAMPOS, "Faltan campos obligatorios: nombre, peso");
    }

    @Test
    void unAltaSinTipoYSinPesoDa422SoloConElTipo() {
        // Sin `tipo` ninguna de las dos reglas corre: el 422 no nombra `peso` ni `volumen`.
        comprobarQueSeRechaza(altaDe(SKU_LIBRO),
                422, FALTAN_CAMPOS, "Faltan campos obligatorios: tipo");
    }

    @Test
    void unAltaSinTipoPeroConPesoYVolumenDa422SoloConElTipo() {
        // Tampoco corre la regla de DIGITAL: sin tipo no se sabe si el peso sobra.
        comprobarQueSeRechaza(altaDe(SKU_LIBRO, peso("0.45"), volumen("0.0012")),
                422, FALTAN_CAMPOS, "Faltan campos obligatorios: tipo");
    }

    // ---------------------------------------------------------------------------------
    // 400: un DIGITAL no lleva peso ni volumen
    // ---------------------------------------------------------------------------------

    @Test
    void unDigitalConPesoDa400() {
        comprobarQueSeRechaza(altaDe(SKU_EBOOK, TIPO_DIGITAL, peso("1")),
                400, PETICION_INVALIDA, "peso: no va en un producto DIGITAL");
    }

    @Test
    void unDigitalConVolumenDa400() {
        comprobarQueSeRechaza(altaDe(SKU_EBOOK, TIPO_DIGITAL, volumen("0.0012")),
                400, PETICION_INVALIDA, "volumen: no va en un producto DIGITAL");
    }

    @Test
    void unDigitalConPesoYVolumenDa400ConLosDosErroresSeparadosPorComa() {
        comprobarQueSeRechaza(altaDe(SKU_EBOOK, TIPO_DIGITAL, peso("1"), volumen("0.0012")),
                400, PETICION_INVALIDA, "peso: no va en un producto DIGITAL, volumen: no va en un producto DIGITAL");
    }

    // ---------------------------------------------------------------------------------
    // 400: un tipo que no es FISICO ni DIGITAL
    // ---------------------------------------------------------------------------------

    // "fisico" (el enum distingue mayúsculas), 0 (el ordinal de FISICO) y "1" (el ordinal como texto)
    // tampoco valen: el contrato solo admite los nombres FISICO y DIGITAL.
    @ParameterizedTest(name = "\"tipo\": {0} da 400")
    @ValueSource(strings = {"\"OTRO\"", "\"fisico\"", "0", "\"1\""})
    void unTipoQueNoEsFisicoNiDigitalDa400(String tipo) {
        comprobarQueSeRechaza(altaDe(SKU_LIBRO, "\"tipo\": " + tipo, peso("0.45"), volumen("0.0012")),
                400, PETICION_INVALIDA, "tipo: tiene que ser FISICO o DIGITAL");
    }

    @Test
    void unTipoInvalidoGanaAunqueFalteElNombre() {
        // Jackson corta al leer el body, antes de validar: sale el 400 del tipo, no el 422 de `nombre`.
        String sinNombre = json(campoSku(SKU_LIBRO), PRECIO, CATEGORIA, "\"tipo\": \"OTRO\"",
                peso("0.45"), volumen("0.0012"));

        comprobarQueSeRechaza(sinNombre, 400, PETICION_INVALIDA, "tipo: tiene que ser FISICO o DIGITAL");
    }

    @Test
    void unValorQueNoEsUnNumeroNoSeMuestraComoErrorDelTipo() {
        // Otro valor mal escrito del body sigue con el mensaje de siempre: no es un error del enum.
        String precioConLetras = json(campoSku(SKU_LIBRO), NOMBRE, "\"precio\": \"abc\"", CATEGORIA, TIPO_FISICO,
                peso("0.45"), volumen("0.0012"));

        comprobarQueSeRechaza(precioConLetras, 400, PETICION_INVALIDA, "El body no es un JSON válido");
    }

    // ---------------------------------------------------------------------------------
    // 400: decimales de más y valores que no son positivos
    // ---------------------------------------------------------------------------------

    @Test
    void unVolumenConMasDeSeisDecimalesDa400() {
        // 0.0000004 m3 quedaría en 0 en la base: se rechaza en vez de redondearlo.
        comprobarQueSeRechaza(altaDe(SKU_LIBRO, TIPO_FISICO, peso("0.45"), volumen("0.0000004")),
                400, PETICION_INVALIDA, "volumen: puede tener hasta 6 decimales");
    }

    @Test
    void unPesoConMasDeTresDecimalesDa400() {
        comprobarQueSeRechaza(altaDe(SKU_LIBRO, TIPO_FISICO, peso("0.4505"), volumen("0.0012")),
                400, PETICION_INVALIDA, "peso: puede tener hasta 3 decimales");
    }

    // Límite de @Digits: con más enteros de los permitidos sale el mismo texto que con más decimales.
    @ParameterizedTest(name = "peso {0} y volumen {1} dan 400: {2}")
    @CsvSource(delimiterString = "|", value = {
            "0.45 | 10000 | volumen: puede tener hasta 6 decimales",
            "10000000 | 0.0012 | peso: puede tener hasta 3 decimales"
    })
    void unEnteroDeMasDa400ConElTextoDeLosDecimales(String pesoEnviado, String volumenEnviado, String mensaje) {
        comprobarQueSeRechaza(altaDe(SKU_LIBRO, TIPO_FISICO, peso(pesoEnviado), volumen(volumenEnviado)),
                400, PETICION_INVALIDA, mensaje);
    }

    @ParameterizedTest(name = "el volumen {0} da 400")
    @ValueSource(strings = {"0", "-1"})
    void unVolumenQueNoEsPositivoDa400(String volumenEnviado) {
        comprobarQueSeRechaza(altaDe(SKU_LIBRO, TIPO_FISICO, peso("0.45"), volumen(volumenEnviado)),
                400, PETICION_INVALIDA, "volumen: El volumen debe ser mayor a 0 m3");
    }

    // ---------------------------------------------------------------------------------
    // 400: campos que el body no admite
    // ---------------------------------------------------------------------------------

    @Test
    void unAltaConCantidadDa400() {
        // La cantidad es de Inventario: el contrato cierra el body (additionalProperties: false).
        comprobarQueSeRechaza(
                altaDe(SKU_LIBRO, TIPO_FISICO, peso("0.45"), volumen("0.0012"), "\"cantidad\": 10"),
                400, PETICION_INVALIDA, CAMPO_NO_SE_ACEPTA.formatted("cantidad"));
    }

    @Test
    void unAltaConCantidadYStockDa400NombrandoSoloUno() {
        usuariosAceptaLaKey();
        guardarCategoria();
        long productosAntes = productoRepository.count();

        ResponseEntity<String> respuesta = postConKey("/products",
                altaDe(SKU_LIBRO, TIPO_FISICO, peso("0.45"), volumen("0.0012"), "\"cantidad\": 10", "\"stock\": 5"));

        String mensaje = mensajeDe(respuesta, 400, PETICION_INVALIDA);
        // Jackson corta en el primer campo de más: el message nombra uno solo, no los dos.
        assertThat(mensaje).isIn(CAMPO_NO_SE_ACEPTA.formatted("cantidad"), CAMPO_NO_SE_ACEPTA.formatted("stock"));
        assertThat(productoRepository.count()).isEqualTo(productosAntes);
    }

    @Test
    void unAltaDeCategoriaConDescripcionDa400YNoCreaLaCategoria() {
        usuariosAceptaLaKey();
        long categoriasAntes = categoriaRepository.count();

        // El contrato cierra el body del post de /categories (unevaluatedProperties: false) y no tiene
        // `descripcion`: se rechaza en vez de ignorarla.
        ResponseEntity<String> respuesta = postConKey("/categories",
                json("\"id\": \"" + ID_CATEGORIA + "\"", "\"nombre\": \"Libros\"", "\"descripcion\": \"x\""));

        String mensaje = mensajeDe(respuesta, 400, PETICION_INVALIDA);
        assertThat(mensaje).isEqualTo(CAMPO_NO_SE_ACEPTA.formatted("descripcion"));
        assertThat(categoriaRepository.existsById(ID_CATEGORIA)).isFalse();
        assertThat(categoriaRepository.count()).isEqualTo(categoriasAntes);
    }

    // ---------------------------------------------------------------------------------
    // Armado de bodies y revisión de lo que se rechaza
    // ---------------------------------------------------------------------------------

    /**
     * Manda el alta y comprueba que se rechaza: el código, el `error`, el `message`
     * exacto y que la cantidad de productos no cambie.
     */
    private void comprobarQueSeRechaza(String body, int codigo, String error, String mensajeEsperado) {
        usuariosAceptaLaKey();
        guardarCategoria();
        long productosAntes = productoRepository.count();

        ResponseEntity<String> respuesta = postConKey("/products", body);

        String mensaje = mensajeDe(respuesta, codigo, error);
        assertThat(mensaje).isEqualTo(mensajeEsperado);
        assertThat(productoRepository.count()).isEqualTo(productosAntes);
    }

    private static String json(String... campos) {
        return "{" + String.join(", ", campos) + "}";
    }

    private static String campoSku(String sku) {
        return "\"sku\": \"" + sku + "\"";
    }

    /** Un alta con el sku, el nombre, el precio y la categoría de siempre, más los campos que se pasen. */
    private static String altaDe(String sku, String... resto) {
        String[] campos = Stream.concat(Stream.of(campoSku(sku), NOMBRE, PRECIO, CATEGORIA), Stream.of(resto))
                .toArray(String[]::new);
        return json(campos);
    }

    private static String peso(String valor) {
        return "\"peso\": " + valor;
    }

    private static String volumen(String valor) {
        return "\"volumen\": " + valor;
    }

    private void guardarCategoria() {
        Categoria categoria = new Categoria();
        categoria.setId(ID_CATEGORIA);
        categoria.setNombre("Libros");
        categoriaRepository.save(categoria);
    }

    private String tipoEnLaBase(String sku) {
        return jdbcTemplate.queryForObject("select tipo from productos where sku = ?", String.class, sku);
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
