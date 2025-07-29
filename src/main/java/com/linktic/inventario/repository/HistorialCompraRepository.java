package com.linktic.inventario.repository;

import com.linktic.inventario.model.HistorialCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad HistorialCompra
 */
@Repository
public interface HistorialCompraRepository extends JpaRepository<HistorialCompra, Long> {

    /**
     * Buscar historial por ID de producto
     */
    List<HistorialCompra> findByProductoIdOrderByFechaOperacionDesc(Integer productoId);

    /**
     * Buscar historial por tipo de operación
     */
    List<HistorialCompra> findByTipoOperacionOrderByFechaOperacionDesc(HistorialCompra.TipoOperacion tipoOperacion);

    /**
     * Buscar historial por rango de fechas
     */
    @Query("SELECT h FROM HistorialCompra h WHERE h.fechaOperacion BETWEEN :fechaInicio AND :fechaFin ORDER BY h.fechaOperacion DESC")
    List<HistorialCompra> findByFechaOperacionBetween(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                                     @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Buscar historial por producto y tipo de operación
     */
    List<HistorialCompra> findByProductoIdAndTipoOperacionOrderByFechaOperacionDesc(Integer productoId, 
                                                                                   HistorialCompra.TipoOperacion tipoOperacion);

    /**
     * Obtener estadísticas de operaciones por tipo
     */
    @Query("SELECT h.tipoOperacion, COUNT(h), SUM(h.cantidad) FROM HistorialCompra h GROUP BY h.tipoOperacion")
    List<Object[]> getEstadisticasPorTipoOperacion();

    /**
     * Obtener el total de operaciones por producto
     */
    @Query("SELECT COUNT(h) FROM HistorialCompra h WHERE h.productoId = :productoId")
    long countByProductoId(@Param("productoId") Integer productoId);
} 