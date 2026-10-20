import { useCallback, useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import Campo from '../../componentes/Campo'
import DisenoApp from '../../componentes/DisenoApp'
import EstadoSolicitud from '../../componentes/EstadoSolicitud'
import Icono from '../../componentes/Icono'
import LineaTiempo from '../../componentes/LineaTiempo'
import ListaDocumentos from '../../componentes/ListaDocumentos'
import SelectorArchivo, { validarArchivo } from '../../componentes/SelectorArchivo'
import { useSesion } from '../../contexto/SesionContext'
import { consultarSolicitud, descargarDocumento, enviarCorreccion, iniciarPago } from '../../servicios/tramitesService'
import { fechaCorta, fechaDia, moneda } from '../../utilidades/formato'
import { NAV_CIUDADANO } from '../../utilidades/navegacion'
import { useErrorSesion } from '../../utilidades/useErrorSesion'

const ESTADO_PAGO = { PENDIENTE: 'Pendiente', APROBADO: 'Aprobado', RECHAZADO: 'No confirmado', CANCELADO: 'Cancelado' }

export default function DetalleSolicitud() {
  const { id } = useParams()
  const { sesion } = useSesion()
  const navegar = useNavigate()
  const recienRadicada = useLocation().state?.recienRadicada
  const atenderSesion = useErrorSesion()
  const [solicitud, setSolicitud] = useState(null)
  const [error, setError] = useState(null)
  const [pagando, setPagando] = useState(false)

  const manejarError = useCallback((e) => {
    if (!atenderSesion(e)) setError(e)
  }, [atenderSesion])

  useEffect(() => {
    consultarSolicitud(sesion.token, id).then(setSolicitud).catch(manejarError)
  }, [sesion.token, id, manejarError])

  async function pagar() {
    setPagando(true)
    setError(null)
    try {
      const orden = await iniciarPago(sesion.token, solicitud.id)
      navegar(`/pasarela?referencia=${encodeURIComponent(orden.referencia)}`)
    } catch (e) {
      manejarError(e)
      setPagando(false)
    }
  }

  if (!solicitud) {
    return (
      <DisenoApp seccion="Portal ciudadano" navegacion={NAV_CIUDADANO}>
        {error ? <Aviso tono="error">{error.message}</Aviso> : <p className="texto-suave">Cargando solicitud…</p>}
      </DisenoApp>
    )
  }

  const s = solicitud
  return (
    <DisenoApp seccion="Portal ciudadano" navegacion={NAV_CIUDADANO}>
      <Link to="/portal" className="volver">
        <Icono nombre="atras" tamano={16} /> Mis solicitudes
      </Link>

      <header className="encabezado-pagina encabezado-pagina--compacto">
        <div>
          <span className="radicado">{s.numeroRadicado}</span>
          <h1>{s.tramite.nombre}</h1>
          <p>
            Radicada el {fechaCorta(s.fechaRadicacion)} en {s.ciudadano.municipio}, {s.ciudadano.estado}
          </p>
        </div>
        <EstadoSolicitud estado={s.estado} />
      </header>

      {error && (
        <div className="acceso__mensaje">
          <Aviso tono="error" errores={error.errores}>
            {error.message}
          </Aviso>
        </div>
      )}

      <div className="detalle">
        <div className="detalle__principal">
          {/* Lo que le toca hacer al ciudadano según el estado */}
          {s.estado === 'PENDIENTE_PAGO' && (
            <section className="panel paso-siguiente">
              <div>
                <h2>{recienRadicada ? 'Solicitud radicada. Falta el pago' : 'Falta el pago'}</h2>
                <p>
                  Paga {moneda(s.costo)} en línea para que el municipio revise tu solicitud. La respuesta llegará en{' '}
                  {s.tramite.plazoDias} días hábiles a partir de la confirmación.
                </p>
              </div>
              <Boton onClick={pagar} cargando={pagando}>
                <Icono nombre="candado" tamano={16} />
                Pagar {moneda(s.costo)}
              </Boton>
            </section>
          )}

          {s.estado === 'EN_REVISION' && (
            <Aviso tono="info">
              {recienRadicada ? 'Solicitud radicada. ' : ''}El municipio está revisando tu solicitud. Tendrás respuesta a más tardar el{' '}
              <strong>{fechaDia(s.fechaLimite)}</strong>.
            </Aviso>
          )}

          {s.estado === 'REQUIERE_INFORMACION' && (
            <FormularioCorreccion solicitud={s} alActualizar={setSolicitud} alError={manejarError} />
          )}

          {s.estado === 'APROBADA' && s.documentoOficial && (
            <section className="panel paso-siguiente paso-siguiente--exito">
              <div>
                <h2>Tu trámite fue aprobado</h2>
                <p>
                  Descarga el documento oficial. Cualquier persona puede verificarlo con el código{' '}
                  <strong>{s.documentoOficial.codigoVerificacion}</strong>.
                </p>
                {s.motivoResolucion && <p className="texto-suave">Observaciones: {s.motivoResolucion}</p>}
              </div>
              <Boton onClick={() => descargarDocumento(sesion.token, s.id, s.documentoOficial).catch(manejarError)}>
                <Icono nombre="descargar" tamano={16} />
                Descargar documento oficial
              </Boton>
            </section>
          )}

          {s.estado === 'RECHAZADA' && (
            <Aviso tono="error">
              <strong>Tu solicitud fue rechazada.</strong> Motivo: {s.motivoResolucion}
            </Aviso>
          )}

          <section className="panel separado">
            <div className="panel__cabecera">
              <h2 className="panel__titulo">Documentos que adjuntaste</h2>
            </div>
            <div className="panel__cuerpo">
              <ListaDocumentos idSolicitud={s.id} documentos={s.adjuntos} alError={manejarError} />
            </div>
          </section>

          <section className="panel separado">
            <div className="panel__cabecera">
              <h2 className="panel__titulo">Historial</h2>
            </div>
            <div className="panel__cuerpo">
              <LineaTiempo historial={s.historial} />
            </div>
          </section>
        </div>

        <aside className="detalle__lateral">
          <section className="panel">
            <dl className="datos">
              <div>
                <dt>Costo</dt>
                <dd>{Number(s.costo) > 0 ? moneda(s.costo) : 'Gratuito'}</dd>
              </div>
              <div>
                <dt>Atiende</dt>
                <dd>{s.tramite.dependencia}</dd>
              </div>
              <div>
                <dt>Respuesta</dt>
                <dd>{s.fechaLimite ? `A más tardar el ${fechaDia(s.fechaLimite)}` : `${s.tramite.plazoDias} días hábiles tras el pago`}</dd>
              </div>
              {s.observaciones && (
                <div>
                  <dt>Tu mensaje</dt>
                  <dd>{s.observaciones}</dd>
                </div>
              )}
            </dl>
          </section>

          {s.pagos.length > 0 && (
            <section className="panel separado">
              <div className="panel__cabecera">
                <h2 className="panel__titulo">Pagos</h2>
              </div>
              <ul className="pagos">
                {[...s.pagos].reverse().map((p) => (
                  <li key={p.referencia}>
                    <div>
                      <strong>{p.referencia}</strong>
                      <span>
                        {moneda(p.valor)}
                        {p.banco ? ` · ${p.banco}` : ''} · {fechaCorta(p.fechaConfirmacion || p.fechaCreacion)}
                      </span>
                    </div>
                    <span className={`pastilla pastilla--pago-${p.estado.toLowerCase()}`}>{ESTADO_PAGO[p.estado]}</span>
                  </li>
                ))}
              </ul>
            </section>
          )}
        </aside>
      </div>
    </DisenoApp>
  )
}

// BPMN: «Información adicional recibida». El ciudadano responde y la solicitud vuelve a revisión.
function FormularioCorreccion({ solicitud, alActualizar, alError }) {
  const { sesion } = useSesion()
  const [comentario, setComentario] = useState('')
  const [archivos, setArchivos] = useState({})
  const [errores, setErrores] = useState({})
  const [aviso, setAviso] = useState(null)
  const [enviando, setEnviando] = useState(false)

  async function enviar(evento) {
    evento.preventDefault()
    if (!comentario.trim() && Object.keys(archivos).length === 0) {
      setAviso('Escribe una respuesta o adjunta al menos un documento.')
      return
    }
    setEnviando(true)
    setAviso(null)
    try {
      alActualizar(await enviarCorreccion(sesion.token, solicitud.id, comentario, archivos))
    } catch (e) {
      alError(e)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="panel paso-siguiente paso-siguiente--columna">
      <div>
        <h2>El municipio necesita información adicional</h2>
        <blockquote className="mensaje-funcionario">{solicitud.informacionSolicitada}</blockquote>
      </div>
      <form className="pila" onSubmit={enviar} noValidate>
        {aviso && <Aviso tono="error">{aviso}</Aviso>}
        <Campo etiqueta="Tu respuesta">
          <textarea className="control control--area" rows={3} maxLength={1000} value={comentario} onChange={(e) => setComentario(e.target.value)} />
        </Campo>
        <div>
          <p className="campo__etiqueta">Reemplaza o agrega documentos (opcional)</p>
          <div className="requisitos-lista requisitos-lista--compacta">
            {solicitud.tramite.requisitos.map((r) => (
              <SelectorArchivo
                key={r.id}
                requisito={{ ...r, obligatorio: true }}
                archivo={archivos[r.id]}
                error={errores[r.id]}
                alElegir={(a) => {
                  const problema = validarArchivo(a)
                  setErrores((e) => ({ ...e, [r.id]: problema }))
                  if (!problema) setArchivos((x) => ({ ...x, [r.id]: a }))
                }}
                alQuitar={() => setArchivos(({ [r.id]: _, ...resto }) => resto)}
              />
            ))}
          </div>
        </div>
        <div>
          <Boton type="submit" cargando={enviando}>
            Enviar corrección
          </Boton>
        </div>
      </form>
    </section>
  )
}
