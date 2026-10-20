import { useState } from 'react'
import { Link } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import Campo from '../../componentes/Campo'
import DisenoAcceso from '../../componentes/DisenoAcceso'
import Icono from '../../componentes/Icono'
import { solicitarRecuperacion } from '../../servicios/identidadService'

export default function Recuperar() {
  const [correo, setCorreo] = useState('')
  const [errorCampo, setErrorCampo] = useState(null)
  const [error, setError] = useState(null)
  const [resultado, setResultado] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const esFuncionario = correo.trim().toLowerCase().endsWith('@gobierno.com')

  async function enviar(evento) {
    evento.preventDefault()
    setError(null)
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(correo.trim())) {
      setErrorCampo('Escribe el correo con el que ingresas.')
      return
    }
    setErrorCampo(null)
    setEnviando(true)
    try {
      setResultado(await solicitarRecuperacion(correo.trim().toLowerCase()))
    } catch (e) {
      setError(e)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <DisenoAcceso>
      <Link to="/ingresar" className="volver">
        <Icono nombre="atras" tamano={16} /> Volver al ingreso
      </Link>
      <header className="acceso__encabezado">
        <h1>Recupera el acceso a tu cuenta</h1>
        <p>
          {esFuncionario
            ? 'Como funcionario, el administrador de la plataforma te enviará una contraseña temporal.'
            : 'Te enviaremos un enlace a tu correo para que crees una nueva contraseña.'}
        </p>
      </header>

      {resultado ? (
        <div className="pila">
          <Aviso tono={resultado.canal === 'ADMINISTRADOR' ? 'info' : 'exito'}>{resultado.mensaje}</Aviso>
          <Link to="/ingresar" className="boton boton--secundario boton--bloque">
            Volver al ingreso
          </Link>
        </div>
      ) : (
        <form className="pila" onSubmit={enviar} noValidate>
          {error && <Aviso tono="error">{error.message}</Aviso>}
          <Campo etiqueta="Correo electrónico" error={errorCampo}>
            <input
              className="control"
              type="email"
              autoComplete="email"
              autoCapitalize="none"
              value={correo}
              onChange={(e) => setCorreo(e.target.value)}
            />
          </Campo>
          <Boton type="submit" bloque cargando={enviando}>
            {esFuncionario ? 'Pedir contraseña temporal' : 'Enviar enlace'}
          </Boton>
        </form>
      )}
    </DisenoAcceso>
  )
}
