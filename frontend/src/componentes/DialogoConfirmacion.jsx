import { useEffect, useRef } from 'react'
import Boton from './Boton'

// Ventana de confirmación accesible basada en <dialog>.
export default function DialogoConfirmacion({ abierto, titulo, children, textoConfirmar, peligro = false, cargando = false, alConfirmar, alCancelar }) {
  const dialogo = useRef(null)

  useEffect(() => {
    const d = dialogo.current
    if (!d) return
    if (abierto && !d.open) d.showModal()
    if (!abierto && d.open) d.close()
  }, [abierto])

  return (
    <dialog ref={dialogo} className="dialogo" onCancel={(e) => { e.preventDefault(); alCancelar() }} aria-labelledby="dialogo-titulo">
      <h2 id="dialogo-titulo">{titulo}</h2>
      <div className="dialogo__cuerpo">{children}</div>
      <div className="dialogo__acciones">
        <Boton variante="secundario" onClick={alCancelar} disabled={cargando}>
          Cancelar
        </Boton>
        <Boton variante={peligro ? 'peligro' : 'primario'} onClick={alConfirmar} cargando={cargando}>
          {textoConfirmar}
        </Boton>
      </div>
    </dialog>
  )
}
