-- =====================================================================
-- Plantilla del catálogo (municipio_id_municipio = NULL).
-- La primera vez que un municipio consulta su catálogo, el sistema copia esta
-- plantilla; después cada funcionario la ajusta para su municipio.
--
-- Costos (MXN) y plazos (días hábiles) tomados de fichas oficiales de trámites:
--   Constancia de residencia ........ Temixco, Mor. 2025 (2 UMA)
--   Constancia de no adeudo ......... Temixco, Mor. 2025 (3.5 UMA)
--   Licencia de funcionamiento SARE . Corregidora, Qro.
--   Alineamiento y número oficial ... Ciudad de México (número oficial)
--   Uso de suelo .................... Santa Isabel Cholula, Pue. ($19.50/m², referencia 100 m²)
--   Licencia de construcción ........ Temixco, Mor. 2025 (0.30 UMA/m², referencia 40 m²)
--   Servicio Militar Nacional ....... San Andrés Cholula, Pue. (gratuito)
-- =====================================================================

INSERT INTO tipo_tramite (id_tramite, nombre, descripcion, costo, plazo_dias, dependencia) VALUES
(1, 'Constancia de residencia',
    'Documento que acredita que vives en el municipio. Se usa para trámites escolares, laborales, bancarios o migratorios.',
    226.00, 3, 'Secretaría del Ayuntamiento'),
(2, 'Constancia de no adeudo',
    'Certifica que el predio o la toma de agua no tiene pagos pendientes con el municipio. Se pide para vender, heredar o escriturar.',
    396.00, 5, 'Tesorería Municipal'),
(3, 'Licencia de funcionamiento SARE (bajo riesgo)',
    'Autoriza la apertura de un negocio de bajo riesgo (tienda, papelería, cafetería, consultorio) por medio del Sistema de Apertura Rápida de Empresas.',
    200.00, 3, 'Dirección de Desarrollo Económico'),
(4, 'Constancia de alineamiento y número oficial',
    'Define el límite del predio con la vía pública y asigna su número oficial. Es requisito para construir y para contratar servicios.',
    300.00, 6, 'Dirección de Desarrollo Urbano'),
(5, 'Constancia de uso de suelo',
    'Indica qué usos permite el plan de desarrollo urbano para un predio (habitacional, comercial, mixto). Costo de referencia para un predio de 100 m².',
    1950.00, 4, 'Dirección de Desarrollo Urbano'),
(6, 'Licencia de construcción (obra menor)',
    'Permiso para construir, ampliar o remodelar hasta 60 m² en zona urbana. Costo de referencia para 40 m².',
    1360.00, 10, 'Dirección de Obras Públicas'),
(7, 'Precartilla del Servicio Militar Nacional',
    'Inscripción en la Junta Municipal de Reclutamiento para obtener la Cartilla de Identidad del Servicio Militar Nacional. Trámite gratuito.',
    0.00, 5, 'Junta Municipal de Reclutamiento');

SELECT setval('tipo_tramite_id_tramite_seq', 7);

INSERT INTO requisito_tramite (nombre, descripcion, obligatorio, tipo_tramite_id_tramite, orden) VALUES
-- 1. Constancia de residencia
('Identificación oficial', 'Credencial INE, pasaporte o cédula profesional vigente.', TRUE, 1, 1),
('Comprobante de domicilio', 'Recibo de luz, agua, teléfono o predial con antigüedad no mayor a 3 meses.', TRUE, 1, 2),
('Acta de nacimiento', 'Copia legible del acta de nacimiento.', TRUE, 1, 3),
('Fotografía tamaño infantil', 'Fotografía reciente, a color o blanco y negro.', FALSE, 1, 4),
-- 2. Constancia de no adeudo
('Identificación oficial', 'Credencial INE o pasaporte del propietario o del titular del contrato.', TRUE, 2, 1),
('Último recibo pagado', 'Recibo de predial o de agua que demuestre que la cuenta está al corriente.', TRUE, 2, 2),
('Carta poder', 'Solo si quien solicita no es el propietario: carta poder simple con dos testigos.', FALSE, 2, 3),
-- 3. Licencia SARE
('Identificación oficial', 'Credencial INE o pasaporte del titular del negocio.', TRUE, 3, 1),
('Constancia de situación fiscal', 'Emitida por el SAT, con el RFC del titular.', TRUE, 3, 2),
('Comprobante de domicilio del negocio', 'Con antigüedad no mayor a 3 meses.', TRUE, 3, 3),
('Dictamen de uso de suelo', 'Vigente y compatible con el giro del negocio.', TRUE, 3, 4),
('Recibo de predial al corriente', 'Del inmueble donde operará el negocio.', TRUE, 3, 5),
('Contrato de arrendamiento', 'Solo si el local es rentado.', FALSE, 3, 6),
-- 4. Alineamiento y número oficial
('Identificación oficial', 'Credencial INE o pasaporte del propietario.', TRUE, 4, 1),
('Documento que acredite la propiedad', 'Escritura, título o constancia de posesión del predio.', TRUE, 4, 2),
('Recibo de predial vigente', 'Del año en curso.', TRUE, 4, 3),
('Croquis de ubicación', 'Ubicación del predio con calles cercanas y referencias.', TRUE, 4, 4),
-- 5. Uso de suelo
('Documento que acredite la propiedad', 'Escritura, contrato de compraventa o donación.', TRUE, 5, 1),
('Recibo de predial al corriente', 'O constancia de no adeudo.', TRUE, 5, 2),
('Plano o croquis del predio', 'Con medidas y cuadro de áreas.', TRUE, 5, 3),
('Fotografías del predio', 'Dos interiores y dos exteriores, en un solo PDF.', TRUE, 5, 4),
('Alineamiento y número oficial', 'Constancia vigente.', FALSE, 5, 5),
-- 6. Licencia de construcción
('Identificación oficial', 'Credencial INE o pasaporte del propietario.', TRUE, 6, 1),
('Documento que acredite la propiedad', 'Escritura, título o constancia de posesión.', TRUE, 6, 2),
('Alineamiento y número oficial', 'Constancia vigente.', TRUE, 6, 3),
('Recibo de predial al corriente', 'Del año en curso.', TRUE, 6, 4),
('Dibujo de la construcción', 'Croquis que ubique la obra dentro del predio, con medidas.', TRUE, 6, 5),
-- 7. Servicio Militar Nacional
('Acta de nacimiento', 'Copia legible.', TRUE, 7, 1),
('CURP', 'Formato reciente.', TRUE, 7, 2),
('Comprobante de domicilio', 'Con antigüedad no mayor a 3 meses.', TRUE, 7, 3),
('Comprobante de estudios', 'Certificado o constancia del último grado de estudios.', TRUE, 7, 4),
('Fotografía tamaño cartilla', '35 × 45 mm, de frente.', TRUE, 7, 5);
