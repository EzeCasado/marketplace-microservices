package grupo2.catalogodeproductos_tpi.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Clase de Configuración de Spring MVC.
 * Se utiliza para registrar componentes web, como nuestro Interceptor.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Autowired
    private ApiKeyAuthInterceptor apiKeyAuthInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a todas las rutas (/products, /categories, etc.)
                .allowedOrigins("http://localhost:3000") // Solo dejamos pasar a nuestro Frontend
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // Métodos permitidos
                .allowedHeaders("*") // Permitir todos los headers (incluyendo X-API-Key)
                .allowCredentials(true);
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
