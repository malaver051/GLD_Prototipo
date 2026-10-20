// Operaciones del microservicio de comunidad: incidencias (una función por endpoint).
import { BASE_COMUNIDAD, obtenerArchivo, solicitarComunidad as solicitar } from './api'

// --- Ciudadano y funcionario
export const consultarMapaMunicipio = (token) => solicitar('/mapa/municipio', { token })

export const listarDependencias = (token) => solicitar('/dependencias', { token })

export function listarIncidencias(token, filtros = {}) {
  const parametros = new URLSearchParams()
  Object.entries(filtros).forEach(([clave, valor]) => {
    if (valor !== undefined && valor !== null && valor !== '') parametros.append(clave, valor)
  })
  const consulta = parametros.toString()
  return solicitar(`/incidencias${consulta ? `?${consulta}` : ''}`, { token })
}

export const consultarIncidencia = (token, id) => solicitar(`/incidencias/${id}`, { token })

export const obtenerFotoIncidencia = (token, id) => obtenerArchivo(BASE_COMUNIDAD, `/incidencias/${id}/foto`, token)

// --- Ciudadano
// datos: { categoria, descripcion, referencia, latitud, longitud, foto (File opcional) }
export function reportarIncidencia(token, datos) {
  const formulario = new FormData()
  formulario.append('categoria', datos.categoria)
  formulario.append('descripcion', datos.descripcion.trim())
  if (datos.referencia?.trim()) formulario.append('referencia', datos.referencia.trim())
  if (datos.latitud != null) formulario.append('latitud', datos.latitud)
  if (datos.longitud != null) formulario.append('longitud', datos.longitud)
  if (datos.foto) formulario.append('foto', datos.foto)
  return solicitar('/incidencias', { metodo: 'POST', cuerpo: formulario, token })
}

export const calificarIncidencia = (token, id, calificacion, comentario) =>
  solicitar(`/incidencias/${id}/calificacion`, { metodo: 'POST', cuerpo: { calificacion, comentario: comentario || null }, token })

export const reabrirIncidencia = (token, id, motivo) =>
  solicitar(`/incidencias/${id}/reapertura`, { metodo: 'POST', cuerpo: { motivo }, token })

// --- Funcionario
export const resumenIncidencias = (token) => solicitar('/incidencias/resumen', { token })

export const asignarIncidencia = (token, id, idDependencia, comentario) =>
  solicitar(`/incidencias/${id}/asignacion`, { metodo: 'PUT', cuerpo: { idDependencia, comentario: comentario || null }, token })

export const cambiarEstadoIncidencia = (token, id, estado, comentario) =>
  solicitar(`/incidencias/${id}/estado`, { metodo: 'PUT', cuerpo: { estado, comentario: comentario || null }, token })
