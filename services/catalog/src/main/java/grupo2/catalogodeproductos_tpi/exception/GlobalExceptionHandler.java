package grupo2.catalogodeproductos_tpi.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Manejador Global de Excepciones.
 *
 * @ControllerAdvice le dice a Spring que esta clase es un componente
 * global que "asesora" a todos los Controladores.
 *
 * Su trabajo es interceptar excepciones específicas que ocurran
 * durante la ejecución de un endpoint.
 *
 * Extiende ResponseEntityExceptionHandler, que ya sabe qué código HTTP le
 * corresponde a cada excepción de Spring MVC (método no soportado, JSON roto,
 * ruta inexistente, tipo de parámetro equivocado, etc.). Lo único que cambia
 * acá es la forma de la respuesta: en vez del ProblemDetail de Spring,
 * { "error": "...", "message": "..." }, como pide el contrato
 * (docs/api/catalog.yaml).
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Mensaje fijo de los errores 500: nunca lleva detalles internos.
     * Lo que haga falta para entender el error va al log.
     */
    private static final String MENSAJE_ERROR_INESPERADO = "Ocurrió un error inesperado";

    /**
     * Códigos de las anotaciones de validación que significan "el campo falta"
     * (FieldError.getCode() devuelve el nombre de la anotación).
     */
    private static final Set<String> CODIGOS_DE_CAMPO_FALTANTE = Set.of("NotNull", "NotBlank", "NotEmpty");

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
        //    (el mensaje es el que pasamos al lanzar la excepción)
        Map<String, Object> body = cuerpo("RecursoNoEncontrado", ex.getMessage());

        // 2. Devolvemos el JSON con el código HTTP 404 (NOT_FOUND)
        return createResponseEntity(body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    /**
     * Maneja la excepción SkuAlreadyExistsException (Error 409).
     * Atrapa la excepción lanzada por ProductoService.
     */
    @ExceptionHandler(SkuAlreadyExistsException.class)
    public ResponseEntity<Object> handleSkuAlreadyExistsException(
            SkuAlreadyExistsException ex, WebRequest request) {

        Map<String, Object> body = cuerpo("Conflicto", ex.getMessage());

        // 2. Devolvemos el JSON con el código HTTP 409 (CONFLICT)
        return createResponseEntity(body, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    /**
     * Maneja la excepción CategoriaDuplicadaException (Error 409).
     * Atrapa la excepción lanzada por CategoriaService.
     */
    @ExceptionHandler(CategoriaDuplicadaException.class)
    public ResponseEntity<Object> handleCategoriaDuplicadaException(
            CategoriaDuplicadaException ex, WebRequest request) {

        Map<String, Object> body = cuerpo("Conflicto", ex.getMessage());

        return createResponseEntity(body, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    /**
     * Maneja la excepción ApiKeyInvalidaException (Error 401).
     * La lanza ApiKeyAuthInterceptor cuando falta la X-API-Key o Usuarios la rechaza.
     */
    @ExceptionHandler(ApiKeyInvalidaException.class)
    public ResponseEntity<Object> handleApiKeyInvalidaException(
            ApiKeyInvalidaException ex, WebRequest request) {

        Map<String, Object> body = cuerpo("NoAutorizado", ex.getMessage());

        return createResponseEntity(body, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    /**
     * Maneja la excepción ApiKeySinPermisoException (Error 403).
     * La lanza ApiKeyAuthInterceptor cuando la X-API-Key es válida pero no tiene permiso.
     */
    @ExceptionHandler(ApiKeySinPermisoException.class)
    public ResponseEntity<Object> handleApiKeySinPermisoException(
            ApiKeySinPermisoException ex, WebRequest request) {

        Map<String, Object> body = cuerpo("SinPermiso", ex.getMessage());

        return createResponseEntity(body, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    /**
     * Maneja la excepción UsuariosNoDisponibleException (Error 503).
     * La lanza ApiKeyAuthInterceptor cuando Usuarios no respondió y no se pudo validar la key.
     *
     * No pasa por handleExceptionInternal: ahí cualquier 5xx se loguea como error
     * con su stack trace, y la falla de Usuarios ya la loguea UsuarioClient.
     */
    @ExceptionHandler(UsuariosNoDisponibleException.class)
    public ResponseEntity<Object> handleUsuariosNoDisponibleException(
            UsuariosNoDisponibleException ex, WebRequest request) {

        Map<String, Object> body = cuerpo("ServicioNoDisponible", ex.getMessage());

        return createResponseEntity(body, new HttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE, request);
    }

    /**
     * Maneja las excepciones de validación (Error 422 o 400).
     *
     * Esta excepción (MethodArgumentNotValidException) la lanza Spring
     * automáticamente cuando un DTO marcado con @Valid falla sus
     * validaciones (ej. @NotBlank, @Positive).
     *
     * Sobrescribe el método de ResponseEntityExceptionHandler en vez de usar
     * un @ExceptionHandler propio: con los dos, Spring no arranca porque la
     * excepción tendría dos manejadores.
     *
     * Reglas del contrato:
     * - Si falta algún campo (@NotNull, @NotBlank o @NotEmpty), 422 con la
     *   lista de los que faltan. Los valores inválidos no se mencionan: se ven
     *   cuando el cliente completa lo que falta.
     * - Si no falta ninguno, 400 con "campo: mensaje" por cada error.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        List<FieldError> errores = ex.getBindingResult().getFieldErrors();

        // Un campo puede fallar dos validaciones a la vez (ej. @NotBlank y @Size):
        // el TreeSet saca los repetidos y los deja en orden alfabético, para que
        // el texto no cambie entre corridas.
        Set<String> camposQueFaltan = errores.stream()
                .filter(error -> error.getCode() != null && CODIGOS_DE_CAMPO_FALTANTE.contains(error.getCode()))
                .map(FieldError::getField)
                .collect(Collectors.toCollection(TreeSet::new));

        HttpStatus nuevoStatus;
        String message;

        if (!camposQueFaltan.isEmpty()) {
            nuevoStatus = HttpStatus.UNPROCESSABLE_CONTENT;
            message = "Faltan campos obligatorios: " + String.join(", ", camposQueFaltan);
        } else {
            nuevoStatus = HttpStatus.BAD_REQUEST;
            // Ordenados por el texto "campo: mensaje": el orden en que Spring entrega
            // los errores no está garantizado.
            message = errores.stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .sorted()
                    .collect(Collectors.joining(", "));
        }

        // Se llama a la implementación de la clase base y no a la de esta clase,
        // porque el cuerpo ya está armado.
        return super.handleExceptionInternal(
                ex, cuerpo(errorDe(nuevoStatus), message), headers, nuevoStatus, request);
    }

    /**
     * Punto por el que pasan todas las excepciones de Spring MVC que maneja
     * ResponseEntityExceptionHandler (405, 406, 415, 404 de rutas, 400 de JSON
     * roto o de tipo de parámetro, etc.).
     *
     * Acá se reemplaza el ProblemDetail de Spring por { "error", "message" }.
     * El código HTTP lo decide Spring; el "error" sale de ese código y el
     * "message" lo armamos a mano según la excepción: nunca es el texto de la
     * excepción.
     *
     * Cualquier 5xx se loguea con su stack trace completo, porque la respuesta
     * no dice qué pasó.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {

        if (statusCode.is5xxServerError()) {
            log.error("Error inesperado en {}", request.getDescription(false), ex);
        }

        Map<String, Object> cuerpo = cuerpo(errorDe(statusCode), mensajeDe(ex, statusCode));

        return super.handleExceptionInternal(ex, cuerpo, headers, statusCode, request);
    }

    /**
     * Último paso de la clase base antes de devolver la respuesta, por el que
     * pasan todas las respuestas de error de esta clase.
     *
     * Fija Content-Type: application/json. Así Spring no negocia el formato con
     * el Accept del cliente, y el error sale siempre como { "error", "message" }
     * aunque el cliente pida otra cosa (ej. Accept: application/xml en un 406).
     */
    @Override
    protected ResponseEntity<Object> createResponseEntity(
            Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

        return ResponseEntity.status(statusCode)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    /**
     * Manejador "Atrapa-Todo" para cualquier excepción no controlada (Error 500).
     *
     * @ExceptionHandler(Exception.class) atrapa CUALQUIER otra excepción
     * que no haya sido manejada por los métodos de arriba
     * (ej. un NullPointerException inesperado). Las de Spring MVC las atiende
     * ResponseEntityExceptionHandler, que es más específico.
     *
     * Es la red de seguridad final. La respuesta lleva un mensaje fijo y la
     * excepción va al log (lo hace handleExceptionInternal).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(
            Exception ex, WebRequest request) {

        return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * Arma el cuerpo { "error", "message" }. Es un LinkedHashMap para que el
     * JSON salga siempre con "error" primero.
     */
    private static Map<String, Object> cuerpo(String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", error);
        body.put("message", message);
        return body;
    }

    /**
     * El "error" del contrato para cada código HTTP (tabla "Códigos de error"
     * de docs/api/catalog.yaml).
     *
     * Si Spring da un código que el contrato no tiene (ej. 413 por un
     * multipart demasiado grande), se mantiene el código y el "error" sale de
     * su serie: PeticionInvalida para un 4xx y ErrorInternoDelServidor para un 5xx.
     */
    private static String errorDe(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "PeticionInvalida";
            case 404 -> "RecursoNoEncontrado";
            case 405 -> "MetodoNoPermitido";
            case 406 -> "FormatoNoAceptable";
            case 415 -> "TipoDeContenidoNoSoportado";
            case 422 -> "FaltanCamposObligatorios";
            default -> status.is5xxServerError() ? "ErrorInternoDelServidor" : "PeticionInvalida";
        };
    }

    /**
     * El "message" de cada excepción de Spring, en español y armado a mano.
     * Solo usa datos de la petición (el método HTTP, el nombre de un
     * parámetro), nunca el texto de la excepción.
     */
    private static String mensajeDe(Exception ex, HttpStatusCode status) {
        return switch (ex) {
            case HttpRequestMethodNotSupportedException e -> "La ruta no acepta el método " + e.getMethod();
            case HttpMediaTypeNotSupportedException e -> "El body tiene que ser application/json";
            case HttpMediaTypeNotAcceptableException e -> "Solo se puede responder en application/json";
            case NoResourceFoundException e -> "La ruta no existe";
            case NoHandlerFoundException e -> "La ruta no existe";
            case HttpMessageNotReadableException e -> "El body no es un JSON válido";
            case MethodArgumentTypeMismatchException e -> "El parámetro " + e.getName() + " tiene un valor inválido";
            case MissingServletRequestParameterException e -> "Falta el parámetro " + e.getParameterName();
            case HandlerMethodValidationException e -> mensajeDeValidacion(e);
            default -> status.is5xxServerError() ? MENSAJE_ERROR_INESPERADO : "La petición no es válida";
        };
    }

    /**
     * El "message" de un parámetro de la ruta que no cumple sus restricciones
     * (ej. un categoryId con mayúsculas): nombra el parámetro, no el error de validación.
     */
    private static String mensajeDeValidacion(HandlerMethodValidationException ex) {
        String nombres = ex.getParameterValidationResults().stream()
                .map(resultado -> resultado.getMethodParameter().getParameterName())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(", "));

        return nombres.isEmpty()
                ? "La petición no es válida"
                : "El parámetro " + nombres + " tiene un valor inválido";
    }
}
