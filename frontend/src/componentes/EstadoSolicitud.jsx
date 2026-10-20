// Pastilla con el estado de una solicitud.
const CLASE = {
  PENDIENTE_PAGO: 'pastilla--pendiente',
  EN_REVISION: 'pastilla--revision',
  REQUIERE_INFORMACION: 'pastilla--pendiente',
  APROBADA: 'pastilla--activo',
  RECHAZADA: 'pastilla--rechazo',
}

export const ETIQUETA_ESTADO = {
  PENDIENTE_PAGO: 'Pendiente de pago',
  EN_REVISION: 'En revisión',
  REQUIERE_INFORMACION: 'Requiere información',
  APROBADA: 'Aprobada',
  RECHAZADA: 'Rechazada',
}

export default function EstadoSolicitud({ estado }) {
  return <span className={`pastilla ${CLASE[estado] || ''}`}>{ETIQUETA_ESTADO[estado] || estado}</span>
}
