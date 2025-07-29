package com.linktic.inventario.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad para registrar el historial de compras (funcionalidad opcional)
 */
@Entity
@Table(name = "historial_compras")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Integer productoId;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "precio_total", precision = 10, scale = 2)
    private BigDecimal precioTotal;

    @Column(name = "tipo_operacion", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoOperacion tipoOperacion;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "observaciones")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_operacion", nullable = false, updatable = false)
    private LocalDateTime fechaOperacion;

    public enum TipoOperacion {
        COMPRA,
        VENTA,
        AJUSTE,
        DEVOLUCION
    }

    // Método para calcular el precio total
    public void calcularPrecioTotal() {
        if (precioUnitario != null && cantidad != null) {
            this.precioTotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        }
    }

    // Método para validar la operación
    public boolean isValid() {
        return productoId != null && 
               cantidad != null && 
               cantidad > 0 && 
               tipoOperacion != null;
    }
} 