import { Navigate } from 'react-router-dom'
import { useSesion } from '../contexto/SesionContext'
import { rutaParaSesion } from '../utilidades/rutas'

// Solo permite la pantalla si la sesión está en el paso indicado (p. ej. VERIFICAR_MFA).
export function RutaPaso({ paso, children }) {
  const { sesion } = useSesion()
  if (!sesion) return <Navigate to="/ingresar" replace />
  if (sesion.paso !== paso) return <Navigate to={rutaParaSesion(sesion)} replace />
  return children
}

// Solo permite la pantalla a usuarios con acceso concedido y el rol indicado.
export function RutaRol({ rol, children }) {
  const { sesion } = useSesion()
  if (!sesion) return <Navigate to="/ingresar" replace />
  if (sesion.paso !== 'ACCESO_CONCEDIDO' || sesion.usuario?.rol !== rol) {
    return <Navigate to={rutaParaSesion(sesion)} replace />
  }
  return children
}

// Pantallas públicas: si ya hay sesión completa, envía al inicio del rol.
export function RutaPublica({ children }) {
  const { sesion } = useSesion()
  if (sesion?.paso === 'ACCESO_CONCEDIDO') return <Navigate to={rutaParaSesion(sesion)} replace />
  return children
}
