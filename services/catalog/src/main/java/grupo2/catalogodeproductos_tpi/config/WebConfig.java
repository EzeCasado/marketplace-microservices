package grupo2.catalogodeproductos_tpi.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Clase de Configuración de Spring MVC.
 * Se utiliza para registrar componentes web, como nuestro Interceptor.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Autowired
    private ApiKeyAuthInterceptor apiKeyAuthInterceptor;

    // El JsonMapper que arma Spring Boot, con su configuración.
    @Autowired
    private JsonMapper jsonMapper;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a todas las rutas (/products, /categories, etc.)
                .allowedOrigins("http://localhost:3000") // Solo dejamos pasar a nuestro Frontend
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // Métodos permitidos
                .allowedHeaders("*") // Permitir todos los headers (incluyendo X-API-Key)
                .allowCredentials(true);
    }

    /**
     * Un campo de más en un body da 400: los bodies del contrato son cerrados (additionalProperties: false).
     *
     * Solo cambia el conversor que lee lo que entra a la API. El JsonMapper de Boot sigue tolerante,
     * para que los clientes de Usuarios, Inventario y Reseñas ignoren los campos que esos servicios sumen.
     * El mapper estricto sale del de Boot (rebuild) para conservar su configuración.
     * También rechaza un número como valor de un enum: por defecto Jackson lo toma como la posición,
     * y "tipo": 0 entraría como FISICO y "tipo": "1" como DIGITAL.
     *
     * Boot registra su conversor JSON en este mismo builder desde un WebMvcConfigurer con @Order(0).
     * Este no tiene @Order, así que corre después y su conversor es el que queda.
     */
    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        JsonMapper estricto = jsonMapper.rebuild()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(EnumFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
                .build();
        builder.withJsonConverter(new JacksonJsonHttpMessageConverter(estricto));
    }

    /**
     * Registra nuestros interceptores personalizados en el registro de Spring.
     *
     * @param registry El registro donde se añaden los interceptores.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Registra nuestro "Guardia" de API Key para todas las rutas.
        registry.addInterceptor(apiKeyAuthInterceptor);

    }
}
