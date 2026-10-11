-- ============================================================
-- ElectroServis G.M.G - Base de datos para la aplicacion Java
-- (la app trabaja con estas tablas; NO toca electroservis_db)
-- ============================================================
CREATE DATABASE IF NOT EXISTS electroservis_app
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE electroservis_app;

CREATE TABLE IF NOT EXISTS usuario (
    idUsuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    clave VARCHAR(100) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    estado VARCHAR(15) NOT NULL DEFAULT 'ACTIVO'
);

CREATE TABLE IF NOT EXISTS cliente (
    idCliente INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    cedula VARCHAR(20),
    telefono VARCHAR(30),
    direccion VARCHAR(200),
    email VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS proveedor (
    idProveedor INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ruc VARCHAR(20),
    telefono VARCHAR(30),
    direccion VARCHAR(200),
    email VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS producto (
    idProducto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    stock INT NOT NULL DEFAULT 0,
    precioCompra DOUBLE NOT NULL DEFAULT 0,
    precioVenta DOUBLE NOT NULL DEFAULT 0,
    idProveedor INT NULL,
    foto VARCHAR(255) NULL,
    CONSTRAINT fk_producto_prov FOREIGN KEY (idProveedor)
        REFERENCES proveedor(idProveedor)
);

CREATE TABLE IF NOT EXISTS equipo (
    idEquipo INT AUTO_INCREMENT PRIMARY KEY,
    idCliente INT NOT NULL,
    tipo VARCHAR(50),
    marca VARCHAR(50),
    modelo VARCHAR(50),
    nroSerie VARCHAR(50),
    fallaReportada VARCHAR(255),
    CONSTRAINT fk_equipo_cli FOREIGN KEY (idCliente)
        REFERENCES cliente(idCliente)
);

CREATE TABLE IF NOT EXISTS ordenservicio (
    idOrden INT AUTO_INCREMENT PRIMARY KEY,
    idCliente INT NOT NULL,
    idEquipo INT NOT NULL,
    idUsuario INT NOT NULL,
    fechaIngreso DATETIME NOT NULL,
    fechaEntrega DATETIME NULL,
    estado VARCHAR(30) NOT NULL,
    diagnostico VARCHAR(500),
    costoReparacion DOUBLE NOT NULL DEFAULT 0,
    observaciones VARCHAR(500),
    CONSTRAINT fk_orden_cli FOREIGN KEY (idCliente) REFERENCES cliente(idCliente),
    CONSTRAINT fk_orden_usu FOREIGN KEY (idUsuario) REFERENCES usuario(idUsuario)
);

CREATE TABLE IF NOT EXISTS venta (
    idVenta INT AUTO_INCREMENT PRIMARY KEY,
    idCliente INT NOT NULL,
    idUsuario INT NOT NULL,
    fechaVenta DATETIME NOT NULL,
    total DOUBLE NOT NULL DEFAULT 0,
    formaPago VARCHAR(30),
    CONSTRAINT fk_venta_cli FOREIGN KEY (idCliente) REFERENCES cliente(idCliente),
    CONSTRAINT fk_venta_usu FOREIGN KEY (idUsuario) REFERENCES usuario(idUsuario)
);

CREATE TABLE IF NOT EXISTS detalleventa (
    idDetalleVenta INT AUTO_INCREMENT PRIMARY KEY,
    idVenta INT NOT NULL,
    idProducto INT NOT NULL,
    cantidad INT NOT NULL,
    precioUnitario DOUBLE NOT NULL,
    subtotal DOUBLE NOT NULL,
    CONSTRAINT fk_detv_venta FOREIGN KEY (idVenta) REFERENCES venta(idVenta),
    CONSTRAINT fk_detv_prod FOREIGN KEY (idProducto) REFERENCES producto(idProducto)
);

CREATE TABLE IF NOT EXISTS compra (
    idCompra INT AUTO_INCREMENT PRIMARY KEY,
    idProveedor INT NOT NULL,
    idUsuario INT NOT NULL,
    fechaCompra DATETIME NOT NULL,
    total DOUBLE NOT NULL DEFAULT 0,
    estado VARCHAR(20) NOT NULL DEFAULT 'COMPLETADA',
    CONSTRAINT fk_compra_prov FOREIGN KEY (idProveedor) REFERENCES proveedor(idProveedor),
    CONSTRAINT fk_compra_usu FOREIGN KEY (idUsuario) REFERENCES usuario(idUsuario)
);

CREATE TABLE IF NOT EXISTS detallecompra (
    idDetalleCompra INT AUTO_INCREMENT PRIMARY KEY,
    idCompra INT NOT NULL,
    idProducto INT NOT NULL,
    cantidad INT NOT NULL,
    precioUnitario DOUBLE NOT NULL,
    subtotal DOUBLE NOT NULL,
    CONSTRAINT fk_detc_compra FOREIGN KEY (idCompra) REFERENCES compra(idCompra),
    CONSTRAINT fk_detc_prod FOREIGN KEY (idProducto) REFERENCES producto(idProducto)
);

CREATE TABLE IF NOT EXISTS caja (
    idCaja INT AUTO_INCREMENT PRIMARY KEY,
    idUsuario INT NOT NULL,
    fechaApertura DATETIME NOT NULL,
    fechaCierre DATETIME NULL,
    montoInicial DOUBLE NOT NULL DEFAULT 0,
    montoFinal DOUBLE NULL,
    estado VARCHAR(15) NOT NULL DEFAULT 'ABIERTA',
    CONSTRAINT fk_caja_usu FOREIGN KEY (idUsuario) REFERENCES usuario(idUsuario)
);

CREATE TABLE IF NOT EXISTS caja_movimiento (
    idMovimiento INT AUTO_INCREMENT PRIMARY KEY,
    idCaja INT NOT NULL,
    tipo VARCHAR(15) NOT NULL,
    monto DOUBLE NOT NULL,
    descripcion VARCHAR(255),
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mov_caja FOREIGN KEY (idCaja) REFERENCES caja(idCaja)
);

-- Usuario inicial (clave 2307, igual que tu MySQL)
INSERT INTO usuario (nombre, usuario, clave, rol, estado)
VALUES ('Marcos', 'Marcos', '2307', 'ADMINISTRADOR', 'ACTIVO')
ON DUPLICATE KEY UPDATE clave='2307', estado='ACTIVO';

-- Caja inicial para que el modulo Caja funcione
INSERT INTO caja (idCaja, idUsuario, fechaApertura, montoInicial, estado)
VALUES (1, 1, NOW(), 0, 'ABIERTA')
ON DUPLICATE KEY UPDATE idCaja=1;
