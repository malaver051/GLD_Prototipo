-- =====================================================================
-- Dependencias del prototipo y centro de los 43 municipios precargados en ms-identidad.
-- Las coordenadas son aproximadas al centro de la cabecera municipal y solo sirven
-- para abrir el mapa y validar que el reporte esté cerca del municipio.
-- =====================================================================

INSERT INTO dependencia (nombre, descripcion, categoria_sugerida) VALUES
('Obras Públicas',                     'Baches, banquetas, puentes y señalización vial.',                         'VIAS'),
('Alumbrado Público',                  'Luminarias apagadas, postes dañados y cableado.',                         'ALUMBRADO'),
('Servicios Públicos · Aseo y Limpia', 'Basura acumulada, recolección y limpieza de calles.',                     'ASEO'),
('Seguridad Pública',                  'Situaciones de riesgo, vandalismo y apoyo de policía municipal.',         'SEGURIDAD'),
('Protección Civil',                   'Árboles caídos, encharcamientos y riesgos por lluvias.',                  NULL),
('Agua Potable y Alcantarillado',      'Fugas de agua, coladeras tapadas o sin tapa y drenaje.',                  NULL),
('Parques y Jardines',                 'Áreas verdes, poda y mantenimiento de parques.',                          NULL),
('Atención Ciudadana',                 'Reportes que no corresponden a otra dependencia.',                        'OTRO');

INSERT INTO centro_municipio (codigo_municipio, latitud, longitud) VALUES
('01001', 21.881800, -102.291600),  -- Aguascalientes
('02002', 32.624500, -115.452300),  -- Mexicali
('02004', 32.514900, -117.038200),  -- Tijuana
('03003', 24.142600, -110.312800),  -- La Paz
('04002', 19.830100,  -90.534900),  -- Campeche
('05030', 25.423200, -101.005300),  -- Saltillo
('05035', 25.542800, -103.406800),  -- Torreón
('06002', 19.243300, -103.725000),  -- Colima
('07101', 16.753100,  -93.115600),  -- Tuxtla Gutiérrez
('08019', 28.635300, -106.088900),  -- Chihuahua
('08037', 31.690400, -106.424500),  -- Juárez
('09007', 19.357400,  -99.092700),  -- Iztapalapa
('09015', 19.432600,  -99.144000),  -- Cuauhtémoc
('10005', 24.027700, -104.653200),  -- Durango
('11015', 21.019000, -101.257400),  -- Guanajuato
('11020', 21.125000, -101.686000),  -- León
('12001', 16.853100,  -99.823700),  -- Acapulco de Juárez
('12029', 17.551500,  -99.500600),  -- Chilpancingo de los Bravo
('13048', 20.101100,  -98.759100),  -- Pachuca de Soto
('14039', 20.676700, -103.347500),  -- Guadalajara
('14120', 20.721400, -103.391500),  -- Zapopan
('15033', 19.601800,  -99.050700),  -- Ecatepec de Morelos
('15058', 19.400600,  -99.014800),  -- Nezahualcóyotl
('15106', 19.282600,  -99.655700),  -- Toluca
('16053', 19.706000, -101.195000),  -- Morelia
('17007', 18.924200,  -99.221600),  -- Cuernavaca
('18017', 21.503900, -104.894600),  -- Tepic
('19039', 25.686600, -100.316100),  -- Monterrey
('20067', 17.073200,  -96.726600),  -- Oaxaca de Juárez
('21114', 19.041400,  -98.206300),  -- Puebla
('22014', 20.588800, -100.389900),  -- Querétaro
('23004', 18.500100,  -88.296100),  -- Othón P. Blanco (Chetumal)
('23005', 21.161900,  -86.851500),  -- Benito Juárez (Cancún)
('24028', 22.156500, -100.985500),  -- San Luis Potosí
('25006', 24.809100, -107.394000),  -- Culiacán
('26030', 29.072900, -110.955900),  -- Hermosillo
('27004', 17.989500,  -92.947500),  -- Centro (Villahermosa)
('28041', 23.736900,  -99.141100),  -- Victoria
('29033', 19.313900,  -98.240400),  -- Tlaxcala
('30087', 19.543800,  -96.910200),  -- Xalapa
('30193', 19.173800,  -96.134200),  -- Veracruz
('31050', 20.967400,  -89.592600),  -- Mérida
('32056', 22.770900, -102.583200);  -- Zacatecas
