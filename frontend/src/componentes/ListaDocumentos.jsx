import { useState } from 'react'
import { useSesion } from '../contexto/SesionContext'
import { descargarDocumento } from '../servicios/tramitesService'
import { fechaCorta, tamanoArchivo } from '../utilidades/formato'
import Icono from './Icono'

// Adjuntos de una solicitud con descarga protegida por el token.
export default function ListaDocumentos({ idSolicitud, documentos, alError }) {
  const { sesion } = useSesion()
  const [descargando, setDescargando] = useState(null)

  async function descargar(documento) {
    setDescargando(documento.id)
    try {
      await descargarDocumento(sesion.token, idSolicitud, documento)
    } catch (e) {
      alError?.(e)
    } finally {
      setDescargando(null)
    }
  }

  if (documentos.length === 0) return <p className="texto-suave">Sin documentos adjuntos.</p>
  return (
    <ul className="documentos">
      {documentos.map((d) => (
        <li key={d.id}>
          <div>
            <strong>{d.requisito || d.nombreArchivo}</strong>
            <span>
              {d.nombreArchivo} · {tamanoArchivo(d.tamanoBytes)} · {fechaCorta(d.fechaCarga)}
            </span>
          </div>
          <button type="button" className="boton boton--texto" onClick={() => descargar(d)} disabled={descargando === d.id}>
            {descargando === d.id ? 'Descargando…' : 'Descargar'}
          </button>
        </li>
      ))}
    </ul>
  )
}
