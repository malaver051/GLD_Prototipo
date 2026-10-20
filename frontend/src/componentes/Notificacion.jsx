import { useEffect } from 'react'
import Icono from './Icono'

export default function Notificacion({ mensaje, alCerrar, duracion = 7000 }) {
  useEffect(() => {
    if (!mensaje) return undefined
    const t = setTimeout(alCerrar, duracion)
    return () => clearTimeout(t)
  }, [mensaje, alCerrar, duracion])

  if (!mensaje) return null
  return (
    <div className="notificacion" role="status">
      <span>{mensaje}</span>
      <button type="button" onClick={alCerrar} aria-label="Cerrar aviso">
        <Icono nombre="cerrar" tamano={16} />
      </button>
    </div>
  )
}
