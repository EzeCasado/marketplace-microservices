package grupo2.catalogodeproductos_tpi.controller;

import grupo2.catalogodeproductos_tpi.dto.CreateProductoDTO;
import grupo2.catalogodeproductos_tpi.dto.ProductoResponseDTO;
import grupo2.catalogodeproductos_tpi.dto.UpdateProductoDTO;
import grupo2.catalogodeproductos_tpi.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para los endpoints de Productos.
 *
 * @RestController
 * @RequestMapping("/products") Define la URL base (http://localhost:8080/products)
 * @RequiredArgsConstructor Inyecta ProductoService.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> createProduct(@Valid @RequestBody CreateProductoDTO createDto) {
        ProductoResponseDTO nuevoProducto = productoService.crearProducto(createDto);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    @GetMapping("/{sku}")
    public ResponseEntity<ProductoResponseDTO> getProductBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productoService.obtenerPorSku(sku));
    }

    @PatchMapping("/{sku}")
    public ResponseEntity<ProductoResponseDTO> updateProduct(
            @PathVariable String sku,
            @Valid @RequestBody UpdateProductoDTO updateDto) {
        
        ProductoResponseDTO productoActualizado = productoService.actualizarProducto(sku, updateDto);
        return ResponseEntity.ok(productoActualizado);
    }

    /**
     * [Endpoint 2.X] DELETE /products/{sku}
     * Realiza una baja lógica del producto (activo = false).
     */
    @DeleteMapping("/{sku}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String sku) {
        productoService.darDeBajaProducto(sku); // Llamamos a la lógica de Germán
        return ResponseEntity.noContent().build();
    }

    // Dentro de la clase ProductoController.java

    /**
     * [Endpoint 2.2] GET /products (Búsqueda y Filtrado)
     * Obtiene una lista de todos los productos, con filtros opcionales.
     *
     * @param nombre Filtro opcional por nombre (búsqueda parcial).
     * @param categoriaId Filtro opcional por ID de categoría (búsqueda exacta).
     * @return ResponseEntity con la lista de ProductoResponseDTO (200 OK).
     */
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> searchProducts(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long categoriaId) {

        List<ProductoResponseDTO> productos = productoService.buscarProductos(nombre, categoriaId);
        return ResponseEntity.ok(productos);
    }
}