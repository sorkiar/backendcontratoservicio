-- V3: Agrega claves de configuración faltantes para la generación de PDF
-- Las claves del grupo empresa_emisora ya deben existir (emprRuc, emprRazonSocial, emprDireccionFiscal).
-- Se añaden las claves específicas para los campos del PDF.

INSERT IGNORE INTO configuracion (grupo, clave, valor, tipo_dato, descripcion, fecha_creacion, fecha_actualizacion)
VALUES
  ('empresa_emisora', 'emprNombreComercial',    '',
   'STRING', 'Nombre comercial mostrado en el encabezado del PDF', NOW(), NOW()),
  ('empresa_emisora', 'emprTelefono',           '',
   'STRING', 'Teléfono de la empresa para el PDF',                 NOW(), NOW()),
  ('empresa_emisora', 'emprPaginaWeb',          '',
   'STRING', 'Página web de la empresa para el PDF',               NOW(), NOW()),
  ('empresa_emisora', 'emprPdfTextoInferior',
   'Representación impresa de comprobante electrónico.',
   'STRING', 'Texto de pie de página en el PDF',                   NOW(), NOW()),
  ('empresa_emisora', 'emprPdfEslogan',         '',
   'STRING', 'Eslogan o mensaje especial en el PDF',               NOW(), NOW());
