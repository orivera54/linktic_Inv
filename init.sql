-- Script de inicialización de la base de datos ltprods
-- Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS ltprods;
USE ltprods;

-- Crear tabla Producto (referenciada por Inventario)
CREATE TABLE IF NOT EXISTS Producto (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    categoria VARCHAR(100),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Crear tabla Inventario
CREATE TABLE IF NOT EXISTS Inventario (
    producto_id INT NOT NULL,
    cantidad INT NOT NULL DEFAULT 0,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    PRIMARY KEY (producto_id),
    FOREIGN KEY (producto_id) REFERENCES Producto(id) ON DELETE CASCADE
);

-- Crear tabla HistorialCompras (funcionalidad opcional)
CREATE TABLE IF NOT EXISTS historial_compras (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2),
    precio_total DECIMAL(10,2),
    tipo_operacion ENUM('COMPRA', 'VENTA', 'AJUSTE', 'DEVOLUCION') NOT NULL,
    usuario VARCHAR(100),
    observaciones TEXT,
    fecha_operacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (producto_id) REFERENCES Producto(id) ON DELETE CASCADE
);

-- Insertar datos de ejemplo para Producto
INSERT INTO Producto (id, nombre, descripcion, precio, categoria, activo) VALUES
(1, 'Laptop HP Pavilion', 'Laptop de 15 pulgadas con procesador Intel i5', 899.99, 'Electrónicos', true),
(2, 'Mouse Inalámbrico', 'Mouse inalámbrico ergonómico', 29.99, 'Accesorios', true),
(3, 'Teclado Mecánico', 'Teclado mecánico RGB con switches blue', 89.99, 'Accesorios', true),
(4, 'Monitor 24"', 'Monitor LED de 24 pulgadas Full HD', 199.99, 'Electrónicos', true),
(5, 'Auriculares Bluetooth', 'Auriculares inalámbricos con cancelación de ruido', 149.99, 'Audio', true);

-- Insertar datos de ejemplo para Inventario
INSERT INTO Inventario (producto_id, cantidad) VALUES
(1, 25),
(2, 100),
(3, 50),
(4, 15),
(5, 30);

-- Crear índices para mejorar el rendimiento
CREATE INDEX idx_producto_categoria ON Producto(categoria);
CREATE INDEX idx_producto_activo ON Producto(activo);
CREATE INDEX idx_inventario_cantidad ON Inventario(cantidad);
CREATE INDEX idx_historial_producto_fecha ON historial_compras(producto_id, fecha_operacion);
CREATE INDEX idx_historial_tipo_operacion ON historial_compras(tipo_operacion);

-- Crear vistas útiles
CREATE VIEW v_productos_con_stock AS
SELECT p.id, p.nombre, p.precio, i.cantidad, p.categoria
FROM Producto p
JOIN Inventario i ON p.id = i.producto_id
WHERE p.activo = true AND i.cantidad > 0;

CREATE VIEW v_productos_sin_stock AS
SELECT p.id, p.nombre, p.precio, p.categoria
FROM Producto p
LEFT JOIN Inventario i ON p.id = i.producto_id
WHERE p.activo = true AND (i.cantidad = 0 OR i.cantidad IS NULL);

-- Crear procedimiento almacenado para actualizar inventario
DELIMITER //
CREATE PROCEDURE sp_actualizar_inventario(
    IN p_producto_id INT,
    IN p_cantidad INT,
    IN p_tipo_operacion VARCHAR(20)
)
BEGIN
    DECLARE cantidad_actual INT DEFAULT 0;
    DECLARE nueva_cantidad INT DEFAULT 0;
    
    -- Obtener cantidad actual
    SELECT COALESCE(cantidad, 0) INTO cantidad_actual
    FROM Inventario
    WHERE producto_id = p_producto_id;
    
    -- Calcular nueva cantidad según el tipo de operación
    CASE p_tipo_operacion
        WHEN 'COMPRA' THEN
            SET nueva_cantidad = cantidad_actual + p_cantidad;
        WHEN 'VENTA' THEN
            IF cantidad_actual >= p_cantidad THEN
                SET nueva_cantidad = cantidad_actual - p_cantidad;
            ELSE
                SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Stock insuficiente';
            END IF;
        WHEN 'AJUSTE' THEN
            SET nueva_cantidad = p_cantidad;
        ELSE
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Tipo de operación no válido';
    END CASE;
    
    -- Actualizar inventario
    INSERT INTO Inventario (producto_id, cantidad) 
    VALUES (p_producto_id, nueva_cantidad)
    ON DUPLICATE KEY UPDATE 
        cantidad = nueva_cantidad,
        fecha_actualizacion = CURRENT_TIMESTAMP,
        version = version + 1;
        
    -- Registrar en historial
    INSERT INTO historial_compras (producto_id, cantidad, tipo_operacion, observaciones)
    VALUES (p_producto_id, p_cantidad, p_tipo_operacion, 
            CONCAT('Operación automática: ', p_tipo_operacion, ' - Cantidad: ', p_cantidad));
END //
DELIMITER ;

-- Crear trigger para actualizar fecha_actualizacion en Producto
DELIMITER //
CREATE TRIGGER tr_producto_update
BEFORE UPDATE ON Producto
FOR EACH ROW
BEGIN
    SET NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
END //
DELIMITER ;

-- Crear trigger para validar cantidad no negativa en Inventario
DELIMITER //
CREATE TRIGGER tr_inventario_cantidad_check
BEFORE INSERT ON Inventario
FOR EACH ROW
BEGIN
    IF NEW.cantidad < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La cantidad no puede ser negativa';
    END IF;
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER tr_inventario_cantidad_update_check
BEFORE UPDATE ON Inventario
FOR EACH ROW
BEGIN
    IF NEW.cantidad < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La cantidad no puede ser negativa';
    END IF;
END //
DELIMITER ;