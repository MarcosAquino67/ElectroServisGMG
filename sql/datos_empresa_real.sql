-- ============================================================================
--  Datos reales del emisor (ELECTROSERVIS G.M.G. - NATIVIDAD CARDENAS REYES)
--  Se cargan en la tabla empresa para la Factura Electronica y el ticket.
--  Ejecutar UNA sola vez sobre una base ya existente:
--      mysql -uroot -p electroservis_app < datos_empresa_real.sql
-- ============================================================================

-- Columnas nuevas: nombre comercial (titulo del ticket) y ciudad (direccion).
-- Si ya existen, este ALTER da error y se puede saltar.
ALTER TABLE empresa
    ADD COLUMN nombreComercial VARCHAR(150) NOT NULL DEFAULT '' AFTER razonSocial,
    ADD COLUMN ciudad VARCHAR(80) NULL AFTER direccion;

UPDATE empresa SET
    razonSocial            = 'NATIVIDAD CARDENAS REYES',
    nombreComercial        = 'ELECTROSERVIS G.M.G.',
    ruc                    = '2403405',
    dvRuc                  = '3',
    direccion              = 'BARRIO DIVINO NIÑO - WALTER INSFRAN-CALLE 4 E/ CALLE  7 Y 6-MANZANA N° 7.- CASA #9024 N° 0',
    ciudad                 = 'CAAGUAZU',
    telefono               = '(0971)452427',
    email                  = 'electroservisgmg@gmail.com',
    actividades            = 'COMERCIO AL POR MAYOR DE MERCANCÍAS N.C.P. A CAMBIO DE UNA RETRIBUCIÓN O POR CONTRATA\nCOMERCIO AL POR MENOR DE EQUIPOS INFORMÁTICOS Y SOFTWARE\nCOMERCIO AL POR MENOR DE EQUIPOS DE TELECOMUNICACIONES',
    timbrado               = '19019758',
    timbradoVigenciaDesde  = '2026-07-30',
    establecimiento        = '001',
    puntoExpedicion        = '001',
    nroFacturaInicio       = 13,
    nroFacturaFin          = 9999999,
    tipoContribuyente      = '1',
    logo                   = NULL
WHERE idEmpresa = 1;
