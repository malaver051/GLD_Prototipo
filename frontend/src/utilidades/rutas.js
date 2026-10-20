// Pantalla a la que lleva cada paso de la autenticación.
const INICIO_POR_ROL = {
  ADMIN: '/admin',
  FUNCIONARIO: '/consola',
  CIUDADANO: '/portal',
}

const RUTA_POR_PASO = {
  VERIFICAR_MFA: '/verificacion',
  CONFIGURAR_MFA: '/vincular-autenticador',
  CAMBIAR_CLAVE: '/cambiar-clave',
}

export function rutaParaSesion(sesion) {
  if (!sesion) return '/ingresar'
  if (sesion.paso === 'ACCESO_CONCEDIDO') return INICIO_POR_ROL[sesion.usuario?.rol] || '/ingresar'
  return RUTA_POR_PASO[sesion.paso] || '/ingresar'
}

export const NOMBRE_ROL = {
  ADMIN: 'Administrador de plataforma',
  FUNCIONARIO: 'Funcionario municipal',
  CIUDADANO: 'Ciudadano',
}

export const NOMBRE_DOCUMENTO = {
  CURP: 'CURP',
  INE: 'Credencial INE',
  PASAPORTE: 'Pasaporte',
  NO_APLICA: 'No aplica',
}
