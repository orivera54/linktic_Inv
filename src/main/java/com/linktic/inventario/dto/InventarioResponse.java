package com.linktic.inventario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO para respuestas JSON API de inventario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioResponse {

    @JsonProperty("data")
    private InventarioData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventarioData {
        @JsonProperty("type")
        private String type = "inventario";

        @JsonProperty("id")
        private String id;

        @JsonProperty("attributes")
        private InventarioAttributes attributes;

        @JsonProperty("relationships")
        private InventarioRelationships relationships;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventarioAttributes {
        @JsonProperty("cantidad")
        private Integer cantidad;

        @JsonProperty("fecha_creacion")
        private LocalDateTime fechaCreacion;

        @JsonProperty("fecha_actualizacion")
        private LocalDateTime fechaActualizacion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventarioRelationships {
        @JsonProperty("producto")
        private ProductoRelationship producto;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductoRelationship {
        @JsonProperty("data")
        private ProductoReference data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductoReference {
        @JsonProperty("type")
        private String type = "productos";

        @JsonProperty("id")
        private String id;
    }
} 