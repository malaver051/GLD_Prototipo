-- =====================================================================
-- ms-tramites · Esquema "tramites"
-- Basado en el modelo relacional del proyecto: TIPO_TRAMITE, REQUISITO_TRAMITE,
-- SOLICITUD_TRAMITE, HISTORIAL, PAGO y DOCUMENTO.
--
-- Base de datos por servicio: las referencias a MUNICIPIO y USUARIO (que viven en
-- ms-identidad) se guardan como identificadores sin llave foránea.
-- Las columnas marcadas como EXTENSIÓN no están en el modelo original.
-- =====================================================================

CREATE TABLE tipo_tramite (
    id_tramite              SERIAL         PRIMARY KEY,
    nombre                  VARCHAR(150)   NOT NULL,
    descripcion             VARCHAR(600)   NOT NULL,
    costo                   NUMERIC(12, 2) NOT NULL CHECK (costo >= 0),
    plazo_dias              INTEGER        NOT NULL CHECK (plazo_dias > 0),     -- días hábiles
    estado                  VARCHAR(20)    NOT NULL DEFAULT 'HABILITADO'
                            CHECK (estado IN ('HABILITADO', 'DESHABILITADO')),
    municipio_id_municipio  INTEGER,                                         -- NULL = plantilla para municipios nuevos
    -- EXTENSIÓN
    dependencia             VARCHAR(120)   NOT NULL,
    fecha_actualizacion     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE requisito_tramite (
    id_requisito             SERIAL        PRIMARY KEY,
    nombre                   VARCHAR(150)  NOT NULL,
    descripcion              VARCHAR(400)  NOT NULL,
    obligatorio              BOOLEAN       NOT NULL DEFAULT TRUE,
    tipo_tramite_id_tramite  INTEGER       NOT NULL REFERENCES tipo_tramite (id_tramite) ON DELETE CASCADE,
    -- EXTENSIÓN: orden en el formulario
    orden                    INTEGER       NOT NULL DEFAULT 0
);

CREATE TABLE solicitud_tramite (
    id_solicitud             SERIAL         PRIMARY KEY,
    numero_radicado          VARCHAR(30)    NOT NULL UNIQUE,
    fecha_radicacion         TIMESTAMP      NOT NULL,
    estado                   VARCHAR(30)    NOT NULL
                             CHECK (estado IN ('PENDIENTE_PAGO', 'EN_REVISION', 'REQUIERE_INFORMACION', 'APROBADA', 'RECHAZADA')),
    observaciones            VARCHAR(1000),
    tipo_tramite_id_tramite  INTEGER        NOT NULL REFERENCES tipo_tramite (id_tramite),
    usuario_id_usuario       INTEGER        NOT NULL,                        -- ciudadano (ms-identidad)
    -- EXTENSIÓN: datos copiados al radicar (base de datos por servicio)
    municipio_id_municipio   INTEGER        NOT NULL,
    municipio_nombre         VARCHAR(120)   NOT NULL,
    municipio_estado         VARCHAR(80)    NOT NULL,
    nombre_ciudadano         VARCHAR(150)   NOT NULL,
    correo_ciudadano         VARCHAR(150)   NOT NULL,
    documento_ciudadano      VARCHAR(60)    NOT NULL,
    costo                    NUMERIC(12, 2) NOT NULL,
    fecha_limite             DATE,                                           -- se fija al entrar a revisión
    fecha_resolucion         TIMESTAMP
);

CREATE TABLE historial (
    id_historial                     SERIAL       PRIMARY KEY,
    estado_anterior                  VARCHAR(30),
    estado_nuevo                     VARCHAR(30)  NOT NULL,
    fecha_cambio                     TIMESTAMP    NOT NULL,
    motivo                           VARCHAR(1000) NOT NULL,
    origen_cambio                    VARCHAR(20)  NOT NULL
                                     CHECK (origen_cambio IN ('CIUDADANO', 'FUNCIONARIO', 'PASARELA', 'SISTEMA')),
    usuario_id_usuario               INTEGER,
    solicitud_tramite_id_solicitud   INTEGER      NOT NULL REFERENCES solicitud_tramite (id_solicitud) ON DELETE CASCADE,
    -- EXTENSIÓN
    nombre_usuario                   VARCHAR(150)
);

CREATE TABLE pago (
    id_pago                          SERIAL         PRIMARY KEY,
    referencia_orden                 VARCHAR(30)    NOT NULL UNIQUE,
    valor                            NUMERIC(12, 2) NOT NULL,
    fecha_creacion                   TIMESTAMP      NOT NULL,
    fecha_confirmacion               TIMESTAMP,
    medio_pago                       VARCHAR(20)    NOT NULL,
    estado                           VARCHAR(20)    NOT NULL
                                     CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO', 'CANCELADO')),
    solicitud_tramite_id_solicitud   INTEGER        NOT NULL REFERENCES solicitud_tramite (id_solicitud) ON DELETE CASCADE,
    -- EXTENSIÓN
    banco                            VARCHAR(80)
);

CREATE TABLE documento (
    id_documento                     SERIAL       PRIMARY KEY,
    nombre_archivo                   VARCHAR(200) NOT NULL,
    tipo_documento                   VARCHAR(20)  NOT NULL CHECK (tipo_documento IN ('ADJUNTO', 'OFICIAL')),
    ruta_archivo                     VARCHAR(400) NOT NULL,
    fecha_carga                      TIMESTAMP    NOT NULL,
    estado_firma                     VARCHAR(20)  NOT NULL CHECK (estado_firma IN ('NO_APLICA', 'FIRMADO')),
    codigo_verificacion              VARCHAR(30)  UNIQUE,
    usuario_id_usuario               INTEGER      NOT NULL,
    solicitud_tramite_id_solicitud   INTEGER      NOT NULL REFERENCES solicitud_tramite (id_solicitud) ON DELETE CASCADE,
    -- EXTENSIÓN
    requisito_id_requisito           INTEGER      REFERENCES requisito_tramite (id_requisito) ON DELETE SET NULL,
    tipo_contenido                   VARCHAR(80)  NOT NULL,
    tamano_bytes                     BIGINT       NOT NULL,
    sello_sha256                     VARCHAR(64)
);

CREATE SEQUENCE radicado_seq START 1;

CREATE INDEX ix_tipo_tramite_municipio ON tipo_tramite (municipio_id_municipio);
CREATE INDEX ix_solicitud_municipio ON solicitud_tramite (municipio_id_municipio, estado);
CREATE INDEX ix_solicitud_usuario ON solicitud_tramite (usuario_id_usuario);
CREATE INDEX ix_historial_solicitud ON historial (solicitud_tramite_id_solicitud);

-- Un municipio no puede tener dos trámites con el mismo nombre
CREATE UNIQUE INDEX uq_tipo_tramite_nombre
    ON tipo_tramite (municipio_id_municipio, lower(nombre))
    WHERE municipio_id_municipio IS NOT NULL;
