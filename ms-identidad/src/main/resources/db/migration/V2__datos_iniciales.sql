-- =====================================================================
-- Datos iniciales: roles del prototipo y municipios de México
-- El usuario "admin" lo crea la aplicación al iniciar (InicializadorAdministrador),
-- para que su contraseña se cifre con el mismo BCrypt que usa el sistema.
-- =====================================================================

INSERT INTO rol (nombre, descripcion, ambito) VALUES
    ('CIUDADANO',   'Persona que realiza trámites y consultas en el portal ciudadano', 'CIUDADANIA'),
    ('FUNCIONARIO', 'Servidor público que gestiona solicitudes en la consola municipal', 'GOBIERNO'),
    ('ADMIN',       'Administrador de la plataforma; crea y gestiona las cuentas de funcionarios', 'PLATAFORMA');

-- codigo_municipio = clave geoestadística del INEGI (2 dígitos de entidad + 3 de municipio)
-- departamento     = entidad federativa
INSERT INTO municipio (codigo_municipio, nombre, departamento) VALUES
    -- Capitales de las 32 entidades federativas (en la Ciudad de México, la alcaldía Cuauhtémoc)
    ('01001', 'Aguascalientes',          'Aguascalientes'),
    ('02002', 'Mexicali',                'Baja California'),
    ('03003', 'La Paz',                  'Baja California Sur'),
    ('04002', 'Campeche',                'Campeche'),
    ('05030', 'Saltillo',                'Coahuila de Zaragoza'),
    ('06002', 'Colima',                  'Colima'),
    ('07101', 'Tuxtla Gutiérrez',        'Chiapas'),
    ('08019', 'Chihuahua',               'Chihuahua'),
    ('09015', 'Cuauhtémoc',              'Ciudad de México'),
    ('10005', 'Durango',                 'Durango'),
    ('11015', 'Guanajuato',              'Guanajuato'),
    ('12029', 'Chilpancingo de los Bravo','Guerrero'),
    ('13048', 'Pachuca de Soto',         'Hidalgo'),
    ('14039', 'Guadalajara',             'Jalisco'),
    ('15106', 'Toluca',                  'México'),
    ('16053', 'Morelia',                 'Michoacán de Ocampo'),
    ('17007', 'Cuernavaca',              'Morelos'),
    ('18017', 'Tepic',                   'Nayarit'),
    ('19039', 'Monterrey',               'Nuevo León'),
    ('20067', 'Oaxaca de Juárez',        'Oaxaca'),
    ('21114', 'Puebla',                  'Puebla'),
    ('22014', 'Querétaro',               'Querétaro'),
    ('23004', 'Othón P. Blanco',         'Quintana Roo'),
    ('24028', 'San Luis Potosí',         'San Luis Potosí'),
    ('25006', 'Culiacán',                'Sinaloa'),
    ('26030', 'Hermosillo',              'Sonora'),
    ('27004', 'Centro',                  'Tabasco'),
    ('28041', 'Victoria',                'Tamaulipas'),
    ('29033', 'Tlaxcala',                'Tlaxcala'),
    ('30087', 'Xalapa',                  'Veracruz de Ignacio de la Llave'),
    ('31050', 'Mérida',                  'Yucatán'),
    ('32056', 'Zacatecas',               'Zacatecas'),
    -- Municipios con mayor población o relevancia económica
    ('09007', 'Iztapalapa',              'Ciudad de México'),
    ('14120', 'Zapopan',                 'Jalisco'),
    ('02004', 'Tijuana',                 'Baja California'),
    ('08037', 'Juárez',                  'Chihuahua'),
    ('11020', 'León',                    'Guanajuato'),
    ('15033', 'Ecatepec de Morelos',     'México'),
    ('15058', 'Nezahualcóyotl',          'México'),
    ('12001', 'Acapulco de Juárez',      'Guerrero'),
    ('23005', 'Benito Juárez (Cancún)',  'Quintana Roo'),
    ('05035', 'Torreón',                 'Coahuila de Zaragoza'),
    ('30193', 'Veracruz',                'Veracruz de Ignacio de la Llave');
