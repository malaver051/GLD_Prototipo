import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import Campo from '../../componentes/Campo'
import DisenoApp from '../../componentes/DisenoApp'
import EstadoIncidencia, { ETIQUETA_INCIDENCIA } from '../../componentes/EstadoIncidencia'
import FotoIncidencia from '../../componentes/FotoIncidencia'
import Icono from '../../componentes/Icono'
import LineaTiempo from '../../componentes/LineaTiempo'
import Mapa from '../../componentes/Mapa'
import { useSesion } from '../../contexto/SesionContext'
import {
  asignarIncidencia,
  cambiarEstadoIncidencia,
  consultarIncidencia,
  listarDependencias,
} from '../../servicios/comunidadService'
import { fechaCorta } from '../../utilidades/formato'
import { NAV_FUNCIONARIO } from '../../utilidades/navegacion'
import { useErrorSesion } from '../../utilidades/useErrorSesion'

// Formulario para asignar o reasignar la dependencia (HU-10).
function FormularioAsignacion({ incidencia, dependencias, enviando, alEnviar }) {
  const actual = incidencia.dependencia?.id
  const sugerida = dependencias.find((d) => d.categoriaSugerida === incidencia.categoria)
  // null = el funcionario aún no elige: se usa la sugerida (aunque las dependencias lleguen después)
  const [elegida, setElegida] = useState(null)
  const idDependencia = elegida ?? (actual || !sugerida ? '' : String(sugerida.id))
  const [comentario, setComentario] = useState('')
  const [error, setError] = useState(null)

  function enviar(evento) {
    evento.preventDefault()
    if (!idDependencia) {
      setError('Elige la dependencia que atenderá la incidencia.')
      return
    }
    alEnviar(Number(idDependencia), comentario.trim())
  }

  return (
    <form className="pila" onSubmit={enviar} noValidate>
      <Campo
        etiqueta={actual ? 'Nueva dependencia' : 'Dependencia responsable'}
        ayuda={!actual && sugerida ? `Sugerida para «${incidencia.categoriaEtiqueta}»: ${sugerida.nombre}.` : undefined}
        error={error}
      >
        <select
          className="control"
          value={idDependencia}
          onChange={(e) => {
            setElegida(e.target.value)
            setError(null)
          }}
        >
          <option value="">Elige una dependencia</option>
          {dependencias
            .filter((d) => d.id !== actual)
            .map((d) => (
              <option key={d.id} value={d.id}>
                {d.nombre}
              </option>
            ))}
        </select>
      </Campo>
      <Campo etiqueta="Comentario (opcional)">
        <textarea
          className="control control--area"
          rows={2}
          maxLength={500}
          value={comentario}
          onChange={(e) => setComentario(e.target.value)}
        />
      </Campo>
      <Boton type="submit" bloque variante={actual ? 'secundario' : 'primario'} cargando={enviando}>
        {actual ? 'Reasignar' : 'Asignar dependencia'}
      </Boton>
    </form>
  )
}

