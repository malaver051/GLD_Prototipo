import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import Campo from '../../componentes/Campo'
import CampoContrasena from '../../componentes/CampoContrasena'
import DisenoAcceso from '../../componentes/DisenoAcceso'
import RequisitosContrasena from '../../componentes/RequisitosContrasena'
import { useSesion } from '../../contexto/SesionContext'
import { cambiarClaveInicial } from '../../servicios/identidadService'
import { contrasenaValida } from '../../utilidades/contrasena'
import { rutaParaSesion } from '../../utilidades/rutas'
import { useErrorSesion } from '../../utilidades/useErrorSesion'

// Primer ingreso del funcionario: reemplaza la clave temporal que generó el sistema.
export default function CambiarClave() {
  const { sesion, aplicarRespuesta } = useSesion()
  const navegar = useNavigate()
  const atenderSesion = useErrorSesion()
  const [nueva, setNueva] = useState('')
  const [confirmacion, setConfirmacion] = useState('')
  const [errores, setErrores] = useState({})
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  async function guardar(evento) {
    evento.preventDefault()
    const encontrados = {}
    if (!contrasenaValida(nueva)) encontrados.nueva = 'La contraseña aún no cumple todos los requisitos.'
    if (confirmacion !== nueva) encontrados.confirmacion = 'Las contraseñas no coinciden.'
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) return

    setEnviando(true)
    setError(null)
    try {
      const sesionNueva = aplicarRespuesta(await cambiarClaveInicial(sesion.token, nueva, confirmacion))
      navegar(rutaParaSesion(sesionNueva), { replace: true })
    } catch (e) {
      if (!atenderSesion(e, 'Pasaron más de 10 minutos. Ingresa de nuevo con tu contraseña temporal.')) setError(e)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <DisenoAcceso>
      <header className="acceso__encabezado">
        <h1>Crea tu propia contraseña</h1>
        <p>
          Hola, {sesion.usuario?.nombreCompleto}. Ingresaste con una contraseña temporal; cámbiala por una que solo tú conozcas
          para entrar a la consola municipal.
        </p>
      </header>

      <form className="pila" onSubmit={guardar} noValidate>
        {error && (
          <Aviso tono="error" errores={error.errores}>
            {error.message}
          </Aviso>
        )}
        <Campo etiqueta="Nueva contraseña" error={errores.nueva}>
          <CampoContrasena valor={nueva} alCambiar={setNueva} autoComplete="new-password" />
        </Campo>
        <RequisitosContrasena valor={nueva} />
        <Campo etiqueta="Confirma la nueva contraseña" error={errores.confirmacion}>
          <CampoContrasena valor={confirmacion} alCambiar={setConfirmacion} autoComplete="new-password" />
        </Campo>
        <Boton type="submit" bloque cargando={enviando}>
          Guardar y entrar
        </Boton>
      </form>
    </DisenoAcceso>
  )
}
