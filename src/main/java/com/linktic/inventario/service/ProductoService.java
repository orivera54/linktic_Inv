package com.linktic.inventario.service;

import com.linktic.inventario.model.Producto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * Servicio para comunicarse con el microservicio de productos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoService {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.productos-service.base-url}")
    private String baseUrl;

    @Value("${app.productos-service.api-key}")
    private String apiKey;

    @Value("${app.productos-service.timeout}")
    private int timeout;

    /**
     * Obtener un producto por ID
     */
    @CircuitBreaker(name = "productos-service", fallbackMethod = "getProductoFallback")
    @Retry(name = "productos-service")
    public Mono<Producto> getProductoById(Integer productoId) {
        log.info("Consultando producto con ID: {}", productoId);
        
        return webClientBuilder
                .baseUrl(baseUrl)
                .build()
                .get()
                .uri("/api/v1/products/{id}", productoId)
                .header(HttpHeaders.ACCEPT, "application/vnd.api+json")
                .header("X-API-Key", apiKey)
                .retrieve()
                .bodyToMono(ProductoResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .map(this::mapToProducto)
                .doOnSuccess(producto -> log.info("Producto obtenido exitosamente: {}", producto.getNombre()))
                .doOnError(error -> log.error("Error al obtener producto con ID {}: {}", productoId, error.getMessage()));
    }

    /**
     * Verificar si un producto existe
     */
    @CircuitBreaker(name = "productos-service", fallbackMethod = "productoExistsFallback")
    @Retry(name = "productos-service")
    public Mono<Boolean> productoExists(Integer productoId) {
        log.info("Verificando existencia del producto con ID: {}", productoId);
        
        return webClientBuilder
                .baseUrl(baseUrl)
                .build()
                .get()
                .uri("/api/v1/products/{id}/exists", productoId)
                .header(HttpHeaders.ACCEPT, "application/vnd.api+json")
                .header("X-API-Key", apiKey)
                .retrieve()
                .bodyToMono(ProductoExistsResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .map(ProductoExistsResponse::isExists)
                .doOnSuccess(exists -> log.info("Producto {} existe: {}", productoId, exists))
                .doOnError(error -> log.error("Error al verificar existencia del producto {}: {}", productoId, error.getMessage()));
    }

    /**
     * Obtener productos por lotes
     */
    @CircuitBreaker(name = "productos-service", fallbackMethod = "getProductosBatchFallback")
    @Retry(name = "productos-service")
    public Mono<List<Producto>> getProductosBatch(List<Integer> productoIds) {
        log.info("Consultando productos por lotes: {}", productoIds);
        
        String idsParam = String.join(",", productoIds.stream().map(String::valueOf).toList());
        
        return webClientBuilder
                .baseUrl(baseUrl)
                .build()
                .get()
                .uri("/api/v1/products/batch?ids={ids}", idsParam)
                .header(HttpHeaders.ACCEPT, "application/vnd.api+json")
                .header("X-API-Key", apiKey)
                .retrieve()
                .bodyToMono(ProductosBatchResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .map(this::mapToProductosList)
                .doOnSuccess(productos -> log.info("Productos obtenidos exitosamente: {}", productos.size()))
                .doOnError(error -> log.error("Error al obtener productos por lotes: {}", error.getMessage()));
    }

    // Métodos de fallback para circuit breaker
    public Mono<Producto> getProductoFallback(Integer productoId, Exception ex) {
        log.warn("Fallback: No se pudo obtener el producto {} debido a: {}", productoId, ex.getMessage());
        return Mono.error(new RuntimeException("Servicio de productos no disponible"));
    }

    public Mono<Boolean> productoExistsFallback(Integer productoId, Exception ex) {
        log.warn("Fallback: No se pudo verificar la existencia del producto {} debido a: {}", productoId, ex.getMessage());
        return Mono.just(false);
    }

    public Mono<List<Producto>> getProductosBatchFallback(List<Integer> productoIds, Exception ex) {
        log.warn("Fallback: No se pudieron obtener los productos {} debido a: {}", productoIds, ex.getMessage());
        return Mono.error(new RuntimeException("Servicio de productos no disponible"));
    }

    // Métodos auxiliares para mapeo de respuestas
    private Producto mapToProducto(ProductoResponse response) {
        if (response != null && response.getData() != null && response.getData().getAttributes() != null) {
            ProductoResponse.ProductoAttributes attrs = response.getData().getAttributes();
            return Producto.builder()
                    .id(Integer.valueOf(response.getData().getId()))
                    .nombre(attrs.getNombre())
                    .descripcion(attrs.getDescripcion())
                    .precio(attrs.getPrecio())
                    .categoria(attrs.getCategoria())
                    .activo(attrs.getActivo())
                    .fechaCreacion(attrs.getFechaCreacion())
                    .fechaActualizacion(attrs.getFechaActualizacion())
                    .build();
        }
        return null;
    }

    private List<Producto> mapToProductosList(ProductosBatchResponse response) {
        if (response != null && response.getData() != null) {
            return response.getData().stream()
                    .map(this::mapToProductoFromData)
                    .toList();
        }
        return List.of();
    }

    private Producto mapToProductoFromData(ProductoResponse.ProductoData data) {
        if (data != null && data.getAttributes() != null) {
            ProductoResponse.ProductoAttributes attrs = data.getAttributes();
            return Producto.builder()
                    .id(Integer.valueOf(data.getId()))
                    .nombre(attrs.getNombre())
                    .descripcion(attrs.getDescripcion())
                    .precio(attrs.getPrecio())
                    .categoria(attrs.getCategoria())
                    .activo(attrs.getActivo())
                    .fechaCreacion(attrs.getFechaCreacion())
                    .fechaActualizacion(attrs.getFechaActualizacion())
                    .build();
        }
        return null;
    }

    // Clases DTO para las respuestas del servicio de productos
    public static class ProductoResponse {
        private ProductoData data;

        public ProductoData getData() { return data; }
        public void setData(ProductoData data) { this.data = data; }

        public static class ProductoData {
            private String id;
            private String type;
            private ProductoAttributes attributes;

            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            public ProductoAttributes getAttributes() { return attributes; }
            public void setAttributes(ProductoAttributes attributes) { this.attributes = attributes; }
        }

        public static class ProductoAttributes {
            private String nombre;
            private String descripcion;
            private java.math.BigDecimal precio;
            private String categoria;
            private Boolean activo;
            private java.time.LocalDateTime fechaCreacion;
            private java.time.LocalDateTime fechaActualizacion;

            // Getters y setters
            public String getNombre() { return nombre; }
            public void setNombre(String nombre) { this.nombre = nombre; }
            public String getDescripcion() { return descripcion; }
            public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
            public java.math.BigDecimal getPrecio() { return precio; }
            public void setPrecio(java.math.BigDecimal precio) { this.precio = precio; }
            public String getCategoria() { return categoria; }
            public void setCategoria(String categoria) { this.categoria = categoria; }
            public Boolean getActivo() { return activo; }
            public void setActivo(Boolean activo) { this.activo = activo; }
            public java.time.LocalDateTime getFechaCreacion() { return fechaCreacion; }
            public void setFechaCreacion(java.time.LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
            public java.time.LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
            public void setFechaActualizacion(java.time.LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
        }
    }

    public static class ProductoExistsResponse {
        private boolean exists;

        public boolean isExists() { return exists; }
        public void setExists(boolean exists) { this.exists = exists; }
    }

    public static class ProductosBatchResponse {
        private List<ProductoResponse.ProductoData> data;

        public List<ProductoResponse.ProductoData> getData() { return data; }
        public void setData(List<ProductoResponse.ProductoData> data) { this.data = data; }
    }
}