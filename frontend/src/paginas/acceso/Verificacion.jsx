import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import CodigoOtp from '../../componentes/CodigoOtp'
import DisenoAcceso from '../../componentes/DisenoAcceso'
import Icono from '../../componentes/Icono'
import { useSesion } from '../../contexto/SesionContext'
import { verificarMfa } from '../../servicios/identidadService'
import { rutaParaSesion } from '../../utilidades/rutas'
import { useErrorSesion } from '../../utilidades/useErrorSesion'

const VENCIDO = 'Pasaron más de 10 minutos desde que escribiste tu contraseña. Ingresa de nuevo.'

export default function Verificacion() {
  const { sesion, aplicarRespuesta, cerrarSesion } = useSesion()
  const navegar = useNavigate()
  const atenderSesion = useErrorSesion()
  const [codigo, setCodigo] = useState('')
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  async function verificar(valor = codigo) {
    if (valor.length !== 6 || enviando) return
    setEnviando(true)
    setError(null)
    try {
      const nueva = aplicarRespuesta(await verificarMfa(sesion.token, valor))
      navegar(rutaParaSesion(nueva), { replace: true })
    } catch (e) {
      if (!atenderSesion(e, VENCIDO)) {
        setError(e)
        setCodigo('')
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <DisenoAcceso>
      <Link
        to="/ingresar"
        className="volver"
        onClick={() => cerrarSesion()}
      >
        <Icono nombre="atras" tamano={16} /> Usar otra cuenta
      </Link>
      <header className="acceso__encabezado">
        <h1>Escribe tu código de verificación</h1>
        <p>
          Abre Microsoft Authenticator en tu celular y escribe el código de 6 dígitos de la cuenta GLD de{' '}
          <strong>{sesion.usuario?.correo}</strong>.
        </p>
      </header>

      <div className="pila">
        {error && <Aviso tono="error">{error.message}</Aviso>}
        <CodigoOtp valor={codigo} alCambiar={setCodigo} alCompletar={verificar} error={Boolean(error)} deshabilitado={enviando} />
        <Boton bloque cargando={enviando} disabled={codigo.length !== 6} onClick={() => verificar()}>
          Verificar y entrar
        </Boton>
        <p className="campo__ayuda">El código cambia cada 30 segundos. Si no coincide, revisa que la hora de tu celular sea automática.</p>
      </div>
    </DisenoAcceso>
  )
}
