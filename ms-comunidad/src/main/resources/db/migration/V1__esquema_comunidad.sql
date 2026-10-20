-- =====================================================================
-- ms-comunidad · Esquema "comunidad"
-- Épica GLD-E2 (Reporte de incidencias). Basado en la entidad INCIDENCIA del
-- modelo relacional y en el contrato OpenAPI 07-comunidad.yaml.
--
-- Base de datos por servicio: MUNICIPIO y USUARIO viven en ms-identidad, así que
-- aquí se guardan sus identificadores (sin llave foránea) y una copia de los datos
-- que se muestran. Las columnas marcadas como EXTENSIÓN no están en el modelo original.
-- =====================================================================

-- Dependencias municipales a las que se asigna una incidencia (lista fija del prototipo)
CREATE TABLE dependencia (
    id_dependencia      SERIAL        PRIMARY KEY,
    nombre              VARCHAR(120)  NOT NULL UNIQUE,
    descripcion         VARCHAR(300)  NOT NULL,
    -- Categoría que normalmente atiende; el funcionario la ve como sugerencia
    categoria_sugerida  VARCHAR(20)   CHECK (categoria_sugerida IN ('VIAS', 'ALUMBRADO', 'ASEO', 'SEGURIDAD', 'OTRO'))
);

-- Centro de cada municipio para abrir el mapa (clave INEGI de ms-identidad)
CREATE TABLE centro_municipio (
    codigo_municipio  VARCHAR(5)     PRIMARY KEY,
    latitud           NUMERIC(9, 6)  NOT NULL,
    longitud          NUMERIC(9, 6)  NOT NULL
);

CREATE TABLE incidencia (
    id_incidencia            SERIAL         PRIMARY KEY,
    numero_seguimiento       VARCHAR(30)    NOT NULL UNIQUE,
    categoria                VARCHAR(20)    NOT NULL
                             CHECK (categoria IN ('VIAS', 'ALUMBRADO', 'ASEO', 'SEGURIDAD', 'OTRO')),
    descripcion              VARCHAR(1000)  NOT NULL,
    latitud                  NUMERIC(9, 6)  NOT NULL CHECK (latitud BETWEEN -90 AND 90),
    longitud                 NUMERIC(9, 6)  NOT NULL CHECK (longitud BETWEEN -180 AND 180),
    estado                   VARCHAR(20)    NOT NULL
                             CHECK (estado IN ('REPORTADA', 'ASIGNADA', 'EN_ATENCION', 'RESUELTA', 'CERRADA')),
    fecha_reporte            TIMESTAMP      NOT NULL,
    usuario_id_usuario       INTEGER        NOT NULL,                       -- ciudadano (ms-identidad)
    municipio_id_municipio   INTEGER        NOT NULL,
    -- EXTENSIÓN: referencia escrita del lugar ("frente a la escuela")
    referencia               VARCHAR(200),
    -- EXTENSIÓN: datos copiados al reportar
    codigo_municipio         VARCHAR(5)     NOT NULL,
    municipio_nombre         VARCHAR(120)   NOT NULL,
    municipio_estado         VARCHAR(80)    NOT NULL,
    nombre_ciudadano         VARCHAR(150)   NOT NULL,
    correo_ciudadano         VARCHAR(150)   NOT NULL,
    -- EXTENSIÓN: foto opcional (JPG o PNG)
    foto_ruta                VARCHAR(300),
    foto_nombre              VARCHAR(200),
    foto_tipo                VARCHAR(50),
    foto_tamano_bytes        BIGINT,
    -- EXTENSIÓN: atención
    dependencia_id_dependencia INTEGER      REFERENCES dependencia (id_dependencia),
    solucion                 VARCHAR(1000),
    fecha_actualizacion      TIMESTAMP      NOT NULL,
    fecha_resolucion         TIMESTAMP,
    fecha_cierre             TIMESTAMP,
    reaperturas              INTEGER        NOT NULL DEFAULT 0 CHECK (reaperturas BETWEEN 0 AND 1),
    -- EXTENSIÓN: calificación del ciudadano (HU-12)
    calificacion             INTEGER        CHECK (calificacion BETWEEN 0 AND 10),
    comentario_calificacion  VARCHAR(500)
);

CREATE INDEX ix_incidencia_municipio ON incidencia (municipio_id_municipio, estado);
CREATE INDEX ix_incidencia_ciudadano ON incidencia (usuario_id_usuario);

CREATE TABLE historial_incidencia (
    id_historial              SERIAL        PRIMARY KEY,
    incidencia_id_incidencia  INTEGER       NOT NULL REFERENCES incidencia (id_incidencia) ON DELETE CASCADE,
    estado_anterior           VARCHAR(20),
    estado_nuevo              VARCHAR(20)   NOT NULL,
    fecha_cambio              TIMESTAMP     NOT NULL,
    comentario                VARCHAR(1000) NOT NULL,
    origen_cambio             VARCHAR(20)   NOT NULL CHECK (origen_cambio IN ('CIUDADANO', 'FUNCIONARIO', 'SISTEMA')),
    usuario_id_usuario        INTEGER,
    nombre_usuario            VARCHAR(150)
);

CREATE SEQUENCE seguimiento_seq START 1;
