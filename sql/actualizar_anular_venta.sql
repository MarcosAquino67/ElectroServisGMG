-- ============================================================
--  Anulacion de ventas
-- ============================================================
--  Agrega el estado a la venta: REGISTRADA (normal) o ANULADA.
--
--  Anular NO borra la venta: la deja marcada y le devuelve el
--  stock a los productos. Se hace asi a proposito, para que el
--  numero de factura ya usado no se vuelva a repetir nunca.
--
--  Ejecutar UNA sola vez (si se vuelve a correr, MySQL avisa que
--  la columna ya existe; no rompe nada, se ignora el aviso).
-- ============================================================

ALTER TABLE venta ADD COLUMN estado VARCHAR(15) NOT NULL DEFAULT 'REGISTRADA' AFTER cdc;

SELECT idVenta, tipoDocumento, nroFactura, total, estado FROM venta ORDER BY idVenta;
