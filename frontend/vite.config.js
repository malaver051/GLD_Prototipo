import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// En desarrollo, Vite reenvía cada ruta a su microservicio:
//   /api/v1/identidad → ms-identidad (8081)
//   /api/v1/tramites  → ms-tramites  (8082)
//   /api/v1/comunidad → ms-comunidad (8083)
// Así el navegador no necesita CORS y el frontend usa rutas relativas.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api/v1/identidad': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/v1/tramites': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/v1/comunidad': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
    },
  },
})
