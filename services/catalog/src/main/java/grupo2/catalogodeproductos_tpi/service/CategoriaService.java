package grupo2.catalogodeproductos_tpi.service;

import grupo2.catalogodeproductos_tpi.dto.CategoriaDTO;
import grupo2.catalogodeproductos_tpi.exception.ResourceNotFoundException;
import grupo2.catalogodeproductos_tpi.mapper.CategoriaMapper;
import grupo2.catalogodeproductos_tpi.model.Categoria;
import grupo2.catalogodeproductos_tpi.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    /**
     * [Endpoint 2.5] POST /categories
     */
    @Transactional
    public CategoriaDTO crearCategoria(CategoriaDTO dto) {
        // Mapeamos DTO a Entidad
        Categoria categoria = categoriaMapper.toCategoria(dto);
        // Guardamos
        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        // Mapeamos Entidad a DTO y devolvemos
        return categoriaMapper.toCategoriaDTO(categoriaGuardada);
    }

    /**
     * [Endpoint 2.6] GET /categories
     */
    @Transactional(readOnly = true)
    public List<CategoriaDTO> obtenerTodas() {
        List<Categoria> categorias = categoriaRepository.findAll();
        return categoriaMapper.toCategoriaDTOList(categorias);
    }

    /**
     * [Endpoint 2.7] GET /categories/{categoryId}
     */
    @Transactional(readOnly = true)
    public CategoriaDTO obtenerPorId(Long categoryId) {
        Categoria categoria = findByIdOrThrow(categoryId);
        return categoriaMapper.toCategoriaDTO(categoria);
    }

    /**
     * Método auxiliar para que ProductoService pueda encontrar una Categoría.
     */
    @Transactional(readOnly = true)
    public Categoria findByIdOrThrow(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
    }
}