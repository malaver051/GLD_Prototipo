import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// En desarrollo, Vite reenvía cada ruta a su microservicio:
//   /api/v1/identidad → ms-identidad (8081)
//   /api/v1/tramites  → ms-tramites  (8082)
//   /api/v1/comunidad → ms-comunidad (8083)
// Así el navegador no necesita CORS y el frontend usa rutas relativas.

// El navegador ve un solo origen (Vite), así que el encabezado Origin no se reenvía:
// los microservicios no tratan la petición como CORS, en local ni en Codespaces.
const haciaMicroservicio = (target) => ({
  target,
  changeOrigin: true,
  configure: (proxy) => {
    proxy.on('proxyReq', (peticion) => peticion.removeHeader('origin'))
  },
})

// En GitHub Codespaces la app se abre en https://<codespace>-5173.app.github.dev
const enCodespaces = Boolean(process.env.CODESPACES)

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    host: enCodespaces ? '0.0.0.0' : 'localhost',
    allowedHosts: ['.app.github.dev'],
    proxy: {
      '/api/v1/identidad': haciaMicroservicio('http://localhost:8081'),
      '/api/v1/tramites': haciaMicroservicio('http://localhost:8082'),
      '/api/v1/comunidad': haciaMicroservicio('http://localhost:8083'),
    },
  },
})
