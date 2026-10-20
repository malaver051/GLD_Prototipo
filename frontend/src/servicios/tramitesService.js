// Operaciones del microservicio de trámites (una función por endpoint).
import { descargarArchivo, solicitarTramites as solicitar } from './api'

// --- Ciudadano
export const consultarCatalogo = (token) => solicitar('/catalogo', { token })

export const consultarTipoTramite = (token, id) => solicitar(`/catalogo/${id}`, { token })

// archivos: { [idRequisito]: File }
export function radicarSolicitud(token, idTipoTramite, observaciones, archivos) {
  const formulario = new FormData()
  formulario.append('idTipoTramite', idTipoTramite)
  if (observaciones?.trim()) formulario.append('observaciones', observaciones.trim())
  Object.entries(archivos).forEach(([idRequisito, archivo]) => formulario.append(`requisito-${idRequisito}`, archivo))
  return solicitar('/solicitudes', { metodo: 'POST', cuerpo: formulario, token })
}

export function enviarCorreccion(token, idSolicitud, comentario, archivos) {
  const formulario = new FormData()
  if (comentario?.trim()) formulario.append('comentario', comentario.trim())
  Object.entries(archivos).forEach(([idRequisito, archivo]) => formulario.append(`requisito-${idRequisito}`, archivo))
  return solicitar(`/solicitudes/${idSolicitud}/correccion`, { metodo: 'POST', cuerpo: formulario, token })
}

export const iniciarPago = (token, idSolicitud) =>
  solicitar(`/solicitudes/${idSolicitud}/pagos`, { metodo: 'POST', token })

export const consultarOrdenPago = (token, referencia) =>
  solicitar(`/pagos/${encodeURIComponent(referencia)}`, { token })

export const registrarResultadoPago = (token, referencia, aprobado, banco) =>
  solicitar(`/pagos/${encodeURIComponent(referencia)}/resultado`, { metodo: 'POST', cuerpo: { aprobado, banco }, token })

// --- Ciudadano y funcionario
export const listarSolicitudes = (token, grupo) =>
  solicitar(`/solicitudes${grupo ? `?grupo=${grupo}` : ''}`, { token })

export const consultarSolicitud = (token, id) => solicitar(`/solicitudes/${id}`, { token })

export const descargarDocumento = (token, idSolicitud, documento) =>
  descargarArchivo(`/solicitudes/${idSolicitud}/documentos/${documento.id}`, token, documento.nombreArchivo)

// --- Funcionario
export const pedirInformacion = (token, id, motivo) =>
  solicitar(`/solicitudes/${id}/solicitud-informacion`, { metodo: 'POST', cuerpo: { motivo }, token })

export const resolverSolicitud = (token, id, aprobada, motivo) =>
  solicitar(`/solicitudes/${id}/resolucion`, { metodo: 'POST', cuerpo: { aprobada, motivo }, token })

export const listarCatalogoAdmin = (token) => solicitar('/admin/tipos-tramite', { token })

export const crearTipoTramite = (token, datos) =>
  solicitar('/admin/tipos-tramite', { metodo: 'POST', cuerpo: datos, token })

export const actualizarTipoTramite = (token, id, datos) =>
  solicitar(`/admin/tipos-tramite/${id}`, { metodo: 'PUT', cuerpo: datos, token })

export const cambiarEstadoTipoTramite = (token, id, habilitado) =>
  solicitar(`/admin/tipos-tramite/${id}/estado`, { metodo: 'PATCH', cuerpo: { habilitado }, token })

// --- Público
export const verificarDocumento = (codigo) => solicitar(`/verificacion/${encodeURIComponent(codigo)}`)
