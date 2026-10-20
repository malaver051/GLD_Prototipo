import { REQUISITOS_CONTRASENA } from '../utilidades/contrasena'
import Icono from './Icono'

export default function RequisitosContrasena({ valor }) {
  return (
    <ul className="requisitos" aria-label="Requisitos de la contraseña">
      {REQUISITOS_CONTRASENA.map((r) => {
        const cumple = r.cumple(valor)
        return (
          <li key={r.clave} data-cumple={cumple}>
            <span className="requisitos__marca">{cumple && <Icono nombre="marca" tamano={11} strokeWidth="3" />}</span>
            {r.texto}
            <span className="visualmente-oculto">{cumple ? ' (cumplido)' : ' (pendiente)'}</span>
          </li>
        )
      })}
    </ul>
  )
}
