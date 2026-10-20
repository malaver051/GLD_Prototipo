// Operaciones del microservicio de identidad (una función por endpoint).
import { solicitar } from './api'

// --- Autenticación
export const ingresar = (identificador, contrasena) =>
  solicitar('/auth/ingreso', { metodo: 'POST', cuerpo: { identificador, contrasena } })

export const registrarCiudadano = (datos) =>
  solicitar('/auth/registro', { metodo: 'POST', cuerpo: datos })

export const obtenerConfiguracionMfa = (token) =>
  solicitar('/auth/mfa/configuracion', { token })

export const activarMfa = (token, codigo) =>
  solicitar('/auth/mfa/activacion', { metodo: 'POST', cuerpo: { codigo }, token })

export const verificarMfa = (token, codigo) =>
  solicitar('/auth/mfa/verificacion', { metodo: 'POST', cuerpo: { codigo }, token })

export const cambiarClaveInicial = (token, nuevaContrasena, confirmacion) =>
  solicitar('/auth/clave-inicial', { metodo: 'POST', cuerpo: { nuevaContrasena, confirmacion }, token })

// --- Recuperación de contraseña
export const solicitarRecuperacion = (correo) =>
  solicitar('/auth/recuperacion', { metodo: 'POST', cuerpo: { correo } })

export const consultarEnlace = (token) =>
  solicitar(`/auth/recuperacion/enlace?token=${encodeURIComponent(token)}`)

export const restablecerClave = (token, nuevaContrasena, confirmacion) =>
  solicitar('/auth/recuperacion/restablecer', { metodo: 'POST', cuerpo: { token, nuevaContrasena, confirmacion } })

// --- Catálogos y perfil
export const listarMunicipios = () => solicitar('/municipios')

export const obtenerPerfil = (token) => solicitar('/usuarios/me', { token })

// --- Administración
export const listarUsuarios = (token, rol, q = '') =>
  solicitar(`/admin/usuarios?rol=${rol}&q=${encodeURIComponent(q)}`, { token })

export const crearFuncionario = (token, datos) =>
  solicitar('/admin/usuarios', { metodo: 'POST', cuerpo: datos, token })

export const cambiarEstadoUsuario = (token, id, activo) =>
  solicitar(`/admin/usuarios/${id}/estado`, { metodo: 'PATCH', cuerpo: { activo }, token })

export const restablecerClaveUsuario = (token, id) =>
  solicitar(`/admin/usuarios/${id}/restablecimiento-clave`, { metodo: 'POST', token })

export const listarRolesAsignables = (token) => solicitar('/admin/roles', { token })

export const listarSolicitudesRestablecimiento = (token) =>
  solicitar('/admin/solicitudes-restablecimiento', { token })

export const atenderSolicitud = (token, id) =>
  solicitar(`/admin/solicitudes-restablecimiento/${id}/atencion`, { metodo: 'POST', token })
