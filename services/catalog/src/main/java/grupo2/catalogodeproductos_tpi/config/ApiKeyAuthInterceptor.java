package grupo2.catalogodeproductos_tpi.config;

import grupo2.catalogodeproductos_tpi.client.UsuarioClient;
import grupo2.catalogodeproductos_tpi.exception.ApiKeyInvalidaException;
import grupo2.catalogodeproductos_tpi.exception.ApiKeySinPermisoException;
import grupo2.catalogodeproductos_tpi.exception.UsuariosNoDisponibleException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor de Spring (Guardia de Seguridad) que se ejecuta antes de cada petición.
 * Se encarga de validar el X-API-Key para operaciones de escritura (POST, PATCH, DELETE).
 *
 * Cuando rechaza una petición no escribe la respuesta: lanza una excepción y
 * la responde GlobalExceptionHandler, así la forma del error sale de un solo lugar.
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
     * @return true si la petición puede continuar.
     * @throws ApiKeyInvalidaException Si falta la key o Usuarios la rechaza (401).
     * @throws ApiKeySinPermisoException Si la key es válida pero no tiene permiso (403).
     * @throws UsuariosNoDisponibleException Si Usuarios no respondió (503).
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

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

        if (apiKey == null || apiKey.trim().isEmpty()) {
            // Error 401: No se proporcionó la Key. No se llama a Usuarios.
            throw new ApiKeyInvalidaException();
        }

        // Validamos la Key contra el Módulo 1
        return switch (usuarioClient.validarToken(apiKey)) {
            case VALIDA -> true;
            case INVALIDA -> throw new ApiKeyInvalidaException();
            case SIN_PERMISO -> throw new ApiKeySinPermisoException();
            case NO_DISPONIBLE -> throw new UsuariosNoDisponibleException();
        };
    }

}
