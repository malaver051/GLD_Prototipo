import { fechaCorta } from '../utilidades/formato'
import { ETIQUETA_ESTADO } from './EstadoSolicitud'

const ORIGEN = {
  CIUDADANO: 'Ciudadano',
  FUNCIONARIO: 'Funcionario',
  PASARELA: 'Pasarela de pagos',
  SISTEMA: 'Sistema',
}

// Historial (de una solicitud o de una incidencia), del más reciente al más antiguo.
// «etiquetas» traduce cada estado; por defecto, los de las solicitudes de trámite.
export default function LineaTiempo({ historial, etiquetas = ETIQUETA_ESTADO }) {
  const eventos = [...historial].reverse()
  return (
    <ol className="linea-tiempo">
      {eventos.map((h, i) => (
        <li key={i} data-estado={h.estadoNuevo} data-reciente={i === 0}>
          <div className="linea-tiempo__cabecera">
            <strong>
              {h.estadoAnterior !== h.estadoNuevo
                ? etiquetas[h.estadoNuevo]
                : h.origen === 'PASARELA'
                  ? 'Pago no confirmado'
                  : h.estadoNuevo === 'ASIGNADA'
                    ? 'Reasignada'
                    : 'Actualización'}
            </strong>
            <span>{fechaCorta(h.fecha)}</span>
          </div>
          <p>{h.motivo}</p>
          <span className="linea-tiempo__autor">{h.usuario || ORIGEN[h.origen]}</span>
        </li>
      ))}
    </ol>
  )
}
