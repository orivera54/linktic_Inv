package com.linktic.inventario.service;

import com.linktic.inventario.dto.InventarioResponse;
import com.linktic.inventario.model.HistorialCompra;
import com.linktic.inventario.model.Inventario;
import com.linktic.inventario.model.Producto;
import com.linktic.inventario.repository.HistorialCompraRepository;
import com.linktic.inventario.repository.InventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para InventarioService
 */
@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private HistorialCompraRepository historialCompraRepository;

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private InventarioService inventarioService;

    private Inventario inventario;
    private Producto producto;

    @BeforeEach
    void setUp() {
        inventario = Inventario.builder()
                .productoId(1)
                .cantidad(100)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        producto = Producto.builder()
                .id(1)
                .nombre("Producto Test")
                .descripcion("Descripción del producto")
                .precio(new BigDecimal("99.99"))
                .categoria("Electrónicos")
                .activo(true)
                .build();
    }

    @Test
    void consultarCantidadDisponible_Success() {
        // Arrange
        when(productoService.productoExists(1)).thenReturn(Mono.just(true));
        when(inventarioRepository.findByProductoId(1)).thenReturn(Optional.of(inventario));
        when(productoService.getProductoById(1)).thenReturn(Mono.just(producto));

        // Act
        InventarioResponse response = inventarioService.consultarCantidadDisponible(1);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals("1", response.getData().getId());
        assertEquals(100, response.getData().getAttributes().getCantidad());
        verify(productoService).productoExists(1);
        verify(inventarioRepository).findByProductoId(1);
        verify(productoService).getProductoById(1);
    }

    @Test
    void consultarCantidadDisponible_ProductoNoExiste() {
        // Arrange
        when(productoService.productoExists(1)).thenReturn(Mono.just(false));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            inventarioService.consultarCantidadDisponible(1);
        });
        verify(productoService).productoExists(1);
        verify(inventarioRepository, never()).findByProductoId(any());
    }

    @Test
    void consultarCantidadDisponible_InventarioNoExiste() {
        // Arrange
        when(productoService.productoExists(1)).thenReturn(Mono.just(true));
        when(inventarioRepository.findByProductoId(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            inventarioService.consultarCantidadDisponible(1);
        });
        verify(productoService).productoExists(1);
        verify(inventarioRepository).findByProductoId(1);
        verify(productoService, never()).getProductoById(any());
    }

    @Test
    void actualizarCantidad_Success() {
        // Arrange
        when(productoService.productoExists(1)).thenReturn(Mono.just(true));
        when(inventarioRepository.findByProductoId(1)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);
        when(productoService.getProductoById(1)).thenReturn(Mono.just(producto));
        when(historialCompraRepository.save(any(HistorialCompra.class))).thenReturn(new HistorialCompra());

        // Act
        InventarioResponse response = inventarioService.actualizarCantidad(1, 150);

        // Assert
        assertNotNull(response);
        assertEquals("1", response.getData().getId());
        assertEquals(150, response.getData().getAttributes().getCantidad());
        verify(inventarioRepository).save(any(Inventario.class));
        verify(historialCompraRepository).save(any(HistorialCompra.class));
    }

    @Test
    void actualizarCantidad_CantidadNegativa() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.actualizarCantidad(1, -10);
        });
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void actualizarCantidad_CrearNuevoInventario() {
        // Arrange
        when(productoService.productoExists(1)).thenReturn(Mono.just(true));
        when(inventarioRepository.findByProductoId(1)).thenReturn(Optional.empty());
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);
        when(productoService.getProductoById(1)).thenReturn(Mono.just(producto));
        when(historialCompraRepository.save(any(HistorialCompra.class))).thenReturn(new HistorialCompra());

        // Act
        InventarioResponse response = inventarioService.actualizarCantidad(1, 50);

        // Assert
        assertNotNull(response);
        verify(inventarioRepository).save(any(Inventario.class));
    }

    @Test
    void incrementarCantidad_Success() {
        // Arrange
        when(productoService.productoExists(1)).thenReturn(Mono.just(true));
        when(inventarioRepository.findByProductoId(1)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);
        when(productoService.getProductoById(1)).thenReturn(Mono.just(producto));
        when(historialCompraRepository.save(any(HistorialCompra.class))).thenReturn(new HistorialCompra());

        // Act
        InventarioResponse response = inventarioService.incrementarCantidad(1, 25, new BigDecimal("10.50"));

        // Assert
        assertNotNull(response);
        assertEquals("1", response.getData().getId());
        assertEquals(125, response.getData().getAttributes().getCantidad());
        verify(inventarioRepository).save(any(Inventario.class));
        verify(historialCompraRepository).save(any(HistorialCompra.class));
    }

    @Test
    void incrementarCantidad_CantidadInvalida() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.incrementarCantidad(1, 0, new BigDecimal("10.50"));
        });
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void decrementarCantidad_Success() {
        // Arrange
        when(productoService.productoExists(1)).thenReturn(Mono.just(true));
        when(inventarioRepository.findByProductoId(1)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);
        when(productoService.getProductoById(1)).thenReturn(Mono.just(producto));
        when(historialCompraRepository.save(any(HistorialCompra.class))).thenReturn(new HistorialCompra());

        // Act
        InventarioResponse response = inventarioService.decrementarCantidad(1, 25, new BigDecimal("15.00"));

        // Assert
        assertNotNull(response);
        assertEquals("1", response.getData().getId());
        assertEquals(75, response.getData().getAttributes().getCantidad());
        verify(inventarioRepository).save(any(Inventario.class));
        verify(historialCompraRepository).save(any(HistorialCompra.class));
    }

    @Test
    void decrementarCantidad_StockInsuficiente() {
        // Arrange
        when(productoService.productoExists(1)).thenReturn(Mono.just(true));
        when(inventarioRepository.findByProductoId(1)).thenReturn(Optional.of(inventario));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.decrementarCantidad(1, 150, new BigDecimal("15.00"));
        });
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void getProductosConStockBajo_Success() {
        // Arrange
        List<Inventario> inventarios = List.of(inventario);
        when(inventarioRepository.findProductosConStockBajo(10)).thenReturn(inventarios);
        when(productoService.getProductoById(1)).thenReturn(Mono.just(producto));

        // Act
        List<InventarioResponse> response = inventarioService.getProductosConStockBajo(10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("1", response.get(0).getData().getId());
        verify(inventarioRepository).findProductosConStockBajo(10);
    }

    @Test
    void getProductosSinStock_Success() {
        // Arrange
        List<Inventario> inventarios = List.of(inventario);
        when(inventarioRepository.findProductosSinStock()).thenReturn(inventarios);
        when(productoService.getProductoById(1)).thenReturn(Mono.just(producto));

        // Act
        List<InventarioResponse> response = inventarioService.getProductosSinStock();

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        verify(inventarioRepository).findProductosSinStock();
    }

    @Test
    void getEstadisticasInventario_Success() {
        // Arrange
        when(inventarioRepository.countTotalProductos()).thenReturn(10L);
        when(inventarioRepository.sumTotalCantidades()).thenReturn(1000L);
        when(inventarioRepository.findProductosSinStock()).thenReturn(List.of());
        when(inventarioRepository.findProductosConStockBajo(10)).thenReturn(List.of());

        // Act
        InventarioService.InventarioStats stats = inventarioService.getEstadisticasInventario();

        // Assert
        assertNotNull(stats);
        assertEquals(10L, stats.getTotalProductos());
        assertEquals(1000L, stats.getTotalCantidades());
        assertEquals(0L, stats.getProductosSinStock());
        assertEquals(0L, stats.getProductosConStockBajo());
        verify(inventarioRepository).countTotalProductos();
        verify(inventarioRepository).sumTotalCantidades();
    }

    @Test
    void inventario_CantidadValida() {
        // Act & Assert
        assertTrue(inventario.isCantidadValida());
        
        inventario.setCantidad(0);
        assertTrue(inventario.isCantidadValida());
        
        inventario.setCantidad(null);
        assertFalse(inventario.isCantidadValida());
    }

    @Test
    void inventario_ActualizarCantidad() {
        // Act
        inventario.actualizarCantidad(200);

        // Assert
        assertEquals(200, inventario.getCantidad());
    }

    @Test
    void inventario_ActualizarCantidadNegativa() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventario.actualizarCantidad(-10);
        });
    }

    @Test
    void inventario_IncrementarCantidad() {
        // Arrange
        int cantidadInicial = inventario.getCantidad();

        // Act
        inventario.incrementarCantidad(50);

        // Assert
        assertEquals(cantidadInicial + 50, inventario.getCantidad());
    }

    @Test
    void inventario_DecrementarCantidad() {
        // Arrange
        int cantidadInicial = inventario.getCantidad();

        // Act
        inventario.decrementarCantidad(30);

        // Assert
        assertEquals(cantidadInicial - 30, inventario.getCantidad());
    }

    @Test
    void inventario_DecrementarCantidadInsuficiente() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventario.decrementarCantidad(150);
        });
    }

    @Test
    void producto_Validaciones() {
        // Act & Assert
        assertTrue(producto.isActivo());
        assertEquals("Producto Test", producto.getNombreFormateado());
        assertTrue(producto.isPrecioValido());
        
        producto.setActivo(false);
        assertFalse(producto.isActivo());
        
        producto.setPrecio(null);
        assertFalse(producto.isPrecioValido());
    }
}