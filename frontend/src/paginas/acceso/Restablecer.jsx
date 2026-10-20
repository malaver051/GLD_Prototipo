import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import Campo from '../../componentes/Campo'
import CampoContrasena from '../../componentes/CampoContrasena'
import DisenoAcceso from '../../componentes/DisenoAcceso'
import RequisitosContrasena from '../../componentes/RequisitosContrasena'
import { consultarEnlace, restablecerClave } from '../../servicios/identidadService'
import { contrasenaValida } from '../../utilidades/contrasena'

// Pantalla a la que llega el ciudadano desde el enlace del correo.
export default function Restablecer() {
  const [parametros] = useSearchParams()
  const token = parametros.get('token') || ''
  const navegar = useNavigate()
  const [enlace, setEnlace] = useState(null)
  const [nueva, setNueva] = useState('')
  const [confirmacion, setConfirmacion] = useState('')
  const [errores, setErrores] = useState({})
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    if (!token) {
      setEnlace({ vigente: false })
      return
    }
    consultarEnlace(token)
      .then(setEnlace)
      .catch((e) => setError(e))
  }, [token])

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
      const { mensaje } = await restablecerClave(token, nueva, confirmacion)
      navegar('/ingresar', { replace: true, state: { mensaje } })
    } catch (e) {
      setError(e)
    } finally {
      setEnviando(false)
    }
  }

  if (enlace && !enlace.vigente) {
    return (
      <DisenoAcceso>
        <header className="acceso__encabezado">
          <h1>Este enlace ya no sirve</h1>
          <p>Los enlaces vencen a los 30 minutos y solo se pueden usar una vez. Pide uno nuevo para cambiar tu contraseña.</p>
        </header>
        <Link to="/recuperar" className="boton boton--primario boton--bloque">
          Pedir un enlace nuevo
        </Link>
      </DisenoAcceso>
    )
  }

  return (
    <DisenoAcceso>
      <header className="acceso__encabezado">
        <h1>Crea una nueva contraseña</h1>
        <p>{enlace?.correo ? `Para la cuenta ${enlace.correo}.` : 'Verificando el enlace…'}</p>
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
        <Boton type="submit" bloque cargando={enviando} disabled={!enlace}>
          Guardar contraseña
        </Boton>
      </form>
    </DisenoAcceso>
  )
}
