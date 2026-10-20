import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import Campo from '../../componentes/Campo'
import CampoContrasena from '../../componentes/CampoContrasena'
import CampoTelefono from '../../componentes/CampoTelefono'
import DisenoAcceso from '../../componentes/DisenoAcceso'
import Icono from '../../componentes/Icono'
import RequisitosContrasena from '../../componentes/RequisitosContrasena'
import { useSesion } from '../../contexto/SesionContext'
import { listarMunicipios, registrarCiudadano } from '../../servicios/identidadService'
import { contrasenaValida } from '../../utilidades/contrasena'
import { agruparPorEstado } from '../../utilidades/formato'

const INICIAL = {
  nombreCompleto: '',
  tipoDocumento: 'CURP',
  numeroDocumento: '',
  correo: '',
  telefono: '',
  idMunicipio: '',
  contrasena: '',
  confirmacion: '',
  aceptaAvisoPrivacidad: false,
}

const AYUDA_DOCUMENTO = {
  CURP: '18 caracteres, por ejemplo GODE561231HDFRRN09.',
  INE: 'Clave de elector o número de la credencial.',
  PASAPORTE: 'Entre 6 y 12 letras o números.',
}

export function PasosRegistro({ actual }) {
  const pasos = ['Tus datos', 'Contraseña', 'Autenticador']
  return (
    <ol className="pasos" aria-label="Pasos para crear tu cuenta">
      {pasos.map((p, i) => (
        <li key={p} data-estado={i < actual ? 'hecho' : i === actual ? 'actual' : 'pendiente'} aria-current={i === actual ? 'step' : undefined}>
          {p}
        </li>
      ))}
    </ol>
  )
}

function validarDatos(d) {
  const errores = {}
  if (!d.nombreCompleto.trim()) errores.nombreCompleto = 'Escribe tu nombre completo.'
  if (!d.numeroDocumento.trim()) errores.numeroDocumento = 'Escribe el número de documento.'
  const correo = d.correo.trim().toLowerCase()
  if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(correo)) errores.correo = 'Escribe un correo válido, por ejemplo nombre@correo.com.'
  else if (correo.endsWith('@gobierno.com')) errores.correo = 'Los correos @gobierno.com son exclusivos para funcionarios. Usa tu correo personal.'
  if (!/^\d{10}$/.test(d.telefono)) errores.telefono = 'El teléfono debe tener 10 dígitos.'
  if (!d.idMunicipio) errores.idMunicipio = 'Elige tu municipio.'
  return errores
}

