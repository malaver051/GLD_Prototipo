import { useCallback, useEffect, useState } from 'react'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import DialogoConfirmacion from '../../componentes/DialogoConfirmacion'
import DisenoApp from '../../componentes/DisenoApp'
import Icono from '../../componentes/Icono'
import Notificacion from '../../componentes/Notificacion'
import { useSesion } from '../../contexto/SesionContext'
import {
  atenderSolicitud,
  cambiarEstadoUsuario,
  listarMunicipios,
  listarRolesAsignables,
  listarSolicitudesRestablecimiento,
  listarUsuarios,
  restablecerClaveUsuario,
} from '../../servicios/identidadService'
import { useErrorSesion } from '../../utilidades/useErrorSesion'
import FormularioFuncionario from './FormularioFuncionario'
import TablaSolicitudes from './TablaSolicitudes'
import TablaUsuarios from './TablaUsuarios'

const AVISO_CORREO = 'El correo es simulado: el mensaje aparece en la consola de ms-identidad.'

export default function ConsolaAdmin() {
  const { sesion } = useSesion()
  const token = sesion.token
  const atenderSesion = useErrorSesion()

  const [pestana, setPestana] = useState('usuarios')
  const [rol, setRol] = useState('FUNCIONARIO')
  const [busqueda, setBusqueda] = useState('')
  const [usuarios, setUsuarios] = useState(null)
  const [solicitudes, setSolicitudes] = useState([])
  const [municipios, setMunicipios] = useState([])
  const [roles, setRoles] = useState([])
  const [error, setError] = useState(null)
  const [formularioAbierto, setFormularioAbierto] = useState(false)
  const [confirmacion, setConfirmacion] = useState(null)
  const [procesando, setProcesando] = useState(false)
  const [notificacion, setNotificacion] = useState(null)

  const manejarError = useCallback(
    (e) => {
      if (!atenderSesion(e)) setError(e)
    },
    [atenderSesion],
  )

  const cargarUsuarios = useCallback(() => {
    listarUsuarios(token, rol, busqueda).then(setUsuarios).catch(manejarError)
  }, [token, rol, busqueda, manejarError])

  const cargarSolicitudes = useCallback(() => {
    listarSolicitudesRestablecimiento(token).then(setSolicitudes).catch(manejarError)
  }, [token, manejarError])

  // Catálogos para el formulario
  useEffect(() => {
    listarMunicipios().then(setMunicipios).catch(manejarError)
    listarRolesAsignables(token).then(setRoles).catch(manejarError)
  }, [token, manejarError])

  // Lista de usuarios: espera un momento mientras se escribe en el buscador
  useEffect(() => {
    const t = setTimeout(cargarUsuarios, busqueda ? 250 : 0)
    return () => clearTimeout(t)
  }, [cargarUsuarios, busqueda])

  useEffect(cargarSolicitudes, [cargarSolicitudes])

  const cerrarNotificacion = useCallback(() => setNotificacion(null), [])

  function alCrear(funcionario) {
    setFormularioAbierto(false)
    setRol('FUNCIONARIO')
    setPestana('usuarios')
    cargarUsuarios()
    setNotificacion(`Cuenta creada para ${funcionario.nombreCompleto}. La contraseña temporal se envió a ${funcionario.correo}. ${AVISO_CORREO}`)
  }

  async function ejecutarConfirmacion() {
    const { tipo, objetivo } = confirmacion
    setProcesando(true)
    setError(null)
    try {
      if (tipo === 'restablecer') {
        const { mensaje } = await restablecerClaveUsuario(token, objetivo.id)
        setNotificacion(`${mensaje} ${AVISO_CORREO}`)
      } else if (tipo === 'atender') {
        const { mensaje } = await atenderSolicitud(token, objetivo.id)
        setNotificacion(`${mensaje} ${AVISO_CORREO}`)
      } else if (tipo === 'estado') {
        const activo = objetivo.estado === 'INACTIVO'
        await cambiarEstadoUsuario(token, objetivo.id, activo)
        setNotificacion(activo ? `${objetivo.nombreCompleto} puede volver a ingresar.` : `${objetivo.nombreCompleto} ya no puede ingresar.`)
      }
      cargarUsuarios()
      cargarSolicitudes()
    } catch (e) {
      manejarError(e)
    } finally {
      setProcesando(false)
      setConfirmacion(null)
    }
  }

  const textosConfirmacion = () => {
    if (!confirmacion) return {}
    const { tipo, objetivo } = confirmacion
    if (tipo === 'restablecer' || tipo === 'atender') {
      return {
        titulo: 'Enviar contraseña temporal',
        texto: `Se generará una contraseña temporal para ${objetivo.nombreCompleto} y se enviará a ${objetivo.correo}. Su contraseña actual dejará de funcionar y deberá cambiarla al ingresar.`,
        boton: 'Enviar contraseña temporal',
      }
    }
    const desactivar = objetivo.estado !== 'INACTIVO'
    return {
      titulo: desactivar ? 'Desactivar cuenta' : 'Activar cuenta',
      texto: desactivar
        ? `${objetivo.nombreCompleto} no podrá ingresar hasta que vuelvas a activar su cuenta.`
        : `${objetivo.nombreCompleto} podrá volver a ingresar con su contraseña actual.`,
      boton: desactivar ? 'Desactivar' : 'Activar',
      peligro: desactivar,
    }
  }
  const textos = textosConfirmacion()

  return (
    <DisenoApp seccion="Administración de la plataforma">
      <div className="encabezado-pagina">
        <div>
          <h1>Cuentas de usuario</h1>
          <p>Crea las cuentas de los funcionarios municipales y atiende sus solicitudes de contraseña.</p>
        </div>
        <Boton onClick={() => setFormularioAbierto(true)}>
          <Icono nombre="mas" tamano={16} />
          Crear funcionario
        </Boton>
      </div>

      <div className="pestanas" role="tablist">
        <button type="button" role="tab" aria-selected={pestana === 'usuarios'} onClick={() => setPestana('usuarios')}>
          Usuarios
        </button>
        <button type="button" role="tab" aria-selected={pestana === 'solicitudes'} onClick={() => setPestana('solicitudes')}>
          Solicitudes de contraseña
          {solicitudes.length > 0 && <span className="contador">{solicitudes.length}</span>}
        </button>
      </div>

      {error && (
        <div className="acceso__mensaje">
          <Aviso tono="error" errores={error.errores}>
            {error.message}
          </Aviso>
        </div>
      )}

      {pestana === 'usuarios' ? (
        <section className="panel">
          <div className="panel__cabecera">
            <div className="filtros">
              <div className="segmentado" role="group" aria-label="Tipo de cuenta">
                <button type="button" aria-pressed={rol === 'FUNCIONARIO'} onClick={() => setRol('FUNCIONARIO')}>
                  Funcionarios
                </button>
                <button type="button" aria-pressed={rol === 'CIUDADANO'} onClick={() => setRol('CIUDADANO')}>
                  Ciudadanos
                </button>
              </div>
            </div>
            <input
              className="control buscador"
              type="search"
              placeholder="Buscar por nombre o correo"
              aria-label="Buscar por nombre o correo"
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
            />
          </div>
          <TablaUsuarios
            usuarios={usuarios}
            rol={rol}
            busqueda={busqueda}
            alRestablecer={(u) => setConfirmacion({ tipo: 'restablecer', objetivo: u })}
            alCambiarEstado={(u) => setConfirmacion({ tipo: 'estado', objetivo: u })}
            alCrear={() => setFormularioAbierto(true)}
          />
        </section>
      ) : (
        <section className="panel">
          <TablaSolicitudes
            solicitudes={solicitudes}
            alAtender={(s) => setConfirmacion({ tipo: 'atender', objetivo: s })}
          />
        </section>
      )}

      {formularioAbierto && (
        <FormularioFuncionario
          token={token}
          municipios={municipios}
          roles={roles}
          alCerrar={() => setFormularioAbierto(false)}
          alCrear={alCrear}
          alErrorSesion={atenderSesion}
        />
      )}

      <DialogoConfirmacion
        abierto={Boolean(confirmacion)}
        titulo={textos.titulo}
        textoConfirmar={textos.boton}
        peligro={textos.peligro}
        cargando={procesando}
        alConfirmar={ejecutarConfirmacion}
        alCancelar={() => !procesando && setConfirmacion(null)}
      >
        {textos.texto}
      </DialogoConfirmacion>

      <Notificacion mensaje={notificacion} alCerrar={cerrarNotificacion} duracion={9000} />
    </DisenoApp>
  )
}
