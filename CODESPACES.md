# Correr el prototipo GLD en GitHub Codespaces

Codespaces abre el proyecto en un computador en la nube de GitHub, con VS Code en el navegador. Trae Java 21, Maven, Node 22 y PostgreSQL ya instalados, con la misma base que en local (`gld`, usuario `postgres`, clave `1234`). No hay que instalar nada en tu computador.

## 1. Crear el Codespace (solo la primera vez)

1. En GitHub abre el repositorio y haz clic en **Code → Codespaces → Create codespace on main**.
   - Con la máquina de 2 núcleos (8 GB) basta. Si quieres que arranque más rápido, en **⋯ → New with options** elige 4 núcleos.
2. Espera a que termine de preparar el entorno (la primera vez tarda unos 5 minutos). Al final se ejecuta `npm install` en `frontend`.
3. Espera también a que VS Code termine de importar los tres proyectos Maven (abajo a la derecha dice «Java: Ready»).

## 2. Encender el prototipo (cada vez)

1. **Backend:** en el panel **Ejecutar y depurar** (Ctrl+Shift+D) elige **Backend completo (3 microservicios)** y dale ▶. Espera a que los tres digan `Started … Application` en la consola.
2. **Frontend:** abre una terminal (Ctrl+ñ) y escribe:
   ```
   cd frontend
   npm run dev
   ```
3. Se abre una pestaña con el prototipo. Si no se abre, ve a la pestaña **Puertos** y haz clic en el globo del puerto **5173 (GLD · frontend)**.

La dirección tiene la forma `https://<nombre-del-codespace>-5173.app.github.dev` y funciona desde cualquier computador.

### Para la exposición: modo rápido

`npm run dev` sirve cientos de archivos sueltos y, a través de Codespaces, la primera carga es lenta. Para la demo usa la versión compilada, que es un solo archivo y carga mucho más rápido, en la misma dirección del puerto 5173:

```
cd frontend
npm run demo
```

Tarda unos 20 segundos en compilar y queda lista cuando dice `Local: http://localhost:5173/`. Si cambias código del frontend, detenla (Ctrl+C) y vuelve a correrla.

## 3. Compartirlo con otras personas (por ejemplo, el profesor)

Por defecto solo tú puedes abrir la dirección. Para que cualquiera con el enlace la abra: pestaña **Puertos** → clic derecho en **5173** → **Visibilidad del puerto → Pública**.

## 4. Cosas a tener en cuenta

- **Se apaga solo** tras 30 minutos sin uso. Para volver a usarlo: GitHub → **Code → Codespaces** → abre el mismo Codespace y repite el paso 2. Los datos (usuarios, solicitudes, incidencias, archivos) se conservan mientras no borres el Codespace.
- **Correos simulados** (recuperación de contraseña, notificaciones): salen en la consola de depuración de cada microservicio, igual que en local.
- **Cuenta de administrador:** `admin` / `admin`, como en local.
- **Horas gratis:** las cuentas personales tienen 120 horas-núcleo al mes (60 horas con 2 núcleos). Detén el Codespace cuando no lo uses: **Code → Codespaces → ⋯ → Stop codespace**.
- **El día de la exposición:** enciéndelo 10 minutos antes, entra una vez como ciudadano para comprobar que todo responde y deja la pestaña abierta.
