const formatoFecha = new Intl.DateTimeFormat('es-MX', {
  day: 'numeric',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})

export function fechaCorta(valor) {
  if (!valor) return 'Nunca'
  return formatoFecha.format(new Date(valor))
}

// +523312345678 → 33 1234 5678
export function telefonoLegible(valor) {
  const digitos = (valor || '').replace(/^\+52/, '')
  if (digitos.length !== 10) return valor
  return `${digitos.slice(0, 2)} ${digitos.slice(2, 6)} ${digitos.slice(6)}`
}

// Agrupa los municipios por entidad federativa para el <select>.
export function agruparPorEstado(municipios) {
  const grupos = new Map()
  for (const m of municipios) {
    if (!grupos.has(m.estado)) grupos.set(m.estado, [])
    grupos.get(m.estado).push(m)
  }
  return [...grupos.entries()]
}

const formatoMoneda = new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' })

export const moneda = (valor) => formatoMoneda.format(Number(valor || 0))

const formatoDia = new Intl.DateTimeFormat('es-MX', { day: 'numeric', month: 'short', year: 'numeric' })

// "2026-10-22" (LocalDate) → 22 oct 2026, sin desfase por zona horaria
export function fechaDia(valor) {
  if (!valor) return '—'
  const [a, m, d] = valor.slice(0, 10).split('-').map(Number)
  return formatoDia.format(new Date(a, m - 1, d))
}

export function tamanoArchivo(bytes) {
  if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}
