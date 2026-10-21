package grupo2.catalogodeproductos_tpi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@SpringBootApplication
public class CatalogoDeProductosTpiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogoDeProductosTpiApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {


        return builder.setConnectTimeout(Duration.ofSeconds(3)) // 3 segundos para establecer una conexión
                .setReadTimeout(Duration.ofSeconds(3)) // 3 segundos para recibir una respuesta
                .build();
    }

}
