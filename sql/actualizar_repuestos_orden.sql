-- ============================================================
--  Repuestos usados en una orden de servicio
-- ============================================================
--  Guarda que productos (repuestos) se usaron en cada reparacion.
--  Al agregar un repuesto se descuenta del stock y su precio se
--  suma al costo de la orden; al quitarlo se devuelve al stock y
--  se resta del costo.
--
--  Ejecutar UNA sola vez (si se vuelve a correr no rompe nada:
--  la tabla ya existe y se deja como esta).
-- ============================================================

CREATE TABLE IF NOT EXISTS orden_repuesto (
    idRepuesto INT AUTO_INCREMENT PRIMARY KEY,
    idOrdenServicio INT NOT NULL,
    idProducto INT NOT NULL,
    cantidad INT NOT NULL,
    precioUnitario DOUBLE NOT NULL DEFAULT 0,
    fecha DATETIME NOT NULL,
    CONSTRAINT fk_rep_orden FOREIGN KEY (idOrdenServicio) REFERENCES ordenservicio(idOrden),
    CONSTRAINT fk_rep_prod FOREIGN KEY (idProducto) REFERENCES producto(idProducto)
);

SHOW TABLES LIKE 'orden_repuesto';
