import { useRef } from 'react'
import { tamanoArchivo } from '../utilidades/formato'
import Icono from './Icono'

export const TIPOS_ACEPTADOS = ['application/pdf', 'image/jpeg', 'image/png']
export const TAMANO_MAXIMO = 5 * 1024 * 1024

// Devuelve un mensaje si el archivo no se puede adjuntar.
export function validarArchivo(archivo) {
  if (!TIPOS_ACEPTADOS.includes(archivo.type)) return 'Adjunta un PDF, JPG o PNG.'
  if (archivo.size > TAMANO_MAXIMO) return 'El archivo supera los 5 MB.'
  return null
}

// Fila de un requisito con su archivo adjunto.
export default function SelectorArchivo({ requisito, archivo, error, alElegir, alQuitar }) {
  const entrada = useRef(null)
  const id = `requisito-${requisito.id}`
  return (
    <div className="requisito" data-error={Boolean(error)} data-listo={Boolean(archivo)}>
      <div className="requisito__texto">
        <label htmlFor={id} className="requisito__nombre">
          {requisito.nombre}
          {!requisito.obligatorio && <span className="requisito__opcional"> (opcional)</span>}
        </label>
        {requisito.descripcion && <p className="requisito__descripcion">{requisito.descripcion}</p>}
        {archivo && (
          <p className="requisito__archivo">
            <Icono nombre="marca" tamano={14} /> {archivo.name} · {tamanoArchivo(archivo.size)}
          </p>
        )}
        {error && <p className="campo__error">{error}</p>}
      </div>
      <div className="requisito__acciones">
        <input
          ref={entrada}
          id={id}
          type="file"
          accept=".pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png"
          className="visualmente-oculto"
          onChange={(e) => {
            const elegido = e.target.files?.[0]
            if (elegido) alElegir(elegido)
            e.target.value = ''
          }}
        />
        <button type="button" className="boton boton--secundario boton--pequeno" onClick={() => entrada.current?.click()}>
          {archivo ? 'Cambiar' : 'Adjuntar'}
        </button>
        {archivo && (
          <button type="button" className="boton boton--texto" onClick={alQuitar}>
            Quitar
          </button>
        )}
      </div>
    </div>
  )
}
