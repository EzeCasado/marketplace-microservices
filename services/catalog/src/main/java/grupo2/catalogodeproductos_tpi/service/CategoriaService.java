package grupo2.catalogodeproductos_tpi.service;

import grupo2.catalogodeproductos_tpi.dto.CategoriaDTO;
import grupo2.catalogodeproductos_tpi.dto.ListaCategoriasDTO;
import grupo2.catalogodeproductos_tpi.exception.CategoriaDuplicadaException;
import grupo2.catalogodeproductos_tpi.exception.ResourceNotFoundException;
import grupo2.catalogodeproductos_tpi.mapper.CategoriaMapper;
import grupo2.catalogodeproductos_tpi.model.Categoria;
import grupo2.catalogodeproductos_tpi.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
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
        // El id lo elige el cliente: si ya existe, es un conflicto (no se pisa la categoría)
        if (categoriaRepository.existsById(dto.getId())) {
            throw new CategoriaDuplicadaException("Ya existe una categoría con el id " + dto.getId());
        }
        // El nombre también es único
        if (categoriaRepository.existsByNombre(dto.getNombre())) {
            throw new CategoriaDuplicadaException("Ya existe una categoría con el nombre " + dto.getNombre());
        }

        // Mapeamos DTO a Entidad
        Categoria categoria = categoriaMapper.toCategoria(dto);

        // Guardamos. Categoria es Persistable (siempre "nueva" hasta que se guarda),
        // así que save() inserta y nunca hace merge. Con saveAndFlush el insert se
        // hace acá y no al final de la transacción: si dos altas pasaron los chequeos
        // de arriba al mismo tiempo, la base rechaza la segunda y el error salta en
        // este método.
        Categoria categoriaGuardada;
        try {
            categoriaGuardada = categoriaRepository.saveAndFlush(categoria);
        } catch (DataIntegrityViolationException e) {
            throw new CategoriaDuplicadaException("Ya existe una categoría con ese id o con ese nombre", e);
        }

        // Mapeamos Entidad a DTO y devolvemos
        return categoriaMapper.toCategoriaDTO(categoriaGuardada);
    }

    /**
     * [Endpoint 2.6] GET /categories
     */
    @Transactional(readOnly = true)
    public ListaCategoriasDTO obtenerTodas() {
        // El orden lo hace la base (ORDER BY nombre): compara según la intercalación
        // de la columna, que no distingue mayúsculas ni tildes.
        List<Categoria> categorias = categoriaRepository.findAll(Sort.by("nombre"));
        return new ListaCategoriasDTO(categoriaMapper.toCategoriaDTOList(categorias));
    }

    /**
     * [Endpoint 2.7] GET /categories/{categoryId}
     */
    @Transactional(readOnly = true)
    public CategoriaDTO obtenerPorId(String categoryId) {
        Categoria categoria = findByIdOrThrow(categoryId);
        return categoriaMapper.toCategoriaDTO(categoria);
    }

    /**
     * Busca una categoría por su id. Si no está, lanza ResourceNotFoundException,
     * que GlobalExceptionHandler devuelve como 404. Hoy la usa obtenerPorId.
     */
    @Transactional(readOnly = true)
    public Categoria findByIdOrThrow(String id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
    }
}