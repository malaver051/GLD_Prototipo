import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import CodigoOtp from '../../componentes/CodigoOtp'
import CodigoQr from '../../componentes/CodigoQr'
import DisenoAcceso from '../../componentes/DisenoAcceso'
import Icono from '../../componentes/Icono'
import { useSesion } from '../../contexto/SesionContext'
import { activarMfa, obtenerConfiguracionMfa } from '../../servicios/identidadService'
import { rutaParaSesion } from '../../utilidades/rutas'
import { useErrorSesion } from '../../utilidades/useErrorSesion'
import { PasosRegistro } from './Registro'

const VENCIDO = 'El tiempo para vincular el autenticador terminó. Ingresa de nuevo con tu correo y contraseña para continuar.'

export default function VincularAutenticador() {
  const { sesion, aplicarRespuesta } = useSesion()
  const navegar = useNavigate()
  const desdeRegistro = useLocation().state?.desdeRegistro
  const atenderSesion = useErrorSesion()
  const [configuracion, setConfiguracion] = useState(null)
  const [codigo, setCodigo] = useState('')
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const [copiada, setCopiada] = useState(false)

  useEffect(() => {
    obtenerConfiguracionMfa(sesion.token)
      .then(setConfiguracion)
      .catch((e) => {
        if (!atenderSesion(e, VENCIDO)) setError(e)
      })
  }, [sesion.token, atenderSesion])

  async function confirmar(valor = codigo) {
    if (valor.length !== 6 || enviando) return
    setEnviando(true)
    setError(null)
    try {
      const nueva = aplicarRespuesta(await activarMfa(sesion.token, valor))
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

  async function copiarClave() {
    try {
      await navigator.clipboard.writeText(configuracion.claveManual.replace(/\s/g, ''))
      setCopiada(true)
      setTimeout(() => setCopiada(false), 2500)
    } catch {
      /* el navegador no permite copiar; la clave sigue visible */
    }
  }

  return (
    <DisenoAcceso ancho>
      {desdeRegistro && <PasosRegistro actual={2} />}
      <header className="acceso__encabezado">
        <h1>Protege tu cuenta con Microsoft Authenticator</h1>
        <p>Cada vez que ingreses te pediremos un código de 6 dígitos que solo aparece en tu celular.</p>
      </header>

      <div className="vincular">
        <ol className="vincular__instrucciones">
          <li>
            <span>
              Instala <strong>Microsoft Authenticator</strong> desde App Store o Google Play.
            </span>
          </li>
          <li>
            <span>
              En la app, toca <strong>+</strong>, elige <strong>Otra cuenta (Google, Facebook, etc.)</strong> y escanea este código.
            </span>
          </li>
          <li>
            <span>Escribe el código de 6 dígitos que aparece en la app para «{configuracion?.emisor || 'GLD'}».</span>
          </li>
        </ol>

        {configuracion && (
          <div className="qr">
            <CodigoQr texto={configuracion.uriOtpauth} descripcion="Código QR para agregar tu cuenta en Microsoft Authenticator" />
            <div className="qr__clave">
              <span>¿No puedes escanearlo? Agrega la cuenta a mano con esta clave:</span>
              <span className="qr__secreto">{configuracion.claveManual}</span>
              <span>Cuenta: {configuracion.cuenta}</span>
              <div>
                <Boton variante="secundario" pequeno onClick={copiarClave}>
                  <Icono nombre={copiada ? 'marca' : 'copiar'} tamano={15} />
                  {copiada ? 'Clave copiada' : 'Copiar clave'}
                </Boton>
              </div>
            </div>
          </div>
        )}

        {error && (
          <Aviso tono="error" errores={error.errores}>
            {error.message}
          </Aviso>
        )}

        <div className="pila pila--compacta">
          <span className="campo__etiqueta" id="etiqueta-codigo">
            Código de Microsoft Authenticator
          </span>
          <CodigoOtp valor={codigo} alCambiar={setCodigo} alCompletar={confirmar} error={Boolean(error)} deshabilitado={!configuracion || enviando} />
        </div>

        <Boton bloque cargando={enviando} disabled={codigo.length !== 6} onClick={() => confirmar()}>
          Vincular y entrar
        </Boton>
      </div>
    </DisenoAcceso>
  )
}
