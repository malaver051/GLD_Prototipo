import { useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useSesion } from '../contexto/SesionContext'

// Si el backend responde 401 (token vencido o inválido), cierra la sesión y vuelve al ingreso.
// Devuelve true cuando el error ya fue atendido así.
export function useErrorSesion() {
  const { cerrarSesion } = useSesion()
  const navegar = useNavigate()
  return useCallback(
    (error, mensaje = 'Tu sesión expiró. Vuelve a ingresar.') => {
      if (error?.estado === 401) {
        cerrarSesion(mensaje)
        navegar('/ingresar', { replace: true })
        return true
      }
      return false
    },
    [cerrarSesion, navegar],
  )
}
