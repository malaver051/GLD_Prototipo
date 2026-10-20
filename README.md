# GLD · Prototipo funcional — Fase 1: inicio de sesión

Plataforma **Gobiernos Locales Digitales (GLD)** · Grupo 3 · 2026-2

Esta fase implementa el microservicio **Identidad y municipios** de la arquitectura y las pantallas de acceso:

| Quién | Cómo ingresa |
|---|---|
| **Administrador** | Usuario `admin`, contraseña `admin`. Es la única cuenta con rol ADMIN y su contraseña no cambia. |
| **Funcionario** | Solo con correo `@gobierno.com`. El admin crea la cuenta, asigna rol y municipio, y el sistema genera una contraseña temporal. En el primer ingreso debe cambiarla. |
| **Ciudadano** | Se registra solo con su correo. Siempre ingresa con contraseña + código de **Microsoft Authenticator** (MFA). |

Recuperación de contraseña:
- **Ciudadano:** recibe un enlace por correo, válido 30 minutos y de un solo uso.
- **Funcionario:** su solicitud le llega al admin, que le envía una nueva contraseña temporal.

> **Correo simulado:** no hay servidor de correo. Los correos (contraseñas temporales y enlaces de recuperación) se imprimen en la **consola de ms-identidad**, en un bloque que empieza con `CORREO SIMULADO`.

---

## 1. Requisitos

- **Java 21** (JDK)
- **Node.js 20** o superior
- **PostgreSQL** con la base `gld` creada en pgAdmin (localhost:5432, usuario `postgres`, contraseña `1234`)
- **VS Code** con las extensiones recomendadas (al abrir la carpeta, VS Code las sugiere):
  - Extension Pack for Java
  - Spring Boot Extension Pack
- **Microsoft Authenticator** en un celular, para probar el ingreso de ciudadanos

No hace falta crear tablas: al iniciar, **Flyway** crea el esquema `identidad`, las tablas, los roles y 43 municipios de México.

## 2. Ejecutar el backend (ms-identidad)

1. En VS Code, abre la carpeta `gld-prototipo`.
2. Espera a que la extensión de Java descargue las dependencias de Maven (barra inferior). La primera vez tarda unos minutos.
3. Abre la vista **Run and Debug**, elige **ms-identidad** y presiona ▶. También puedes abrir `IdentidadApplication.java` y pulsar **Run** sobre el método `main`.
   - Si tienes Maven instalado, también funciona `cd ms-identidad` y luego `mvn spring-boot:run`.
4. Cuando la consola muestre `Started IdentidadApplication`, el servicio está en `http://localhost:8081/api/v1/identidad`.

Prueba rápida: abre `http://localhost:8081/api/v1/identidad/municipios` en el navegador; debe mostrar la lista de municipios.

## 3. Ejecutar el frontend

En una terminal de VS Code:

```bash
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173`. Vite reenvía las llamadas `/api/v1/identidad` al backend del puerto 8081.

## 4. Guion de prueba

1. **Admin:** ingresa con `admin` / `admin` y pulsa **Crear funcionario**, con un correo como `laura.mendez@gobierno.com`. En la consola del backend aparece la contraseña temporal.
2. **Funcionario:** sal, ingresa con ese correo y la contraseña temporal; el sistema exige crear una contraseña propia y luego abre la consola municipal.
3. **Ciudadano:** en la pantalla de ingreso pulsa **Crea tu cuenta ciudadana**, completa los datos (CURP de ejemplo: `GORA900315MJCMZN05`), elige la contraseña, escanea el QR con Microsoft Authenticator (**+ → Otra cuenta (Google, Facebook, etc.)**) y escribe el código.
4. **MFA:** sal e ingresa de nuevo como ciudadano; ahora el sistema pide el código de 6 dígitos de la app.
5. **Recuperación:**
   - Ciudadano: en **¿Olvidaste tu contraseña?** escribe su correo; copia el enlace que aparece en la consola del backend y ábrelo en el navegador.
   - Funcionario: escribe su correo `@gobierno.com`; la solicitud aparece en la pestaña **Solicitudes de contraseña** del admin.

Reglas de contraseña: mínimo 8 caracteres, al menos una mayúscula, un número y un símbolo.

## 5. Estructura

```
gld-prototipo/
├── ms-identidad/                      Spring Boot 3.5 · Java 21 · PostgreSQL
│   └── src/main/java/com/gld/identidad/
│       ├── controller/                MVC · Controlador (REST)
│       ├── service/                   Lógica de negocio por caso de uso
│       ├── model/                     MVC · Modelo (entidades JPA del modelo relacional)
│       ├── repository/                Acceso a datos (Spring Data JPA)
│       ├── dto/                       Datos que entran y salen de la API
│       ├── security/                  JWT, filtro y reglas de acceso por rol
│       ├── support/                   Política de contraseñas, TOTP, correo simulado
│       ├── exception/                 Errores en formato RFC 7807
│       └── config/                    Beans, propiedades y creación de "admin"
│   └── src/main/resources/db/migration/   Scripts Flyway (esquema y datos iniciales)
└── frontend/                          React 19 + Vite
    └── src/
        ├── paginas/                   MVC · Vista (pantallas por rol)
        ├── componentes/               Piezas de interfaz reutilizables
        ├── servicios/                 Llamadas a la API
        ├── contexto/                  Estado de la sesión
        └── estilos/                   Tokens de diseño y estilos
```

