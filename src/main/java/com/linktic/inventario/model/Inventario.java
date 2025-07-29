package com.linktic.inventario.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla Inventario en la base de datos
 */
@Entity
@Table(name = "Inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventario {

    @Id
    @Column(name = "producto_id")
    private Integer productoId;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Version
    @Column(name = "version")
    private Long version;

    // Método para validar que la cantidad no sea negativa
    public boolean isCantidadValida() {
        return cantidad != null && cantidad >= 0;
    }

    // Método para actualizar la cantidad
    public void actualizarCantidad(Integer nuevaCantidad) {
        if (nuevaCantidad != null && nuevaCantidad >= 0) {
            this.cantidad = nuevaCantidad;
        } else {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
    }

    // Método para incrementar la cantidad
    public void incrementarCantidad(Integer cantidadIncremento) {
        if (cantidadIncremento != null && cantidadIncremento > 0) {
            this.cantidad += cantidadIncremento;
        } else {
            throw new IllegalArgumentException("El incremento debe ser positivo");
        }
    }

    // Método para decrementar la cantidad
    public void decrementarCantidad(Integer cantidadDecremento) {
        if (cantidadDecremento != null && cantidadDecremento > 0) {
            if (this.cantidad >= cantidadDecremento) {
                this.cantidad -= cantidadDecremento;
            } else {
                throw new IllegalArgumentException("No hay suficiente stock disponible");
            }
        } else {
            throw new IllegalArgumentException("El decremento debe ser positivo");
        }
    }
} 