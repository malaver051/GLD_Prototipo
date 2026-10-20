// Cliente HTTP de los microservicios. En desarrollo, Vite reenvía cada base a su puerto (ver vite.config.js).
export const BASE_IDENTIDAD = '/api/v1/identidad'
export const BASE_TRAMITES = '/api/v1/tramites'
export const BASE_COMUNIDAD = '/api/v1/comunidad'

const SERVICIO = {
  [BASE_IDENTIDAD]: 'ms-identidad (puerto 8081)',
  [BASE_TRAMITES]: 'ms-tramites (puerto 8082)',
  [BASE_COMUNIDAD]: 'ms-comunidad (puerto 8083)',
}

export class ErrorApi extends Error {
  constructor(estado, detalle, errores = []) {
    super(detalle)
    this.estado = estado
    this.errores = errores
  }
}

async function enviar(base, ruta, { metodo = 'GET', cuerpo, token } = {}) {
  const encabezados = {}
  const esFormulario = cuerpo instanceof FormData
  if (cuerpo !== undefined && !esFormulario) encabezados['Content-Type'] = 'application/json'
  if (token) encabezados.Authorization = `Bearer ${token}`

  let respuesta
  try {
    respuesta = await fetch(base + ruta, {
      method: metodo,
      headers: encabezados,
      body: cuerpo === undefined ? undefined : esFormulario ? cuerpo : JSON.stringify(cuerpo),
    })
  } catch {
    throw new ErrorApi(0, `No hay conexión con el servidor. Verifica que ${SERVICIO[base]} esté en ejecución.`)
  }

  if (!respuesta.ok) {
    const tipo = respuesta.headers.get('content-type') || ''
    const datos = tipo.includes('json') ? await respuesta.json() : null
    // 502/504 llegan del proxy de Vite cuando el microservicio está apagado
    const detalle = datos?.detail || ([502, 503, 504].includes(respuesta.status)
      ? `No hay conexión con ${SERVICIO[base]}. Verifica que esté en ejecución.`
      : 'No fue posible completar la solicitud.')
    throw new ErrorApi(respuesta.status, detalle, datos?.errores || [])
  }
  return respuesta
}

async function json(base, ruta, opciones) {
  const respuesta = await enviar(base, ruta, opciones)
  const tipo = respuesta.headers.get('content-type') || ''
  return tipo.includes('json') ? respuesta.json() : null
}

// Peticiones JSON (y multipart si el cuerpo es FormData)
export const solicitar = (ruta, opciones) => json(BASE_IDENTIDAD, ruta, opciones)
export const solicitarTramites = (ruta, opciones) => json(BASE_TRAMITES, ruta, opciones)
export const solicitarComunidad = (ruta, opciones) => json(BASE_COMUNIDAD, ruta, opciones)

// Trae un archivo protegido (por ejemplo, una foto) para mostrarlo en pantalla
export async function obtenerArchivo(base, ruta, token) {
  const respuesta = await enviar(base, ruta, { token })
  return respuesta.blob()
}

// Descarga un archivo protegido con el token y lo guarda en el equipo
export async function descargarArchivo(ruta, token, nombre) {
  const respuesta = await enviar(BASE_TRAMITES, ruta, { token })
  const blob = await respuesta.blob()
  const url = URL.createObjectURL(blob)
  const enlace = document.createElement('a')
  enlace.href = url
  enlace.download = nombre
  document.body.appendChild(enlace)
  enlace.click()
  enlace.remove()
  setTimeout(() => URL.revokeObjectURL(url), 10000)
}
