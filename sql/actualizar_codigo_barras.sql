-- ============================================================
--  Codigo de barras de productos
-- ============================================================
--  producto.codigo      -> codigo interno (P0016, P0017, ...)
--  producto.codigoBarras -> codigo del fabricante (EAN-13) o uno
--                           generado por el sistema desde Productos.
--
--  Es unico (no puede haber dos productos con el mismo codigo) pero
--  puede quedar vacio: en ese caso ese producto no se puede vender
--  escaneando, solo eligiendolo del combo.
--
--  Ejecutar UNA sola vez (si se vuelve a correr, MySQL avisa que la
--  columna ya existe; no rompe nada, solo hay que ignorar el error).
-- ============================================================

ALTER TABLE producto ADD COLUMN codigoBarras VARCHAR(20) NULL AFTER codigo;
ALTER TABLE producto ADD UNIQUE KEY uk_producto_codigoBarras (codigoBarras);

-- Codigos de ejemplo (EAN-13, prefijo 784 = Paraguay) para los productos
-- cargados actualmente. Los reales se cambian desde FormProductos.
UPDATE producto SET codigoBarras = '7840000010165' WHERE idProducto = 16;
UPDATE producto SET codigoBarras = '7840000010172' WHERE idProducto = 17;
UPDATE producto SET codigoBarras = '7840000010189' WHERE idProducto = 18;
UPDATE producto SET codigoBarras = '7840000010196' WHERE idProducto = 19;
UPDATE producto SET codigoBarras = '7840000010202' WHERE idProducto = 20;
UPDATE producto SET codigoBarras = '7840000010219' WHERE idProducto = 21;
UPDATE producto SET codigoBarras = '7840000010226' WHERE idProducto = 22;

SELECT idProducto, codigo, codigoBarras, nombre FROM producto ORDER BY idProducto;
