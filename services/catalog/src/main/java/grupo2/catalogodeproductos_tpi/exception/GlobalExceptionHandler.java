package grupo2.catalogodeproductos_tpi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador Global de Excepciones.
 *
 * @ControllerAdvice le dice a Spring que esta clase es un componente
 * global que "asesora" a todos los Controladores.
 *
 * Su trabajo es interceptar excepciones específicas que ocurran
 * durante la ejecución de un endpoint.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja la excepción ResourceNotFoundException (Error 404).
     *
     * @ExceptionHandler(ResourceNotFoundException.class) le dice a Spring:
     * "Si CUALQUIER controlador lanza una ResourceNotFoundException,
     * ejecuta ESTE método."
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        // 1. Creamos el cuerpo del error con el formato JSON estándar
        Map<String, Object> body = new HashMap<>();
        body.put("error", "RecursoNoEncontrado");
        body.put("message", ex.getMessage()); // El mensaje que pasamos al lanzar la excepción

        // 2. Devolvemos el JSON con el código HTTP 404 (NOT_FOUND)
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja la excepción SkuAlreadyExistsException (Error 409).
     * Atrapa la excepción lanzada por ProductoService.
     */
    @ExceptionHandler(SkuAlreadyExistsException.class)
    public ResponseEntity<Object> handleSkuAlreadyExistsException(
            SkuAlreadyExistsException ex, WebRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Conflicto");
        body.put("message", ex.getMessage());

        // 2. Devolvemos el JSON con el código HTTP 409 (CONFLICT)
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Maneja las excepciones de validación (Error 400).
     *
     * Esta excepción (MethodArgumentNotValidException) la lanza Spring
     * automáticamente cuando un DTO marcado con @Valid falla sus
     * validaciones (ej. @NotBlank, @Positive).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        // Recolectamos todos los mensajes de error de validación en un solo string
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        Map<String, Object> body = new HashMap<>();
        body.put("error", "PeticionInvalida");
        body.put("message", "Error de validación: " + errors);

        // 2. Devolvemos el JSON con el código HTTP 400 (BAD_REQUEST)
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Manejador "Atrapa-Todo" para cualquier excepción no controlada (Error 500).
     *
     * @ExceptionHandler(Exception.class) atrapa CUALQUIER otra excepción
     * que no haya sido manejada por los métodos de arriba
     * (ej. un NullPointerException inesperado).
     *
     * Es la red de seguridad final.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(
            Exception ex, WebRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("error", "ErrorInternoDelServidor");
        body.put("message", "Ocurrió un error inesperado: " + ex.getMessage());

        // 2. Devolvemos el JSON con el código HTTP 500 (INTERNAL_SERVER_ERROR)
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}