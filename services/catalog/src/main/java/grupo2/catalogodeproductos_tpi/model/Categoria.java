package grupo2.catalogodeproductos_tpi.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Persistable;

import java.util.List;

/**
 * Categoría de productos.
 *
 * El id lo elige quien crea la categoría (ej. "cat-libros"), no lo genera la base.
 * Por eso la clase implementa Persistable: con un id que no es null, Spring Data
 * cree que la entidad ya existe y save() haría un merge, pisando la categoría
 * que estaba. Con isNew() devolviendo true hasta que se guarda o se carga,
 * save() hace siempre un insert.
 */
@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria implements Persistable<String> {

    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false, length = 100, unique = true)
    private String nombre;

    // 'mappedBy' indica que la Entidad 'Producto' es la dueña de esta relación
    // (allí está el @ManyToOne).
    // 'cascade = CascadeType.ALL' es opcional, significa que si borras una Categoria,
    // se borran todos sus productos. Puede ser peligroso, usar con cuidado.
    // 'fetch = FetchType.LAZY' es para performance, no trae los productos
    // a menos que se pidan explicitamente.
    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Producto> productos;

    /**
     * No se guarda en la base: arranca en true y se apaga cuando la entidad
     * se guarda (@PostPersist) o se carga (@PostLoad). Lo lee isNew().
     */
    @Transient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private boolean nuevo = true;

    @Override
    public boolean isNew() {
        return nuevo;
    }

    @PostPersist
    @PostLoad
    void marcarComoGuardada() {
        this.nuevo = false;
    }
}
