package com.linktic.inventario.controller;

import com.linktic.inventario.dto.InventarioResponse;
import com.linktic.inventario.dto.InventarioUpdateRequest;
import com.linktic.inventario.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador REST para la gestión de inventario con JSON API
 */
@RestController
@RequestMapping("/inventario")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Inventario", description = "API para gestión de inventario de productos")
public class InventarioController {

    private final InventarioService inventarioService;

    /**
     * Consultar la cantidad disponible de un producto específico por ID
     */
    @GetMapping(value = "/{productoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Consultar cantidad disponible",
        description = "Obtiene la cantidad disponible de un producto específico por ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventario encontrado",
            content = @Content(schema = @Schema(implementation = InventarioResponse.class))),
        @ApiResponse(responseCode = "404", description = "Producto o inventario no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<InventarioResponse> consultarCantidadDisponible(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable @NotNull @Min(1) Integer productoId) {
        
        log.info("Solicitud de consulta de cantidad disponible para producto: {}", productoId);
        
        try {
            InventarioResponse response = inventarioService.consultarCantidadDisponible(productoId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error al consultar cantidad disponible: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error interno al consultar cantidad disponible: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualizar la cantidad disponible de un producto
     */
    @PatchMapping(value = "/{productoId}", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Actualizar cantidad disponible",
        description = "Actualiza la cantidad disponible de un producto específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cantidad actualizada exitosamente",
            content = @Content(schema = @Schema(implementation = InventarioResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<InventarioResponse> actualizarCantidad(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable @NotNull @Min(1) Integer productoId,
            @Parameter(description = "Datos de actualización", required = true)
            @Valid @RequestBody InventarioUpdateRequest request) {
        
        log.info("Solicitud de actualización de cantidad para producto: {} - Nueva cantidad: {}", 
                productoId, request.getData().getAttributes().getCantidad());
        
        try {
            Integer nuevaCantidad = request.getData().getAttributes().getCantidad();
            InventarioResponse response = inventarioService.actualizarCantidad(productoId, nuevaCantidad);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al actualizar cantidad: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error al actualizar cantidad: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error interno al actualizar cantidad: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Incrementar la cantidad de un producto (compra)
     */
    @PostMapping(value = "/{productoId}/incrementar", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Incrementar cantidad (compra)",
        description = "Incrementa la cantidad disponible de un producto (operación de compra)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cantidad incrementada exitosamente",
            content = @Content(schema = @Schema(implementation = InventarioResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<InventarioResponse> incrementarCantidad(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable @NotNull @Min(1) Integer productoId,
            @Parameter(description = "Cantidad a incrementar", required = true)
            @RequestParam @NotNull @Min(1) Integer cantidad,
            @Parameter(description = "Precio unitario")
            @RequestParam(required = false) BigDecimal precioUnitario) {
        
        log.info("Solicitud de incremento de cantidad para producto: {} - Cantidad: {} - Precio: {}", 
                productoId, cantidad, precioUnitario);
        
        try {
            InventarioResponse response = inventarioService.incrementarCantidad(productoId, cantidad, precioUnitario);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al incrementar cantidad: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error al incrementar cantidad: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error interno al incrementar cantidad: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Decrementar la cantidad de un producto (venta)
     */
    @PostMapping(value = "/{productoId}/decrementar", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Decrementar cantidad (venta)",
        description = "Decrementa la cantidad disponible de un producto (operación de venta)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cantidad decrementada exitosamente",
            content = @Content(schema = @Schema(implementation = InventarioResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o stock insuficiente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<InventarioResponse> decrementarCantidad(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable @NotNull @Min(1) Integer productoId,
            @Parameter(description = "Cantidad a decrementar", required = true)
            @RequestParam @NotNull @Min(1) Integer cantidad,
            @Parameter(description = "Precio unitario")
            @RequestParam(required = false) BigDecimal precioUnitario) {
        
        log.info("Solicitud de decremento de cantidad para producto: {} - Cantidad: {} - Precio: {}", 
                productoId, cantidad, precioUnitario);
        
        try {
            InventarioResponse response = inventarioService.decrementarCantidad(productoId, cantidad, precioUnitario);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al decrementar cantidad: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error al decrementar cantidad: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error interno al decrementar cantidad: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener productos con stock bajo
     */
    @GetMapping(value = "/stock-bajo", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Productos con stock bajo",
        description = "Obtiene la lista de productos con stock bajo"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<InventarioResponse>> getProductosConStockBajo(
            @Parameter(description = "Cantidad mínima para considerar stock bajo")
            @RequestParam(defaultValue = "10") @Min(0) Integer cantidadMinima) {
        
        log.info("Solicitud de productos con stock bajo (menos de {} unidades)", cantidadMinima);
        
        try {
            List<InventarioResponse> response = inventarioService.getProductosConStockBajo(cantidadMinima);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error interno al obtener productos con stock bajo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener productos sin stock
     */
    @GetMapping(value = "/sin-stock", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Productos sin stock",
        description = "Obtiene la lista de productos sin stock disponible"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<InventarioResponse>> getProductosSinStock() {
        log.info("Solicitud de productos sin stock");
        
        try {
            List<InventarioResponse> response = inventarioService.getProductosSinStock();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error interno al obtener productos sin stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener estadísticas del inventario
     */
    @GetMapping(value = "/estadisticas", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Estadísticas del inventario",
        description = "Obtiene estadísticas generales del inventario"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<InventarioService.InventarioStats> getEstadisticasInventario() {
        log.info("Solicitud de estadísticas del inventario");
        
        try {
            InventarioService.InventarioStats stats = inventarioService.getEstadisticasInventario();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error interno al obtener estadísticas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Health check",
        description = "Verifica el estado del servicio de inventario"
    )
    public ResponseEntity<Object> healthCheck() {
        return ResponseEntity.ok(new Object() {
            public final String status = "UP";
            public final String service = "inventario-service";
            public final String timestamp = java.time.LocalDateTime.now().toString();
        });
    }
}