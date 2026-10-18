-- ============================================================
--  Stock minimo por producto (aviso de reposicion)
-- ============================================================
--  Agrega al producto la cantidad a partir de la cual hay que
--  reponer. En la lista de productos la fila se ve en rojo, y en
--  el panel principal aparece la cantidad de productos a reponer.
--
--  stockMinimo = 0  ->  ese producto no avisa nada.
--
--  Ejecutar UNA sola vez (si se vuelve a correr, MySQL avisa que
--  la columna ya existe; no rompe nada, se ignora el aviso).
-- ============================================================

ALTER TABLE producto ADD COLUMN stockMinimo INT NOT NULL DEFAULT 0 AFTER stock;

-- A los productos que ya estaban cargados les ponemos un minimo de 3
-- para que el aviso funcione desde el arranque.
-- OJO: esta linea se corre UNA sola vez. Si mas adelante configuras los
-- minimos desde la pantalla de Productos y volves a correr el script
-- completo, los que dejes en 0 se te vuelven a poner en 3.
UPDATE producto SET stockMinimo = 3 WHERE stockMinimo = 0;

SELECT idProducto, nombre, stock, stockMinimo FROM producto ORDER BY idProducto;
