import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import Campo from '../../componentes/Campo'
import CampoContrasena from '../../componentes/CampoContrasena'
import DisenoAcceso from '../../componentes/DisenoAcceso'
import { useSesion } from '../../contexto/SesionContext'
import { ingresar } from '../../servicios/identidadService'
import { rutaParaSesion } from '../../utilidades/rutas'

export default function Ingresar() {
  const { aplicarRespuesta, avisoSalida } = useSesion()
  const navegar = useNavigate()
  const ubicacion = useLocation()
  const [identificador, setIdentificador] = useState('')
  const [contrasena, setContrasena] = useState('')
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const mensajePrevio = ubicacion.state?.mensaje || avisoSalida

  async function enviar(evento) {
    evento.preventDefault()
    setError(null)
    if (!identificador.trim() || !contrasena) {
      setError({ message: 'Escribe tu correo y tu contraseña.' })
      return
    }
    setEnviando(true)
    try {
      const sesion = aplicarRespuesta(await ingresar(identificador.trim(), contrasena))
      navegar(rutaParaSesion(sesion), { replace: true })
    } catch (e) {
      setError(e)
      setContrasena('')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <DisenoAcceso>
      <header className="acceso__encabezado">
        <h1>Ingresa a tu cuenta</h1>
        <p>Si eres funcionario, usa tu correo institucional @gobierno.com.</p>
      </header>

      <form className="pila" onSubmit={enviar} noValidate>
        {mensajePrevio && !error && <Aviso tono="info">{mensajePrevio}</Aviso>}
        {error && <Aviso tono="error">{error.message}</Aviso>}

        <Campo etiqueta="Correo electrónico">
          <input
            className="control"
            type="text"
            inputMode="email"
            autoComplete="username"
            autoCapitalize="none"
            spellCheck="false"
            value={identificador}
            onChange={(e) => setIdentificador(e.target.value)}
            placeholder="nombre@correo.com"
          />
        </Campo>

        <Campo etiqueta="Contraseña">
          <CampoContrasena valor={contrasena} alCambiar={setContrasena} />
        </Campo>

        <div className="fila-enlace">
          <span />
          <Link to="/recuperar">¿Olvidaste tu contraseña?</Link>
        </div>

        <Boton type="submit" bloque cargando={enviando}>
          Ingresar
        </Boton>
      </form>

      <p className="acceso__pie">
        ¿Es tu primera vez? <Link to="/registro">Crea tu cuenta ciudadana</Link>
      </p>
    </DisenoAcceso>
  )
}
