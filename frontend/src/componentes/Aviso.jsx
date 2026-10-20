import Icono from './Icono'

const ICONO = { error: 'alerta', exito: 'marca', info: 'info', atencion: 'alerta' }

// Mensaje en línea. Con tono "error" se anuncia de inmediato a los lectores de pantalla.
export default function Aviso({ tono = 'info', errores = [], children }) {
  return (
    <div className={`aviso aviso--${tono}`} role={tono === 'error' ? 'alert' : 'status'}>
      <Icono nombre={ICONO[tono]} className="aviso__icono" />
      <div>
        {children}
        {errores.length > 0 && (
          <ul>
            {errores.map((e) => (
              <li key={e}>{e}</li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
