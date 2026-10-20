import { useState } from 'react'
import Icono from './Icono'

// Input de contraseña con botón para mostrar u ocultar. Recibe id y aria-* desde <Campo>.
export default function CampoContrasena({ valor, alCambiar, autoComplete = 'current-password', ...resto }) {
  const [visible, setVisible] = useState(false)
  return (
    <div className="control-clave">
      <input
        {...resto}
        className="control"
        type={visible ? 'text' : 'password'}
        value={valor}
        onChange={(e) => alCambiar(e.target.value)}
        autoComplete={autoComplete}
        spellCheck="false"
      />
      <button
        type="button"
        className="control-clave__boton"
        onClick={() => setVisible((v) => !v)}
        aria-label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
        aria-pressed={visible}
      >
        <Icono nombre={visible ? 'ojoTachado' : 'ojo'} />
      </button>
    </div>
  )
}
