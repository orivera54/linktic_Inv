package com.linktic.inventario.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO que representa un producto obtenido del microservicio de productos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("precio")
    private BigDecimal precio;

    @JsonProperty("categoria")
    private String categoria;

    @JsonProperty("activo")
    private Boolean activo;

    @JsonProperty("fecha_creacion")
    private LocalDateTime fechaCreacion;

    @JsonProperty("fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Método para validar que el producto esté activo
    public boolean isActivo() {
        return activo != null && activo;
    }

    // Método para obtener el nombre formateado
    public String getNombreFormateado() {
        return nombre != null ? nombre.trim() : "";
    }

    // Método para validar que el precio sea válido
    public boolean isPrecioValido() {
        return precio != null && precio.compareTo(BigDecimal.ZERO) >= 0;
    }
} 