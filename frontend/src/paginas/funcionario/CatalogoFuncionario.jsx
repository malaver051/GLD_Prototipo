import { useCallback, useEffect, useState } from 'react'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import DialogoConfirmacion from '../../componentes/DialogoConfirmacion'
import DisenoApp from '../../componentes/DisenoApp'
import Icono from '../../componentes/Icono'
import Notificacion from '../../componentes/Notificacion'
import { useSesion } from '../../contexto/SesionContext'
import { cambiarEstadoTipoTramite, listarCatalogoAdmin } from '../../servicios/tramitesService'
import { fechaCorta, moneda } from '../../utilidades/formato'
import { NAV_FUNCIONARIO } from '../../utilidades/navegacion'
import { useErrorSesion } from '../../utilidades/useErrorSesion'
import FormularioTipoTramite from './FormularioTipoTramite'

// El funcionario administra los trámites que su municipio ofrece en línea.
export default function CatalogoFuncionario() {
  const { sesion } = useSesion()
  const atenderSesion = useErrorSesion()
  const [tramites, setTramites] = useState(null)
  const [edicion, setEdicion] = useState(null) // null cerrado, {} nuevo, objeto para editar
  const [cambioEstado, setCambioEstado] = useState(null)
  const [procesando, setProcesando] = useState(false)
  const [error, setError] = useState(null)
  const [notificacion, setNotificacion] = useState(null)

  const manejarError = useCallback((e) => {
    if (!atenderSesion(e)) setError(e)
  }, [atenderSesion])

  const cargar = useCallback(() => {
    listarCatalogoAdmin(sesion.token).then(setTramites).catch(manejarError)
  }, [sesion.token, manejarError])

  useEffect(cargar, [cargar])

  const cerrarNotificacion = useCallback(() => setNotificacion(null), [])

  async function confirmarCambioEstado() {
    setProcesando(true)
    try {
      const habilitar = cambioEstado.estado !== 'HABILITADO'
      await cambiarEstadoTipoTramite(sesion.token, cambioEstado.id, habilitar)
      setNotificacion(habilitar ? `«${cambioEstado.nombre}» ya aparece en el portal ciudadano.` : `«${cambioEstado.nombre}» ya no aparece en el portal ciudadano.`)
      cargar()
    } catch (e) {
      manejarError(e)
    } finally {
      setProcesando(false)
      setCambioEstado(null)
    }
  }

  return (
    <DisenoApp seccion="Consola municipal" navegacion={NAV_FUNCIONARIO}>
      <div className="encabezado-pagina">
        <div>
          <h1>Catálogo de trámites</h1>
          <p>Los trámites habilitados aparecen en el portal ciudadano de {sesion.usuario?.municipio}. Los cambios de costo no afectan solicitudes ya radicadas.</p>
        </div>
        <Boton onClick={() => setEdicion({})}>
          <Icono nombre="mas" tamano={16} />
          Agregar trámite
        </Boton>
      </div>

      {error && (
        <div className="acceso__mensaje">
          <Aviso tono="error" errores={error.errores}>
            {error.message}
          </Aviso>
        </div>
      )}

      <section className="panel">
        {tramites === null ? (
          <div className="vacio">
            <span>Cargando catálogo…</span>
          </div>
        ) : (
          <div className="tabla-contenedor">
            <table className="tabla">
              <thead>
                <tr>
                  <th scope="col">Trámite</th>
                  <th scope="col">Costo</th>
                  <th scope="col">Plazo</th>
                  <th scope="col">Requisitos</th>
                  <th scope="col">Estado</th>
                  <th scope="col">
                    <span className="visualmente-oculto">Acciones</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {tramites.map((t) => (
                  <tr key={t.id}>
                    <td>
                      <div className="tabla__principal">
                        <strong>{t.nombre}</strong>
                        <span className="tabla__secundario">
                          {t.dependencia} · actualizado {fechaCorta(t.fechaActualizacion)}
                        </span>
                      </div>
                    </td>
                    <td>{t.requierePago ? moneda(t.costo) : 'Gratuito'}</td>
                    <td>{t.plazoDias} días hábiles</td>
                    <td>
                      {t.requisitos.length}
                      <span className="tabla__secundario"> ({t.requisitos.filter((r) => r.obligatorio).length} obligatorios)</span>
                    </td>
                    <td>
                      <span className={`pastilla ${t.estado === 'HABILITADO' ? 'pastilla--activo' : 'pastilla--inactivo'}`}>
                        {t.estado === 'HABILITADO' ? 'Habilitado' : 'Deshabilitado'}
                      </span>
                    </td>
                    <td>
                      <div className="tabla__acciones">
                        <Boton variante="texto" onClick={() => setEdicion(t)}>
                          Editar
                        </Boton>
                        <Boton variante="texto" onClick={() => setCambioEstado(t)}>
                          {t.estado === 'HABILITADO' ? 'Deshabilitar' : 'Habilitar'}
                        </Boton>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {edicion && (
        <FormularioTipoTramite
          tramite={edicion.id ? edicion : null}
          alCerrar={() => setEdicion(null)}
          alGuardar={(guardado, nuevo) => {
            setEdicion(null)
            setNotificacion(nuevo ? `«${guardado.nombre}» se agregó al catálogo.` : `Se guardaron los cambios de «${guardado.nombre}».`)
            cargar()
          }}
          alErrorSesion={atenderSesion}
        />
      )}

      <DialogoConfirmacion
        abierto={Boolean(cambioEstado)}
        titulo={cambioEstado?.estado === 'HABILITADO' ? 'Deshabilitar trámite' : 'Habilitar trámite'}
        textoConfirmar={cambioEstado?.estado === 'HABILITADO' ? 'Deshabilitar' : 'Habilitar'}
        peligro={cambioEstado?.estado === 'HABILITADO'}
        cargando={procesando}
        alConfirmar={confirmarCambioEstado}
        alCancelar={() => !procesando && setCambioEstado(null)}
      >
        {cambioEstado?.estado === 'HABILITADO'
          ? `Los ciudadanos dejarán de ver «${cambioEstado?.nombre}». Las solicitudes ya radicadas siguen su curso.`
          : `«${cambioEstado?.nombre}» volverá a aparecer en el portal ciudadano.`}
      </DialogoConfirmacion>

      <Notificacion mensaje={notificacion} alCerrar={cerrarNotificacion} />
    </DisenoApp>
  )
}
