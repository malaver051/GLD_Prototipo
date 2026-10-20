-- =====================================================================
-- ms-identidad · Esquema "identidad"
-- Basado en el modelo relacional del proyecto (MUNICIPIO, USUARIO, ROL,
-- Relation_5 → USUARIO_ROL). Las columnas marcadas como EXTENSIÓN no están
-- en el modelo original y se agregan para soportar la autenticación.
-- =====================================================================

CREATE TABLE municipio (
    id_municipio         SERIAL       PRIMARY KEY,
    codigo_municipio     VARCHAR(5)   NOT NULL UNIQUE,          -- Clave geoestadística INEGI (estado + municipio)
    nombre               VARCHAR(120) NOT NULL,
    departamento         VARCHAR(80)  NOT NULL,                 -- En México: entidad federativa (estado)
    estado               VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO'
                         CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    fecha_incorporacion  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rol (
    id_rol       SERIAL       PRIMARY KEY,
    nombre       VARCHAR(30)  NOT NULL UNIQUE,
    descripcion  VARCHAR(200) NOT NULL,
    ambito       VARCHAR(20)  NOT NULL
                 CHECK (ambito IN ('CIUDADANIA', 'GOBIERNO', 'PLATAFORMA'))
);

CREATE TABLE usuario (
    id_usuario                   SERIAL       PRIMARY KEY,
    identificador_autenticacion  VARCHAR(150) NOT NULL UNIQUE,  -- correo, o "admin"
    nombre_completo              VARCHAR(150) NOT NULL,
    tipo_documento               VARCHAR(20)  NOT NULL,
    numero_documento             VARCHAR(30)  NOT NULL,
    correo                       VARCHAR(150) NOT NULL UNIQUE,
    telefono                     VARCHAR(20)  NOT NULL,
    estado                       VARCHAR(20)  NOT NULL
                                 CHECK (estado IN ('PENDIENTE_MFA', 'ACTIVO', 'INACTIVO')),
    -- EXTENSIÓN: multi-tenant (el administrador de plataforma no pertenece a un municipio)
    id_municipio                 INTEGER      REFERENCES municipio (id_municipio),
    -- EXTENSIÓN: credenciales y segundo factor
    contrasena_hash              VARCHAR(100) NOT NULL,
    debe_cambiar_clave           BOOLEAN      NOT NULL DEFAULT FALSE,
    mfa_secreto                  VARCHAR(64),
    mfa_activo                   BOOLEAN      NOT NULL DEFAULT FALSE,
    mfa_ultimo_paso              BIGINT,                        -- evita reutilizar un mismo código TOTP
    fecha_creacion               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso                TIMESTAMP,
    CONSTRAINT uq_usuario_documento UNIQUE (tipo_documento, numero_documento)
);

-- Relation_5 del modelo relacional
CREATE TABLE usuario_rol (
    usuario_id_usuario  INTEGER NOT NULL REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    rol_id_rol          INTEGER NOT NULL REFERENCES rol (id_rol),
    PRIMARY KEY (usuario_id_usuario, rol_id_rol)
);

-- EXTENSIÓN: enlace de recuperación de contraseña para ciudadanos
CREATE TABLE token_recuperacion (
    id_token            SERIAL      PRIMARY KEY,
    usuario_id_usuario  INTEGER     NOT NULL REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    token_hash          VARCHAR(64) NOT NULL UNIQUE,            -- SHA-256 del token; el token en claro nunca se guarda
    fecha_expiracion    TIMESTAMP   NOT NULL,
    usado               BOOLEAN     NOT NULL DEFAULT FALSE,
    fecha_creacion      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- EXTENSIÓN: solicitudes de restablecimiento de funcionarios (las atiende el administrador)
CREATE TABLE solicitud_restablecimiento (
    id_solicitud        SERIAL      PRIMARY KEY,
    usuario_id_usuario  INTEGER     NOT NULL REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    estado              VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                        CHECK (estado IN ('PENDIENTE', 'ATENDIDA')),
    fecha_solicitud     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_atencion      TIMESTAMP,
    atendida_por        INTEGER     REFERENCES usuario (id_usuario)
);

-- Un funcionario solo puede tener una solicitud pendiente a la vez
CREATE UNIQUE INDEX uq_solicitud_pendiente
    ON solicitud_restablecimiento (usuario_id_usuario)
    WHERE estado = 'PENDIENTE';

CREATE INDEX ix_usuario_municipio ON usuario (id_municipio);
CREATE INDEX ix_token_usuario ON token_recuperacion (usuario_id_usuario);
