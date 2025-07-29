package com.linktic.inventario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para solicitudes de actualización de inventario en formato JSON API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioUpdateRequest {

    @JsonProperty("data")
    private InventarioUpdateData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventarioUpdateData {
        @JsonProperty("type")
        private String type = "inventario";

        @JsonProperty("id")
        private String id;

        @JsonProperty("attributes")
        private InventarioUpdateAttributes attributes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventarioUpdateAttributes {
        @JsonProperty("cantidad")
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 0, message = "La cantidad no puede ser negativa")
        private Integer cantidad;
    }
} 