// HU-11 · El funcionario atiende la incidencia, registra la solución y la marca como resuelta.
export default function AtencionIncidencia() {
  const { id } = useParams()
  const { sesion } = useSesion()
  const atenderSesion = useErrorSesion()
  const [incidencia, setIncidencia] = useState(null)
  const [dependencias, setDependencias] = useState([])
  const [solucion, setSolucion] = useState('')
  const [errorSolucion, setErrorSolucion] = useState(null)
  const [reasignando, setReasignando] = useState(false)
  const [enviando, setEnviando] = useState(null)
  const [error, setError] = useState(null)
  const [confirmacion, setConfirmacion] = useState(null)

  const manejarError = useCallback(
    (e) => {
      if (!atenderSesion(e)) setError(e)
    },
    [atenderSesion],
  )

  useEffect(() => {
    consultarIncidencia(sesion.token, id).then(setIncidencia).catch(manejarError)
    listarDependencias(sesion.token).then(setDependencias).catch(manejarError)
  }, [sesion.token, id, manejarError])

  async function ejecutar(accion, operacion, mensaje) {
    setEnviando(accion)
    setError(null)
    try {
      const actualizada = await operacion()
      setIncidencia(actualizada)
      setConfirmacion(mensaje(actualizada))
      setReasignando(false)
      setSolucion('')
    } catch (e) {
      manejarError(e)
    } finally {
      setEnviando(null)
    }
  }

  const asignar = (idDependencia, comentario) =>
    ejecutar(
      'ASIGNAR',
      () => asignarIncidencia(sesion.token, incidencia.id, idDependencia, comentario),
      (a) => `La incidencia quedó a cargo de ${a.dependencia.nombre}. Se notificó al ciudadano.`,
    )

  const iniciar = () =>
    ejecutar(
      'INICIAR',
      () => cambiarEstadoIncidencia(sesion.token, incidencia.id, 'EN_ATENCION'),
      () => 'La incidencia está en atención. Se notificó al ciudadano.',
    )

  function resolver(evento) {
    evento.preventDefault()
    if (!solucion.trim()) {
      setErrorSolucion('Registra la solución: el ciudadano la verá antes de calificar.')
      return
    }
    ejecutar(
      'RESOLVER',
      () => cambiarEstadoIncidencia(sesion.token, incidencia.id, 'RESUELTA', solucion.trim()),
      () => 'Incidencia resuelta. El ciudadano recibió la solución y podrá calificar la atención.',
    )
  }

  if (!incidencia) {
    return (
      <DisenoApp seccion="Consola municipal" navegacion={NAV_FUNCIONARIO}>
        {error ? <Aviso tono="error">{error.message}</Aviso> : <p className="texto-suave">Cargando incidencia…</p>}
      </DisenoApp>
    )
  }

  const i = incidencia
  const puede = (accion) => i.accionesPermitidas.includes(accion)

  return (
    <DisenoApp seccion="Consola municipal" navegacion={NAV_FUNCIONARIO}>
      <Link to="/consola/incidencias" className="volver">
        <Icono nombre="atras" tamano={16} /> Incidencias
      </Link>

      <header className="encabezado-pagina encabezado-pagina--compacto">
        <div>
          <span className="radicado">{i.numeroSeguimiento}</span>
          <h1>{i.categoriaEtiqueta}</h1>
          <p>
            Reportada el {fechaCorta(i.fechaReporte)} por {i.ciudadano.nombre}
            {i.reaperturas > 0 && <span className="vence vence--vencida"> · Reabierta por el ciudadano</span>}
          </p>
        </div>
        <EstadoIncidencia estado={i.estado} />
      </header>

      {confirmacion && (
        <div className="acceso__mensaje">
          <Aviso tono="exito">{confirmacion}</Aviso>
        </div>
      )}
      {error && (
        <div className="acceso__mensaje">
          <Aviso tono="error" errores={error.errores}>
            {error.message}
          </Aviso>
        </div>
      )}

      <div className="detalle">
        <div className="detalle__principal">
          <section className="panel">
            <div className="panel__cabecera">
              <h2 className="panel__titulo">Reporte</h2>
            </div>
            <div className="panel__cuerpo pila">
              <p className="texto-reporte">{i.descripcion}</p>
              {i.referencia && (
                <p className="coordenadas">
                  <Icono nombre="ubicacion" tamano={14} /> {i.referencia}
                </p>
              )}
              <Mapa
                centro={[i.latitud, i.longitud]}
                zoom={16}
                punto={[i.latitud, i.longitud]}
                altura={260}
                etiqueta="Ubicación de la incidencia"
              />
              <p className="coordenadas">
                {i.latitud.toFixed(5)}, {i.longitud.toFixed(5)} ·{' '}
                <a
                  href={`https://www.openstreetmap.org/?mlat=${i.latitud}&mlon=${i.longitud}#map=18/${i.latitud}/${i.longitud}`}
                  target="_blank"
                  rel="noreferrer"
                >
                  Abrir en OpenStreetMap
                </a>
              </p>
              {i.foto ? (
                <FotoIncidencia idIncidencia={i.id} descripcion={`Foto del reporte ${i.numeroSeguimiento}`} />
              ) : (
                <p className="texto-suave">El ciudadano no adjuntó foto.</p>
              )}
            </div>
          </section>

          <section className="panel separado">
            <div className="panel__cabecera">
              <h2 className="panel__titulo">Historial</h2>
            </div>
            <div className="panel__cuerpo">
              <LineaTiempo historial={i.historial} etiquetas={ETIQUETA_INCIDENCIA} />
            </div>
          </section>
        </div>

        <aside className="detalle__lateral">
          <section className="panel decision">
            <div className="panel__cabecera">
              <h2 className="panel__titulo">Atención</h2>
            </div>
            <div className="panel__cuerpo pila">
              {i.estado === 'REPORTADA' && (
                <FormularioAsignacion incidencia={i} dependencias={dependencias} enviando={enviando === 'ASIGNAR'} alEnviar={asignar} />
              )}

              {i.estado === 'ASIGNADA' && (
                <>
                  <p>
                    Asignada a <strong>{i.dependencia.nombre}</strong>. Cuando la cuadrilla empiece a trabajar, marca el inicio de la
                    atención.
                  </p>
                  <Boton bloque onClick={iniciar} cargando={enviando === 'INICIAR'}>
                    Iniciar atención
                  </Boton>
                </>
              )}

              {i.estado === 'EN_ATENCION' && (
                <form className="pila" onSubmit={resolver} noValidate>
                  <p>
                    <strong>{i.dependencia.nombre}</strong> está atendiendo la incidencia.
                  </p>
                  <Campo
                    etiqueta="Solución aplicada"
                    ayuda="Qué se hizo para resolverla. El ciudadano la verá antes de calificar."
                    error={errorSolucion}
                  >
                    <textarea
                      className="control control--area"
                      rows={4}
                      maxLength={1000}
                      value={solucion}
                      onChange={(e) => {
                        setSolucion(e.target.value)
                        setErrorSolucion(null)
                      }}
                    />
                  </Campo>
                  <Boton type="submit" bloque cargando={enviando === 'RESOLVER'}>
                    Marcar como resuelta
                  </Boton>
                </form>
              )}

              {['ASIGNADA', 'EN_ATENCION'].includes(i.estado) && puede('ASIGNAR') && (
                <div className="reasignar">
                  {reasignando ? (
                    <FormularioAsignacion incidencia={i} dependencias={dependencias} enviando={enviando === 'ASIGNAR'} alEnviar={asignar} />
                  ) : (
                    <button type="button" className="boton boton--texto" onClick={() => setReasignando(true)}>
                      ¿No le corresponde? Cambiar de dependencia
                    </button>
                  )}
                </div>
              )}

              {i.estado === 'RESUELTA' && (
                <>
                  <p>Resuelta el {fechaCorta(i.fechaResolucion)}. Esperando la calificación del ciudadano.</p>
                  <blockquote className="mensaje-funcionario">{i.solucion}</blockquote>
                </>
              )}

              {i.estado === 'CERRADA' && (
                <>
                  <p>
                    Cerrada el {fechaCorta(i.fechaCierre)}. El ciudadano calificó la atención con{' '}
                    <strong>{i.calificacion} de 10</strong>.
                  </p>
                  {i.comentarioCalificacion && <blockquote className="mensaje-funcionario">{i.comentarioCalificacion}</blockquote>}
                </>
              )}
            </div>
          </section>

          <section className="panel separado">
            <dl className="datos">
              <div>
                <dt>Ciudadano</dt>
                <dd>{i.ciudadano.nombre}</dd>
              </div>
              <div>
                <dt>Correo</dt>
                <dd>{i.ciudadano.correo}</dd>
              </div>
              <div>
                <dt>Dependencia</dt>
                <dd>{i.dependencia?.nombre || <span className="texto-suave">Por asignar</span>}</dd>
              </div>
              <div>
                <dt>Actualizada</dt>
                <dd>{fechaCorta(i.fechaActualizacion)}</dd>
              </div>
            </dl>
          </section>
        </aside>
      </div>
    </DisenoApp>
  )
}