export default function Registro() {
  const { aplicarRespuesta } = useSesion()
  const navegar = useNavigate()
  const [paso, setPaso] = useState(0)
  const [datos, setDatos] = useState(INICIAL)
  const [errores, setErrores] = useState({})
  const [errorServidor, setErrorServidor] = useState(null)
  const [municipios, setMunicipios] = useState([])
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    listarMunicipios()
      .then(setMunicipios)
      .catch((e) => setErrorServidor(e))
  }, [])

  const cambiar = (campo) => (e) => {
    const valor = e.target.type === 'checkbox' ? e.target.checked : e.target.value
    setDatos((d) => ({ ...d, [campo]: valor }))
    setErrores((er) => ({ ...er, [campo]: undefined }))
  }

  function continuar(evento) {
    evento.preventDefault()
    const encontrados = validarDatos(datos)
    setErrores(encontrados)
    if (Object.keys(encontrados).length === 0) {
      setErrorServidor(null)
      setPaso(1)
    }
  }

  async function crearCuenta(evento) {
    evento.preventDefault()
    const encontrados = {}
    if (!contrasenaValida(datos.contrasena)) encontrados.contrasena = 'La contraseña aún no cumple todos los requisitos.'
    if (datos.confirmacion !== datos.contrasena) encontrados.confirmacion = 'Las contraseñas no coinciden.'
    if (!datos.aceptaAvisoPrivacidad) encontrados.aceptaAvisoPrivacidad = 'Debes aceptar el aviso de privacidad para crear la cuenta.'
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) return

    setEnviando(true)
    setErrorServidor(null)
    try {
      const respuesta = await registrarCiudadano({
        ...datos,
        nombreCompleto: datos.nombreCompleto.trim(),
        correo: datos.correo.trim().toLowerCase(),
        idMunicipio: Number(datos.idMunicipio),
      })
      aplicarRespuesta(respuesta)
      navegar('/vincular-autenticador', { replace: true, state: { desdeRegistro: true } })
    } catch (e) {
      setErrorServidor(e)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <DisenoAcceso ancho>
      {paso === 0 ? (
        <Link to="/ingresar" className="volver">
          <Icono nombre="atras" tamano={16} /> Volver al ingreso
        </Link>
      ) : (
        <button type="button" className="volver" onClick={() => setPaso(0)}>
          <Icono nombre="atras" tamano={16} /> Corregir mis datos
        </button>
      )}

      <PasosRegistro actual={paso} />

      <header className="acceso__encabezado">
        <h1>{paso === 0 ? 'Crea tu cuenta ciudadana' : 'Elige tu contraseña'}</h1>
        <p>
          {paso === 0
            ? 'Con tu cuenta podrás solicitar trámites a tu municipio y seguir su estado en línea.'
            : 'Después vincularás Microsoft Authenticator para proteger tu cuenta con un segundo factor.'}
        </p>
      </header>

      {errorServidor && (
        <div className="acceso__mensaje">
          <Aviso tono="error" errores={errorServidor.errores}>
            {errorServidor.message}
          </Aviso>
        </div>
      )}

      {paso === 0 && (
        <form className="pila" onSubmit={continuar} noValidate>
          <Campo etiqueta="Nombre completo" error={errores.nombreCompleto}>
            <input className="control" autoComplete="name" value={datos.nombreCompleto} onChange={cambiar('nombreCompleto')} />
          </Campo>

          <div className="fila-dos">
            <Campo etiqueta="Documento">
              <select className="control" value={datos.tipoDocumento} onChange={cambiar('tipoDocumento')}>
                <option value="CURP">CURP</option>
                <option value="INE">Credencial INE</option>
                <option value="PASAPORTE">Pasaporte</option>
              </select>
            </Campo>
            <Campo etiqueta="Número de documento" ayuda={AYUDA_DOCUMENTO[datos.tipoDocumento]} error={errores.numeroDocumento}>
              <input
                className="control"
                autoCapitalize="characters"
                spellCheck="false"
                value={datos.numeroDocumento}
                onChange={(e) => {
                  setDatos((d) => ({ ...d, numeroDocumento: e.target.value.toUpperCase() }))
                  setErrores((er) => ({ ...er, numeroDocumento: undefined }))
                }}
              />
            </Campo>
          </div>

          <Campo etiqueta="Correo electrónico" ayuda="Lo usarás para ingresar y recuperar tu contraseña." error={errores.correo}>
            <input className="control" type="email" autoComplete="email" autoCapitalize="none" value={datos.correo} onChange={cambiar('correo')} />
          </Campo>

          <div className="fila-dos">
            <Campo etiqueta="Teléfono celular" error={errores.telefono}>
              <CampoTelefono
                valor={datos.telefono}
                alCambiar={(v) => {
                  setDatos((d) => ({ ...d, telefono: v }))
                  setErrores((er) => ({ ...er, telefono: undefined }))
                }}
              />
            </Campo>
            <Campo etiqueta="Municipio donde vives" error={errores.idMunicipio}>
              <select className="control" value={datos.idMunicipio} onChange={cambiar('idMunicipio')}>
                <option value="">Elige tu municipio</option>
                {agruparPorEstado(municipios).map(([estado, lista]) => (
                  <optgroup key={estado} label={estado}>
                    {lista.map((m) => (
                      <option key={m.id} value={m.id}>
                        {m.nombre}
                      </option>
                    ))}
                  </optgroup>
                ))}
              </select>
            </Campo>
          </div>

          <Boton type="submit" bloque>
            Continuar
          </Boton>
        </form>
      )}

      {paso === 1 && (
        <form className="pila" onSubmit={crearCuenta} noValidate>
          <Campo etiqueta="Contraseña" error={errores.contrasena}>
            <CampoContrasena valor={datos.contrasena} alCambiar={(v) => setDatos((d) => ({ ...d, contrasena: v }))} autoComplete="new-password" />
          </Campo>
          <RequisitosContrasena valor={datos.contrasena} />

          <Campo etiqueta="Confirma la contraseña" error={errores.confirmacion}>
            <CampoContrasena valor={datos.confirmacion} alCambiar={(v) => setDatos((d) => ({ ...d, confirmacion: v }))} autoComplete="new-password" />
          </Campo>

          <div className="campo">
            <label className="casilla">
              <input type="checkbox" checked={datos.aceptaAvisoPrivacidad} onChange={cambiar('aceptaAvisoPrivacidad')} />
              <span>
                Acepto el aviso de privacidad y autorizo el tratamiento de mis datos personales para gestionar mis trámites
                municipales.
              </span>
            </label>
            {errores.aceptaAvisoPrivacidad && <p className="campo__error">{errores.aceptaAvisoPrivacidad}</p>}
          </div>

          <Boton type="submit" bloque cargando={enviando}>
            Crear cuenta
          </Boton>
        </form>
      )}

      <p className="acceso__pie">
        ¿Ya tienes cuenta? <Link to="/ingresar">Ingresa</Link>
      </p>
    </DisenoAcceso>
  )
}
