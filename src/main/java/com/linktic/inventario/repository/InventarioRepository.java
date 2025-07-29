package com.linktic.inventario.repository;

import com.linktic.inventario.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Inventario
 */
@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Integer> {

    /**
     * Buscar inventario por ID de producto
     */
    Optional<Inventario> findByProductoId(Integer productoId);

    /**
     * Verificar si existe inventario para un producto
     */
    boolean existsByProductoId(Integer productoId);

    /**
     * Buscar productos con stock bajo (menos de la cantidad especificada)
     */
    @Query("SELECT i FROM Inventario i WHERE i.cantidad < :cantidadMinima")
    List<Inventario> findProductosConStockBajo(@Param("cantidadMinima") Integer cantidadMinima);

    /**
     * Buscar productos sin stock
     */
    @Query("SELECT i FROM Inventario i WHERE i.cantidad = 0")
    List<Inventario> findProductosSinStock();

    /**
     * Obtener el total de productos en inventario
     */
    @Query("SELECT COUNT(i) FROM Inventario i")
    long countTotalProductos();

    /**
     * Obtener la suma total de cantidades en inventario
     */
    @Query("SELECT COALESCE(SUM(i.cantidad), 0) FROM Inventario i")
    long sumTotalCantidades();

    /**
     * Buscar inventario por rango de cantidades
     */
    @Query("SELECT i FROM Inventario i WHERE i.cantidad BETWEEN :cantidadMin AND :cantidadMax")
    List<Inventario> findByCantidadBetween(@Param("cantidadMin") Integer cantidadMin, 
                                         @Param("cantidadMax") Integer cantidadMax);
} 