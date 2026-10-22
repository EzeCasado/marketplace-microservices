package grupo2.catalogodeproductos_tpi;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Base de los tests que levantan la aplicación completa: Tomcat en un puerto
 * libre, MySQL en un contenedor y Usuarios, Inventario y Reseñas simulados con
 * WireMock, cada uno en su propio servidor.
 *
 * Todas las clases de test la extienden, así comparten un solo contexto de
 * Spring: la app, el MySQL y los WireMock arrancan una vez para toda la corrida.
 * Una clase que sume su propio @DynamicPropertySource, @MockitoBean o @Import
 * arma otro contexto y arranca otro MySQL.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import(TestcontainersConfiguration.class)
public abstract class TestDeIntegracion {

    protected static final WireMockServer usuarios = new WireMockServer(options().dynamicPort());
    protected static final WireMockServer inventario = new WireMockServer(options().dynamicPort());
    protected static final WireMockServer resenas = new WireMockServer(options().dynamicPort());

    static {
        usuarios.start();
        inventario.start();
        resenas.start();
    }

    // Pisa las URLs de application.properties para que la app llame a los WireMock.
    // Las rutas son las que usan hoy los clientes; el #17 las cambia por las de la consigna.
    @DynamicPropertySource
    static void apuntarALosServiciosSimulados(DynamicPropertyRegistry registry) {
        registry.add("api.usuarios.url", () -> usuarios.baseUrl() + "/usuarios/validar");
        registry.add("api.inventario.url", () -> inventario.baseUrl() + "/stock/");
        registry.add("api.resenas.url", () -> resenas.baseUrl() + "/reviews/rating/");
    }

    private static final JdkClientHttpRequestFactory clienteHttp = new JdkClientHttpRequestFactory();

    @Autowired
    protected TestRestTemplate restTemplate;

    // El cliente que trae el TestRestTemplate (HttpURLConnection) no puede leer un 401: tira
    // "cannot retry due to server authentication, in streaming mode". El de java.net.http sí.
    // Cambia solo el cliente con el que el test le pega a la app, no el RestTemplate de la app.
    @BeforeEach
    void usarElClienteHttpDeJava() {
        restTemplate.getRestTemplate().setRequestFactory(clienteHttp);
    }

    // Cada test arranca sin stubs ni pedidos registrados. Lo que no tiene stub, WireMock lo responde con 404.
    @BeforeEach
    void limpiarServiciosSimulados() {
        usuarios.resetAll();
        inventario.resetAll();
        resenas.resetAll();
    }
}
