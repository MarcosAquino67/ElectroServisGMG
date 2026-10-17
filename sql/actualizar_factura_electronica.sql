-- ============================================================
-- Migracion: Factura Electronica (SIFEN)
--   * IVA por producto (10 / 5 / 0)
--   * codigo de producto (columna COD. de la factura)
--   * descuento por renglon
--   * CDC + numero de factura + condicion de venta en venta
--   * tabla empresa (datos del emisor + timbrado)
-- Sirve tanto para una base ya creada como para electroservis_app vacia.
-- ============================================================
USE electroservis_app;

-- 1) producto: codigo + IVA
ALTER TABLE producto
    ADD COLUMN codigo VARCHAR(20) NULL AFTER idProducto,
    ADD COLUMN iva DOUBLE NOT NULL DEFAULT 10 AFTER precioVenta;

-- 2) detalleventa: descuento por renglon + IVA del momento de la venta
ALTER TABLE detalleventa
    ADD COLUMN descuento DOUBLE NOT NULL DEFAULT 0 AFTER precioUnitario,
    ADD COLUMN iva DOUBLE NOT NULL DEFAULT 10 AFTER descuento;

-- 3) venta: condicion/tipo de transaccion, tipo de documento, numero y CDC
ALTER TABLE venta
    ADD COLUMN condicionVenta VARCHAR(15) NULL AFTER formaPago,
    ADD COLUMN tipoTransaccion VARCHAR(40) NULL AFTER condicionVenta,
    ADD COLUMN tipoDocumento VARCHAR(20) NOT NULL DEFAULT 'TICKET' AFTER tipoTransaccion,
    ADD COLUMN nroFactura INT NULL AFTER tipoDocumento,
    ADD COLUMN cdc VARCHAR(44) NULL AFTER nroFactura;

-- 4) empresa (datos del emisor para la factura)
CREATE TABLE IF NOT EXISTS empresa (
    idEmpresa INT AUTO_INCREMENT PRIMARY KEY,
    razonSocial VARCHAR(150) NOT NULL,
    ruc VARCHAR(8) NOT NULL,
    dvRuc VARCHAR(1) NOT NULL DEFAULT '0',
    direccion VARCHAR(200),
    telefono VARCHAR(40),
    email VARCHAR(100),
    actividades VARCHAR(600),
    timbrado VARCHAR(20),
    timbradoVigenciaDesde DATE NULL,
    establecimiento VARCHAR(3) NOT NULL DEFAULT '001',
    puntoExpedicion VARCHAR(3) NOT NULL DEFAULT '001',
    nroFacturaInicio INT NOT NULL DEFAULT 1,
    nroFacturaFin INT NOT NULL DEFAULT 9999999,
    tipoContribuyente VARCHAR(1) NOT NULL DEFAULT '2',
    logo VARCHAR(255) NULL
);

INSERT INTO empresa (idEmpresa, razonSocial, ruc, dvRuc, direccion, telefono, email, actividades,
                     timbrado, timbradoVigenciaDesde, establecimiento, puntoExpedicion,
                     nroFacturaInicio, nroFacturaFin, tipoContribuyente)
VALUES (1, 'ELECTROSERVIS G.M.G.', '80012345', '6', 'Av. San Blas - Cnel. Oviedo', '(0981) 123456',
        'electroservisgmg@gmail.com',
        'COMERCIO AL POR MAYOR Y MENOR DE EQUIPOS INFORMATICOS\nSERVICIO DE REPARACION Y MANTENIMIENTO DE EQUIPOS\nVENTA DE REPUESTOS Y ACCESORIOS ELECTRONICOS',
        '12345678', '2026-01-01', '001', '001', 1, 9999999, '2')
ON DUPLICATE KEY UPDATE razonSocial = empresa.razonSocial;

-- 5) codigos para los productos que ya existian (solo si estan vacios)
UPDATE producto SET codigo = CONCAT('P', LPAD(idProducto, 4, '0')) WHERE codigo IS NULL OR codigo = '';
