// Pastilla con el estado de una incidencia y los datos que comparten las pantallas.
export const ETIQUETA_INCIDENCIA = {
  REPORTADA: 'Reportada',
  ASIGNADA: 'Asignada',
  EN_ATENCION: 'En atención',
  RESUELTA: 'Resuelta',
  CERRADA: 'Cerrada',
}

const CLASE = {
  REPORTADA: 'pastilla--pendiente',
  ASIGNADA: 'pastilla--asignada',
  EN_ATENCION: 'pastilla--revision',
  RESUELTA: 'pastilla--activo',
  CERRADA: 'pastilla--inactivo',
}

// Color del marcador en el mapa (mismos tonos de las pastillas)
export const COLOR_ESTADO = {
  REPORTADA: '#b0822f',
  ASIGNADA: '#4a6e8a',
  EN_ATENCION: '#2d5b5e',
  RESUELTA: '#3c6f52',
  CERRADA: '#8a9599',
}

export const CATEGORIAS = [
  { valor: 'VIAS', texto: 'Vías y baches', ayuda: 'Baches, banquetas, señales' },
  { valor: 'ALUMBRADO', texto: 'Alumbrado público', ayuda: 'Luminarias apagadas o dañadas' },
  { valor: 'ASEO', texto: 'Aseo y basura', ayuda: 'Basura acumulada, recolección' },
  { valor: 'SEGURIDAD', texto: 'Seguridad', ayuda: 'Riesgos, vandalismo' },
  { valor: 'OTRO', texto: 'Otro', ayuda: 'Fugas, árboles, otros' },
]

export default function EstadoIncidencia({ estado }) {
  return <span className={`pastilla ${CLASE[estado] || ''}`}>{ETIQUETA_INCIDENCIA[estado] || estado}</span>
}
