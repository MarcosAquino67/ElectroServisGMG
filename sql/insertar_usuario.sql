-- Usuario inicial para ElectroServis G.M.G
USE electroservis_app;

-- Crear tabla si no existe
CREATE TABLE IF NOT EXISTS usuario (
    idUsuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    clave VARCHAR(100) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    estado VARCHAR(15) NOT NULL DEFAULT 'ACTIVO'
);

-- Insertar usuario admin
-- La clave se escribe aca en texto plano y el sistema la convierte sola a
-- hash (SHA-256) la primera vez que ese usuario inicia sesion.
-- Si se vuelve a correr este script, la clave del admin vuelve a ser '2307':
-- sirve como "reset" si alguien se olvido la contrasena.
INSERT INTO usuario (nombre, usuario, clave, rol, estado)
VALUES ('Marcos', 'Marcos', '2307', 'ADMINISTRADOR', 'ACTIVO')
ON DUPLICATE KEY UPDATE clave='2307', estado='ACTIVO';

SELECT * FROM usuario;