### MVC

- **Modelo:** entidades JPA en `model/`, más repositorios y servicios.
- **Vista:** la aplicación React; el backend solo responde JSON.
- **Controlador:** las clases `*Controller` reciben la petición, delegan en un servicio y devuelven un DTO.

### Principios GRASP aplicados

| Principio | Dónde |
|---|---|
| Experto en información | `Usuario` decide `requiereMfa()`, `esFuncionario()`, `aceptaPasoMfa()`; `TokenRecuperacion.estaVigente()`; `Rol.esAsignablePorAdministrador()` |
| Creador | `Usuario.crearCiudadano/crearFuncionario/crearAdministrador` construyen cuentas consistentes |
| Controlador | `AutenticacionController`, `AdministracionController`… solo coordinan, sin reglas de negocio |
| Alta cohesión | Un servicio por responsabilidad: autenticación, segundo factor, registro, recuperación, gestión de usuarios |
| Bajo acoplamiento | Los controladores dependen de servicios; los servicios, de repositorios e interfaces |
| Fabricación pura | `JwtService`, `PoliticaContrasena`, `GeneradorTotp`, `GeneradorClaveTemporal`, `ValidadorDocumento` |
| Indirección | Repositorios entre servicios y base de datos; `NotificadorCorreo` entre servicios y el envío |
| Polimorfismo / Variaciones protegidas | `NotificadorCorreo` (hoy consola; mañana SMTP sin cambiar los servicios) |

## 6. API de la fase 1

Base: `http://localhost:8081/api/v1/identidad`

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/auth/ingreso` | Público |
| POST | `/auth/registro` | Público |
| GET | `/auth/mfa/configuracion` | Token temporal (vincular MFA) |
| POST | `/auth/mfa/activacion` | Token temporal (vincular MFA) |
| POST | `/auth/mfa/verificacion` | Token temporal (código MFA) |
| POST | `/auth/clave-inicial` | Token temporal (cambio de clave) |
| POST | `/auth/recuperacion` | Público |
| GET | `/auth/recuperacion/enlace?token=` | Público |
| POST | `/auth/recuperacion/restablecer` | Público |
| GET | `/municipios` | Público |
| GET | `/usuarios/me` | Cualquier rol |
| GET, POST | `/admin/usuarios` | ADMIN |
| PATCH | `/admin/usuarios/{id}/estado` | ADMIN |
| POST | `/admin/usuarios/{id}/restablecimiento-clave` | ADMIN |
| GET | `/admin/roles` | ADMIN |
| GET | `/admin/solicitudes-restablecimiento` | ADMIN |
| POST | `/admin/solicitudes-restablecimiento/{id}/atencion` | ADMIN |

**Tokens:** cada paso del ingreso responde `{ paso, token, usuario }`.
- Si `paso` es `ACCESO_CONCEDIDO`, el token abre la aplicación durante 60 minutos.
- En los demás pasos (`VERIFICAR_MFA`, `CONFIGURAR_MFA`, `CAMBIAR_CLAVE`), el token es temporal: dura 10 minutos y solo sirve para completar ese paso.

## 7. Cambios respecto al modelo relacional

- `USUARIO` agrega:
  - `id_municipio`, porque el sistema es multi-tenant.
  - `contrasena_hash` (BCrypt) y `debe_cambiar_clave`.
  - `mfa_secreto`, `mfa_activo` y `mfa_ultimo_paso`; este último impide reutilizar un código.
  - `fecha_creacion` y `ultimo_acceso`.
- `Relation_5` se llama `usuario_rol`.
- Tablas nuevas: `token_recuperacion` (enlaces de ciudadanos) y `solicitud_restablecimiento` (pedidos de funcionarios).
- `MUNICIPIO.departamento` guarda la **entidad federativa**, porque el caso es de México. `codigo_municipio` es la clave geoestadística del INEGI.
- Roles: `CIUDADANO`, `FUNCIONARIO` y `ADMIN`.

## 8. Problemas comunes

- **`password authentication failed for user "postgres"`:** revisa la contraseña en `ms-identidad/src/main/resources/application.properties`.
- **`database "gld" does not exist`:** créala en pgAdmin (clic derecho en *Databases → Create*).
- **El código de Authenticator siempre falla:** activa la hora automática del celular y del computador; el código depende de la hora.
- **El frontend dice "No hay conexión con el servidor":** el backend no está en ejecución o no está en el puerto 8081.
- **Para empezar de cero:** en pgAdmin ejecuta `DROP SCHEMA identidad CASCADE;` y reinicia el backend.
