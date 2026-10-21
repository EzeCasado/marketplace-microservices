package grupo2.catalogodeproductos_tpi.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos", indexes = {
    @Index(name = "idx_sku", columnList = "sku", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    // --- NUEVO: Peso en Kilogramos ---
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal peso;

    @Column(length = 50)
    private String dimensiones;

    // --- NUEVO: Estado para Baja Lógica ---
    // true = Visible / false = Dado de baja
    @Column(nullable = false)
    private Boolean activo = true; 

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.activo == null) {
            this.activo = true; // Por defecto nace activo
        }
    }
}