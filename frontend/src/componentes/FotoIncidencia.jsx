import { useEffect, useState } from 'react'
import { useSesion } from '../contexto/SesionContext'
import { obtenerFotoIncidencia } from '../servicios/comunidadService'

// Muestra la foto de una incidencia (la API exige el token, por eso se trae como blob).
export default function FotoIncidencia({ idIncidencia, descripcion }) {
  const { sesion } = useSesion()
  const [url, setUrl] = useState(null)
  const [fallo, setFallo] = useState(false)

  useEffect(() => {
    let vigente = true
    let creada = null
    obtenerFotoIncidencia(sesion.token, idIncidencia)
      .then((blob) => {
        if (!vigente) return
        creada = URL.createObjectURL(blob)
        setUrl(creada)
      })
      .catch(() => vigente && setFallo(true))
    return () => {
      vigente = false
      if (creada) URL.revokeObjectURL(creada)
    }
  }, [sesion.token, idIncidencia])

  if (fallo) return <p className="texto-suave">No fue posible cargar la foto.</p>
  if (!url) return <div className="foto-incidencia foto-incidencia--cargando">Cargando foto…</div>
  return (
    <a href={url} target="_blank" rel="noreferrer" className="foto-incidencia">
      <img src={url} alt={descripcion} />
    </a>
  )
}
