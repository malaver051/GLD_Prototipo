import { useCallback, useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import Campo from '../../componentes/Campo'
import DisenoApp from '../../componentes/DisenoApp'
import EstadoIncidencia, { COLOR_ESTADO, ETIQUETA_INCIDENCIA } from '../../componentes/EstadoIncidencia'
import FotoIncidencia from '../../componentes/FotoIncidencia'
import Icono from '../../componentes/Icono'
import LineaTiempo from '../../componentes/LineaTiempo'
import Mapa from '../../componentes/Mapa'
import { useSesion } from '../../contexto/SesionContext'
import { calificarIncidencia, consultarIncidencia, reabrirIncidencia } from '../../servicios/comunidadService'
import { fechaCorta } from '../../utilidades/formato'
import { NAV_CIUDADANO } from '../../utilidades/navegacion'
import { useErrorSesion } from '../../utilidades/useErrorSesion'

const PUNTAJES = Array.from({ length: 11 }, (_, i) => i)

// HU-12 · Calificar la atención (0 a 10) o, si el problema sigue, reabrir una vez.
function CierreIncidencia({ incidencia, alActualizar, alError }) {
  const { sesion } = useSesion()
  const [modo, setModo] = useState('CALIFICAR')
  const [puntaje, setPuntaje] = useState(null)
  const [comentario, setComentario] = useState('')
  const [motivo, setMotivo] = useState('')
  const [aviso, setAviso] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const puedeReabrir = incidencia.accionesPermitidas.includes('REABRIR')

  async function enviar(evento) {
    evento.preventDefault()
    if (modo === 'CALIFICAR' && puntaje === null) {
      setAviso('Elige una calificación de 0 a 10.')
      return
    }
    if (modo === 'REABRIR' && !motivo.trim()) {
      setAviso('Cuéntanos por qué el problema no quedó resuelto.')
      return
    }
    setEnviando(true)
    setAviso(null)
    try {
      const actualizada =
        modo === 'CALIFICAR'
          ? await calificarIncidencia(sesion.token, incidencia.id, puntaje, comentario.trim())
          : await reabrirIncidencia(sesion.token, incidencia.id, motivo.trim())
      alActualizar(actualizada, modo)
    } catch (e) {
      alError(e)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="panel paso-siguiente paso-siguiente--columna paso-siguiente--exito">
      <div>
        <h2>Tu reporte fue resuelto</h2>
        <p>{incidencia.dependencia?.nombre} registró esta solución:</p>
        <blockquote className="mensaje-funcionario">{incidencia.solucion}</blockquote>
      </div>

      {puedeReabrir && (
        <div className="segmentado" role="group" aria-label="¿Quedó resuelto?">
          <button type="button" aria-pressed={modo === 'CALIFICAR'} onClick={() => setModo('CALIFICAR')}>
            Sí, calificar la atención
          </button>
          <button type="button" aria-pressed={modo === 'REABRIR'} onClick={() => setModo('REABRIR')}>
            No, el problema sigue
          </button>
        </div>
      )}

      <form className="pila" onSubmit={enviar} noValidate>
        {aviso && <Aviso tono="error">{aviso}</Aviso>}
        {modo === 'CALIFICAR' ? (
          <>
            <fieldset className="escala">
              <legend className="campo__etiqueta">¿Qué tan satisfecho quedaste con la atención?</legend>
              <div className="escala__opciones">
                {PUNTAJES.map((n) => (
                  <button
                    key={n}
                    type="button"
                    aria-pressed={puntaje === n}
                    aria-label={`${n} de 10`}
                    onClick={() => {
                      setPuntaje(n)
                      setAviso(null)
                    }}
                  >
                    {n}
                  </button>
                ))}
              </div>
              <div className="escala__extremos" aria-hidden="true">
                <span>Nada satisfecho</span>
                <span>Muy satisfecho</span>
              </div>
            </fieldset>
            <Campo etiqueta="Comentario (opcional)">
              <textarea
                className="control control--area"
                rows={2}
                maxLength={500}
                value={comentario}
                onChange={(e) => setComentario(e.target.value)}
              />
            </Campo>
            <div>
              <Boton type="submit" cargando={enviando}>
                Enviar calificación
              </Boton>
            </div>
          </>
        ) : (
          <>
            <Campo etiqueta="¿Qué sigue mal?" ayuda="La incidencia vuelve a la dependencia. Solo se puede reabrir una vez.">
              <textarea
                className="control control--area"
                rows={3}
                maxLength={500}
                value={motivo}
                onChange={(e) => {
                  setMotivo(e.target.value)
                  setAviso(null)
                }}
              />
            </Campo>
            <div>
              <Boton type="submit" variante="secundario" cargando={enviando}>
                Reabrir incidencia
              </Boton>
            </div>
          </>
        )}
      </form>
    </section>
  )
}

// HU-09 · Detalle y seguimiento de una incidencia del ciudadano.
export default function DetalleIncidencia() {
  const { id } = useParams()
  const ubicacion = useLocation()
  const { sesion } = useSesion()
  const atenderSesion = useErrorSesion()
  const [incidencia, setIncidencia] = useState(null)
  const [error, setError] = useState(null)
  const [confirmacion, setConfirmacion] = useState(null)
  const recienReportada = Boolean(ubicacion.state?.recienReportada)

  const manejarError = useCallback(
    (e) => {
      if (!atenderSesion(e)) setError(e)
    },
    [atenderSesion],
  )

  useEffect(() => {
    consultarIncidencia(sesion.token, id).then(setIncidencia).catch(manejarError)
  }, [sesion.token, id, manejarError])

  if (!incidencia) {
    return (
      <DisenoApp seccion="Portal ciudadano" navegacion={NAV_CIUDADANO}>
        {error ? <Aviso tono="error">{error.message}</Aviso> : <p className="texto-suave">Cargando incidencia…</p>}
      </DisenoApp>
    )
  }

  const i = incidencia

  return (
    <DisenoApp seccion="Portal ciudadano" navegacion={NAV_CIUDADANO}>
      <Link to="/portal/incidencias" className="volver">
        <Icono nombre="atras" tamano={16} /> Mis incidencias
      </Link>

      <header className="encabezado-pagina encabezado-pagina--compacto">
        <div>
          <span className="radicado">{i.numeroSeguimiento}</span>
          <h1>{i.categoriaEtiqueta}</h1>
          <p>
            Reportada el {fechaCorta(i.fechaReporte)} en {i.municipio}, {i.estadoMunicipio}
          </p>
        </div>
        <EstadoIncidencia estado={i.estado} />
      </header>

      {recienReportada && i.estado === 'REPORTADA' && (
        <div className="acceso__mensaje">
          <Aviso tono="exito">
            Recibimos tu reporte. Tu número de seguimiento es <strong>{i.numeroSeguimiento}</strong>; te avisaremos por correo
            cada vez que avance.
          </Aviso>
        </div>
      )}
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
          {i.estado === 'RESUELTA' && (
            <CierreIncidencia
              incidencia={i}
              alActualizar={(actualizada, modo) => {
                setIncidencia(actualizada)
                setError(null)
                setConfirmacion(
                  modo === 'CALIFICAR'
                    ? '¡Gracias! Tu calificación ayuda al municipio a mejorar la atención.'
                    : 'Reabrimos la incidencia y la devolvimos a la dependencia.',
                )
              }}
              alError={manejarError}
            />
          )}

          {i.estado === 'CERRADA' && (
            <section className="panel paso-siguiente paso-siguiente--columna paso-siguiente--neutro">
              <div>
                <h2>Incidencia cerrada</h2>
                <p>
                  Calificaste la atención con <strong>{i.calificacion} de 10</strong>
                  {i.comentarioCalificacion ? `: «${i.comentarioCalificacion}»` : '.'}
                </p>
                <p className="texto-suave">Solución registrada: {i.solucion}</p>
              </div>
            </section>
          )}

          {['ASIGNADA', 'EN_ATENCION'].includes(i.estado) && (
            <Aviso tono="info">
              {i.estado === 'ASIGNADA' ? 'Tu reporte está asignado a ' : 'Tu reporte lo está atendiendo '}
              <strong>{i.dependencia?.nombre}</strong>. Te avisaremos cuando quede resuelto.
            </Aviso>
          )}

          <section className="panel separado">
            <div className="panel__cabecera">
              <h2 className="panel__titulo">Tu reporte</h2>
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
                altura={240}
                etiqueta="Ubicación de la incidencia"
              />
              {i.foto && <FotoIncidencia idIncidencia={i.id} descripcion={`Foto del reporte ${i.numeroSeguimiento}`} />}
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
          <section className="panel">
            <dl className="datos">
              <div>
                <dt>Estado</dt>
                <dd>
                  <span className="estado-punto" style={{ background: COLOR_ESTADO[i.estado] }} aria-hidden="true" />
                  {ETIQUETA_INCIDENCIA[i.estado]}
                </dd>
              </div>
              <div>
                <dt>Dependencia</dt>
                <dd>{i.dependencia?.nombre || <span className="texto-suave">Por asignar</span>}</dd>
              </div>
              <div>
                <dt>Actualizada</dt>
                <dd>{fechaCorta(i.fechaActualizacion)}</dd>
              </div>
              {i.fechaResolucion && (
                <div>
                  <dt>Resuelta</dt>
                  <dd>{fechaCorta(i.fechaResolucion)}</dd>
                </div>
              )}
              <div>
                <dt>Foto</dt>
                <dd>{i.foto ? 'Adjunta' : <span className="texto-suave">Sin foto</span>}</dd>
              </div>
            </dl>
          </section>
        </aside>
      </div>
    </DisenoApp>
  )
}
