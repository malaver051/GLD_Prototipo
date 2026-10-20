import { createContext, useCallback, useContext, useMemo, useState } from 'react'

// Estado de la sesión: { paso, token, usuario }.
// Se guarda en sessionStorage: se conserva al recargar y se borra al cerrar la pestaña.
const CLAVE = 'gld.sesion'
const SesionContext = createContext(null)

function leerSesion() {
  try {
    const guardada = sessionStorage.getItem(CLAVE)
    return guardada ? JSON.parse(guardada) : null
  } catch {
    return null
  }
}

export function SesionProvider({ children }) {
  const [sesion, setSesion] = useState(leerSesion)
  const [avisoSalida, setAvisoSalida] = useState(null)

  // Aplica la respuesta de cualquier paso de autenticación (RespuestaIngreso del backend)
  const aplicarRespuesta = useCallback((respuesta) => {
    const nueva = { paso: respuesta.paso, token: respuesta.token, usuario: respuesta.usuario }
    try {
      sessionStorage.setItem(CLAVE, JSON.stringify(nueva))
    } catch {
      /* el navegador puede bloquear el almacenamiento; la sesión sigue en memoria */
    }
    setAvisoSalida(null)
    setSesion(nueva)
    return nueva
  }, [])

  const cerrarSesion = useCallback((motivo = null) => {
    try {
      sessionStorage.removeItem(CLAVE)
    } catch {
      /* sin almacenamiento disponible */
    }
    setAvisoSalida(motivo)
    setSesion(null)
  }, [])

  const valor = useMemo(
    () => ({ sesion, aplicarRespuesta, cerrarSesion, avisoSalida }),
    [sesion, aplicarRespuesta, cerrarSesion, avisoSalida],
  )

  return <SesionContext.Provider value={valor}>{children}</SesionContext.Provider>
}

export function useSesion() {
  const contexto = useContext(SesionContext)
  if (!contexto) throw new Error('useSesion debe usarse dentro de <SesionProvider>')
  return contexto
}
