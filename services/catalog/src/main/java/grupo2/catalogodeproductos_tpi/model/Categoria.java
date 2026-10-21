package grupo2.catalogodeproductos_tpi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    // 'mappedBy' indica que la Entidad 'Producto' es la dueña de esta relación
    // (allí está el @ManyToOne).
    // 'cascade = CascadeType.ALL' es opcional, significa que si borras una Categoria,
    // se borran todos sus productos. Puede ser peligroso, usar con cuidado.
    // 'fetch = FetchType.LAZY' es para performance, no trae los productos 
    // a menos que se pidan explicitamente.
    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Producto> productos;
}