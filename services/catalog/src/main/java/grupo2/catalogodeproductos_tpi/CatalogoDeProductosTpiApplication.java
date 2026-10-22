package grupo2.catalogodeproductos_tpi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.time.Duration;

@SpringBootApplication
public class CatalogoDeProductosTpiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogoDeProductosTpiApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {


        return builder
                // HTTP/1.1 fijo: si no, el cliente del JDK intenta pasar a HTTP/2 (h2c) en cada conexión
                // por http://, y con un POST con cuerpo el servidor puede cortar el pedido (#32)
                .requestFactoryBuilder(ClientHttpRequestFactoryBuilder.jdk()
                        .withHttpClientCustomizer(cliente -> cliente.version(HttpClient.Version.HTTP_1_1)))
                .connectTimeout(Duration.ofSeconds(3)) // 3 segundos para establecer una conexión
                .readTimeout(Duration.ofSeconds(3)) // 3 segundos para recibir una respuesta
                .build();
    }

}
