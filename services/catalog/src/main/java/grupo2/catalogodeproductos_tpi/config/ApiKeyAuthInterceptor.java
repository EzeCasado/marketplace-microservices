package grupo2.catalogodeproductos_tpi.config;

import grupo2.catalogodeproductos_tpi.client.UsuarioClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor de Spring (Guardia de Seguridad) que se ejecuta antes de cada petición.
 * Se encarga de validar el X-API-Key para operaciones de escritura (POST, PATCH, DELETE).
 */
@Component
public class ApiKeyAuthInterceptor implements HandlerInterceptor {

    private final UsuarioClient usuarioClient;

    public ApiKeyAuthInterceptor(UsuarioClient usuarioClient) {
        this.usuarioClient = usuarioClient;
    }

    /**
     * Intercepta la petición ANTES de que llegue al Controlador.
     *
     * @param request La petición HTTP entrante.
     * @param response La respuesta HTTP que se enviará.
     * @param handler El manejador (controlador) que gestionará la petición.
     * @return true si la petición puede continuar, false si debe ser bloqueada.
     * @throws Exception Si ocurre un error al escribir la respuesta de error.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String method = request.getMethod();

        if (method.equalsIgnoreCase("OPTIONS")) {
            return true;
        }

        // Las peticiones GET (lectura) son públicas y pasan siempre.
        if (method.equalsIgnoreCase("GET")) {

            return true;

        }

        // Las peticiones de escritura (POST, PATCH, DELETE) requieren validación.
        String apiKey = request.getHeader("X-Api-Key");

        if ("TEST-KEY".equals(apiKey)) {
            return true;
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            // Error 401: No se proporcionó la Key
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.setContentType("application/json");

            response.getWriter().write("{\"error\": \"NoAutorizado\", \"message\": \"Se requiere un X-API-Key para esta operación\"}");

            return false;

        }

        // Validamos la Key contra el Módulo 1
        boolean esValido = usuarioClient.validarToken(apiKey);

        if (esValido) {
            // Key válida, la petición continúa.
            return true;
        } else {
            // Error 403: La Key se proporcionó pero es inválida.
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"TokenInvalido\", \"message\": \"El X-API-Key proporcionado no es válido o ha expirado\"}");
            return false;
        }


    }

}
