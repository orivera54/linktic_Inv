package com.linktic.inventario.service;

import com.linktic.inventario.dto.InventarioResponse;
import com.linktic.inventario.model.HistorialCompra;
import com.linktic.inventario.model.Inventario;
import com.linktic.inventario.model.Producto;
import com.linktic.inventario.repository.HistorialCompraRepository;
import com.linktic.inventario.repository.InventarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio principal para la gestión de inventario
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final HistorialCompraRepository historialCompraRepository;
    private final ProductoService productoService;

    /**
     * Consultar la cantidad disponible de un producto específico por ID
     */
    public InventarioResponse consultarCantidadDisponible(Integer productoId) {
        log.info("Consultando cantidad disponible para el producto: {}", productoId);

        // Verificar que el producto existe en el servicio de productos
        productoService.productoExists(productoId)
                .subscribe(exists -> {
                    if (!exists) {
                        throw new RuntimeException("El producto con ID " + productoId + " no existe");
                    }
                });

        // Buscar el inventario del producto
        Optional<Inventario> inventarioOpt = inventarioRepository.findByProductoId(productoId);
        
        if (inventarioOpt.isEmpty()) {
            log.warn("No se encontró inventario para el producto: {}", productoId);
            throw new RuntimeException("No se encontró inventario para el producto con ID " + productoId);
        }

        Inventario inventario = inventarioOpt.get();
        
        // Obtener información del producto
        Producto producto = productoService.getProductoById(productoId).block();
        
        return buildInventarioResponse(inventario, producto);
    }

    /**
     * Actualizar la cantidad disponible de un producto
     */
    public InventarioResponse actualizarCantidad(Integer productoId, Integer nuevaCantidad) {
        log.info("Actualizando cantidad del producto {} a: {}", productoId, nuevaCantidad);

        // Validar que la cantidad no sea negativa
        if (nuevaCantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }

        // Verificar que el producto existe
        productoService.productoExists(productoId)
                .subscribe(exists -> {
                    if (!exists) {
                        throw new RuntimeException("El producto con ID " + productoId + " no existe");
                    }
                });

        // Buscar o crear el inventario
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElse(Inventario.builder()
                        .productoId(productoId)
                        .cantidad(0)
                        .build());

        Integer cantidadAnterior = inventario.getCantidad();
        inventario.actualizarCantidad(nuevaCantidad);
        
        Inventario inventarioGuardado = inventarioRepository.save(inventario);
        
        // Registrar en el historial
        registrarEnHistorial(productoId, cantidadAnterior, nuevaCantidad, "AJUSTE");
        
        // Obtener información del producto
        Producto producto = productoService.getProductoById(productoId).block();
        
        log.info("Cantidad actualizada exitosamente para el producto {}: {} -> {}", 
                productoId, cantidadAnterior, nuevaCantidad);
        
        return buildInventarioResponse(inventarioGuardado, producto);
    }

    /**
     * Incrementar la cantidad de un producto (compra)
     */
    public InventarioResponse incrementarCantidad(Integer productoId, Integer cantidadIncremento, BigDecimal precioUnitario) {
        log.info("Incrementando cantidad del producto {} en: {}", productoId, cantidadIncremento);

        if (cantidadIncremento <= 0) {
            throw new IllegalArgumentException("La cantidad de incremento debe ser positiva");
        }

        // Verificar que el producto existe
        productoService.productoExists(productoId)
                .subscribe(exists -> {
                    if (!exists) {
                        throw new RuntimeException("El producto con ID " + productoId + " no existe");
                    }
                });

        // Buscar o crear el inventario
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElse(Inventario.builder()
                        .productoId(productoId)
                        .cantidad(0)
                        .build());

        Integer cantidadAnterior = inventario.getCantidad();
        inventario.incrementarCantidad(cantidadIncremento);
        
        Inventario inventarioGuardado = inventarioRepository.save(inventario);
        
        // Registrar en el historial
        registrarEnHistorial(productoId, cantidadIncremento, precioUnitario, "COMPRA");
        
        // Obtener información del producto
        Producto producto = productoService.getProductoById(productoId).block();
        
        log.info("Cantidad incrementada exitosamente para el producto {}: {} -> {}", 
                productoId, cantidadAnterior, inventarioGuardado.getCantidad());
        
        return buildInventarioResponse(inventarioGuardado, producto);
    }

    /**
     * Decrementar la cantidad de un producto (venta)
     */
    public InventarioResponse decrementarCantidad(Integer productoId, Integer cantidadDecremento, BigDecimal precioUnitario) {
        log.info("Decrementando cantidad del producto {} en: {}", productoId, cantidadDecremento);

        if (cantidadDecremento <= 0) {
            throw new IllegalArgumentException("La cantidad de decremento debe ser positiva");
        }

        // Verificar que el producto existe
        productoService.productoExists(productoId)
                .subscribe(exists -> {
                    if (!exists) {
                        throw new RuntimeException("El producto con ID " + productoId + " no existe");
                    }
                });

        // Buscar el inventario
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new RuntimeException("No se encontró inventario para el producto con ID " + productoId));

        Integer cantidadAnterior = inventario.getCantidad();
        inventario.decrementarCantidad(cantidadDecremento);
        
        Inventario inventarioGuardado = inventarioRepository.save(inventario);
        
        // Registrar en el historial
        registrarEnHistorial(productoId, cantidadDecremento, precioUnitario, "VENTA");
        
        // Obtener información del producto
        Producto producto = productoService.getProductoById(productoId).block();
        
        log.info("Cantidad decrementada exitosamente para el producto {}: {} -> {}", 
                productoId, cantidadAnterior, inventarioGuardado.getCantidad());
        
        return buildInventarioResponse(inventarioGuardado, producto);
    }

    /**
     * Obtener productos con stock bajo
     */
    public List<InventarioResponse> getProductosConStockBajo(Integer cantidadMinima) {
        log.info("Consultando productos con stock bajo (menos de {} unidades)", cantidadMinima);
        
        List<Inventario> inventarios = inventarioRepository.findProductosConStockBajo(cantidadMinima);
        
        return inventarios.stream()
                .map(inventario -> {
                    Producto producto = productoService.getProductoById(inventario.getProductoId()).block();
                    return buildInventarioResponse(inventario, producto);
                })
                .toList();
    }

    /**
     * Obtener productos sin stock
     */
    public List<InventarioResponse> getProductosSinStock() {
        log.info("Consultando productos sin stock");
        
        List<Inventario> inventarios = inventarioRepository.findProductosSinStock();
        
        return inventarios.stream()
                .map(inventario -> {
                    Producto producto = productoService.getProductoById(inventario.getProductoId()).block();
                    return buildInventarioResponse(inventario, producto);
                })
                .toList();
    }

    /**
     * Obtener estadísticas del inventario
     */
    public InventarioStats getEstadisticasInventario() {
        log.info("Consultando estadísticas del inventario");
        
        long totalProductos = inventarioRepository.countTotalProductos();
        long totalCantidades = inventarioRepository.sumTotalCantidades();
        long productosSinStock = inventarioRepository.findProductosSinStock().size();
        long productosConStockBajo = inventarioRepository.findProductosConStockBajo(10).size();
        
        return InventarioStats.builder()
                .totalProductos(totalProductos)
                .totalCantidades(totalCantidades)
                .productosSinStock(productosSinStock)
                .productosConStockBajo(productosConStockBajo)
                .build();
    }

    /**
     * Registrar operación en el historial
     */
    private void registrarEnHistorial(Integer productoId, Integer cantidad, BigDecimal precioUnitario, String tipoOperacion) {
        try {
            HistorialCompra historial = HistorialCompra.builder()
                    .productoId(productoId)
                    .cantidad(cantidad)
                    .precioUnitario(precioUnitario)
                    .tipoOperacion(HistorialCompra.TipoOperacion.valueOf(tipoOperacion))
                    .fechaOperacion(LocalDateTime.now())
                    .build();
            
            historial.calcularPrecioTotal();
            historialCompraRepository.save(historial);
            
            log.info("Operación registrada en historial: {} - Producto: {} - Cantidad: {}", 
                    tipoOperacion, productoId, cantidad);
        } catch (Exception e) {
            log.error("Error al registrar en historial: {}", e.getMessage());
            // No lanzar excepción para no afectar la operación principal
        }
    }

    /**
     * Registrar operación en el historial (sin precio)
     */
    private void registrarEnHistorial(Integer productoId, Integer cantidadAnterior, Integer cantidadNueva, String tipoOperacion) {
        try {
            HistorialCompra historial = HistorialCompra.builder()
                    .productoId(productoId)
                    .cantidad(Math.abs(cantidadNueva - cantidadAnterior))
                    .tipoOperacion(HistorialCompra.TipoOperacion.valueOf(tipoOperacion))
                    .fechaOperacion(LocalDateTime.now())
                    .observaciones("Ajuste de inventario: " + cantidadAnterior + " -> " + cantidadNueva)
                    .build();
            
            historialCompraRepository.save(historial);
            
            log.info("Ajuste registrado en historial: Producto: {} - Cantidad: {} -> {}", 
                    productoId, cantidadAnterior, cantidadNueva);
        } catch (Exception e) {
            log.error("Error al registrar ajuste en historial: {}", e.getMessage());
        }
    }

    /**
     * Construir respuesta JSON API
     */
    private InventarioResponse buildInventarioResponse(Inventario inventario, Producto producto) {
        return InventarioResponse.builder()
                .data(InventarioResponse.InventarioData.builder()
                        .id(String.valueOf(inventario.getProductoId()))
                        .attributes(InventarioResponse.InventarioAttributes.builder()
                                .cantidad(inventario.getCantidad())
                                .fechaCreacion(inventario.getFechaCreacion())
                                .fechaActualizacion(inventario.getFechaActualizacion())
                                .build())
                        .relationships(InventarioResponse.InventarioRelationships.builder()
                                .producto(InventarioResponse.ProductoRelationship.builder()
                                        .data(InventarioResponse.ProductoReference.builder()
                                                .id(String.valueOf(inventario.getProductoId()))
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();
    }

    /**
     * Clase para estadísticas del inventario
     */
    public static class InventarioStats {
        private long totalProductos;
        private long totalCantidades;
        private long productosSinStock;
        private long productosConStockBajo;

        public InventarioStats() {}

        public InventarioStats(long totalProductos, long totalCantidades, long productosSinStock, long productosConStockBajo) {
            this.totalProductos = totalProductos;
            this.totalCantidades = totalCantidades;
            this.productosSinStock = productosSinStock;
            this.productosConStockBajo = productosConStockBajo;
        }

        public static InventarioStatsBuilder builder() {
            return new InventarioStatsBuilder();
        }

        public long getTotalProductos() { return totalProductos; }
        public void setTotalProductos(long totalProductos) { this.totalProductos = totalProductos; }
        public long getTotalCantidades() { return totalCantidades; }
        public void setTotalCantidades(long totalCantidades) { this.totalCantidades = totalCantidades; }
        public long getProductosSinStock() { return productosSinStock; }
        public void setProductosSinStock(long productosSinStock) { this.productosSinStock = productosSinStock; }
        public long getProductosConStockBajo() { return productosConStockBajo; }
        public void setProductosConStockBajo(long productosConStockBajo) { this.productosConStockBajo = productosConStockBajo; }

        public static class InventarioStatsBuilder {
            private long totalProductos;
            private long totalCantidades;
            private long productosSinStock;
            private long productosConStockBajo;

            InventarioStatsBuilder() {}

            public InventarioStatsBuilder totalProductos(long totalProductos) {
                this.totalProductos = totalProductos;
                return this;
            }

            public InventarioStatsBuilder totalCantidades(long totalCantidades) {
                this.totalCantidades = totalCantidades;
                return this;
            }

            public InventarioStatsBuilder productosSinStock(long productosSinStock) {
                this.productosSinStock = productosSinStock;
                return this;
            }

            public InventarioStatsBuilder productosConStockBajo(long productosConStockBajo) {
                this.productosConStockBajo = productosConStockBajo;
                return this;
            }

            public InventarioStats build() {
                return new InventarioStats(totalProductos, totalCantidades, productosSinStock, productosConStockBajo);
            }
        }
    }
}