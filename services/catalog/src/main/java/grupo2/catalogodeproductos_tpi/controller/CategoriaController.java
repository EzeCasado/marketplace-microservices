package grupo2.catalogodeproductos_tpi.controller;

import grupo2.catalogodeproductos_tpi.dto.CategoriaDTO;
import grupo2.catalogodeproductos_tpi.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para los endpoints de Categorías.
 *
 * @RestController Combina @Controller y @ResponseBody. Le dice a Spring
 * que esta clase maneja peticiones HTTP y que las respuestas
 * deben ser convertidas a JSON automáticamente.
 *
 * @RequestMapping("/categories") Define la URL base para todos
 * los endpoints en esta clase. (Ej. http://localhost:8080/categories)
 *
 * @RequiredArgsConstructor (Lombok) Inyecta CategoriaService en el constructor.
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    /**
     * [Endpoint 2.6] GET /categories
     * Obtiene una lista de todas las categorías.
     *
     * @GetMapping Define que este método maneja peticiones GET
     * a la URL base ("/categories").
     * @return Un ResponseEntity que contiene la lista de CategoriaDTO
     * y un código de estado 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> getAllCategories() {
        // 1. Llama al servicio para obtener los datos.
        List<CategoriaDTO> categorias = categoriaService.obtenerTodas();
        
        // 2. Devuelve los datos con el código 200 OK.
        return ResponseEntity.ok(categorias);
    }

    /**
     * [Endpoint 2.7] GET /categories/{categoryId}
     * Obtiene una categoría específica por su ID.
     *
     * @GetMapping("/{categoryId}") Define que maneja peticiones GET a
     * "/categories/ALGO", donde "ALGO" es una variable.
     *
     * @param categoryId El valor que viene en la URL, capturado por @PathVariable.
     * @return Un ResponseEntity con la CategoriaDTO encontrada (200 OK).
     * Si no se encuentra, CategoriaService lanzará ResourceNotFoundException,
     * que será manejada por GlobalExceptionHandler (devolviendo 404).
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoriaDTO> getCategoryById(@PathVariable Long categoryId) {
        CategoriaDTO categoria = categoriaService.obtenerPorId(categoryId);
        return ResponseEntity.ok(categoria);
    }

    /**
     * [Endpoint 2.5] POST /categories
     * Crea una nueva categoría.
     *
     * @PostMapping Define que maneja peticiones POST a "/categories".
     *
     * @param categoriaDTO El JSON que viene en el cuerpo (body) de la petición,
     * capturado por @RequestBody.
     *
     * @Valid Activa las validaciones definidas en CategoriaDTO
     * (ej. @NotBlank). Si fallan, GlobalExceptionHandler
     * devolverá un error 400 (Bad Request).
     *
     * @return Un ResponseEntity con la CategoriaDTO recién creada
     * y un código de estado 201 (Created).
     */
    @PostMapping
    public ResponseEntity<CategoriaDTO> createCategory(@Valid @RequestBody CategoriaDTO categoriaDTO) {
        CategoriaDTO nuevaCategoria = categoriaService.crearCategoria(categoriaDTO);
        
        // Devolvemos 201 Created (la mejor práctica para POST)
        return new ResponseEntity<>(nuevaCategoria, HttpStatus.CREATED);
    }
}