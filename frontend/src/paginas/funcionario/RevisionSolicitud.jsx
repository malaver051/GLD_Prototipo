import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import Campo from '../../componentes/Campo'
import DisenoApp from '../../componentes/DisenoApp'
import EstadoSolicitud from '../../componentes/EstadoSolicitud'
import Icono from '../../componentes/Icono'
import LineaTiempo from '../../componentes/LineaTiempo'
import ListaDocumentos from '../../componentes/ListaDocumentos'
import { useSesion } from '../../contexto/SesionContext'
import { consultarSolicitud, descargarDocumento, pedirInformacion, resolverSolicitud } from '../../servicios/tramitesService'
import { fechaCorta, fechaDia, moneda } from '../../utilidades/formato'
import { NAV_FUNCIONARIO } from '../../utilidades/navegacion'
import { useErrorSesion } from '../../utilidades/useErrorSesion'

// Las tres salidas del funcionario en el BPMN: «¿Información adicional?» y «¿Aprueba?».
const DECISIONES = {
  APROBAR: {
    texto: 'Aprobar',
    etiqueta: 'Observaciones para el documento (opcional)',
    ayuda: 'Se imprimen en el documento oficial. Ejemplo: vigencia o condiciones.',
    boton: 'Aprobar y emitir documento',
    obligatorio: false,
  },
  SOLICITAR_INFORMACION: {
    texto: 'Pedir información',
    etiqueta: '¿Qué necesitas que el ciudadano corrija o agregue?',
    ayuda: 'El ciudadano verá este mensaje y podrá adjuntar documentos nuevos.',
    boton: 'Enviar solicitud de información',
    obligatorio: true,
  },
  RECHAZAR: {
    texto: 'Rechazar',
    etiqueta: 'Motivo del rechazo',
    ayuda: 'Es obligatorio y el ciudadano lo verá en su solicitud.',
    boton: 'Rechazar solicitud',
    obligatorio: true,
  },
}

export default function RevisionSolicitud() {
  const { id } = useParams()
  const { sesion } = useSesion()
  const atenderSesion = useErrorSesion()
  const [solicitud, setSolicitud] = useState(null)
  const [decision, setDecision] = useState('APROBAR')
  const [texto, setTexto] = useState('')
  const [errorTexto, setErrorTexto] = useState(null)
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const [confirmacion, setConfirmacion] = useState(null)

  const manejarError = useCallback((e) => {
    if (!atenderSesion(e)) setError(e)
  }, [atenderSesion])

  useEffect(() => {
    consultarSolicitud(sesion.token, id).then(setSolicitud).catch(manejarError)
  }, [sesion.token, id, manejarError])

  async function decidir(evento) {
    evento.preventDefault()
    const config = DECISIONES[decision]
    if (config.obligatorio && !texto.trim()) {
      setErrorTexto(decision === 'RECHAZAR' ? 'Escribe el motivo del rechazo.' : 'Escribe qué necesitas del ciudadano.')
      return
    }
    setEnviando(true)
    setError(null)
    try {
      const actualizada =
        decision === 'SOLICITAR_INFORMACION'
          ? await pedirInformacion(sesion.token, solicitud.id, texto.trim())
          : await resolverSolicitud(sesion.token, solicitud.id, decision === 'APROBAR', texto.trim() || null)
      setSolicitud(actualizada)
      setTexto('')
      setConfirmacion(
        decision === 'APROBAR'
          ? 'Solicitud aprobada. Se emitió el documento oficial y se notificó al ciudadano.'
          : decision === 'RECHAZAR'
            ? 'Solicitud rechazada. El ciudadano recibió el motivo.'
            : 'Se pidió la información al ciudadano. La solicitud volverá a tu bandeja cuando responda.',
      )
    } catch (e) {
      manejarError(e)
    } finally {
      setEnviando(false)
    }
  }

  if (!solicitud) {
    return (
      <DisenoApp seccion="Consola municipal" navegacion={NAV_FUNCIONARIO}>
        {error ? <Aviso tono="error">{error.message}</Aviso> : <p className="texto-suave">Cargando solicitud…</p>}
      </DisenoApp>
    )
  }

  const s = solicitud
  const puedeDecidir = s.accionesPermitidas.includes('APROBAR')
  const config = DECISIONES[decision]

  return (
    <DisenoApp seccion="Consola municipal" navegacion={NAV_FUNCIONARIO}>
      <Link to="/consola" className="volver">
        <Icono nombre="atras" tamano={16} /> Bandeja
      </Link>

      <header className="encabezado-pagina encabezado-pagina--compacto">
        <div>
          <span className="radicado">{s.numeroRadicado}</span>
          <h1>{s.tramite.nombre}</h1>
          <p>
            Radicada el {fechaCorta(s.fechaRadicacion)}
            {s.fechaLimite && !['APROBADA', 'RECHAZADA'].includes(s.estado) && (
              <>
                {' · '}
                <span className={s.vencida ? 'vence vence--vencida' : 'vence'}>
                  {s.vencida ? 'Venció' : 'Vence'} el {fechaDia(s.fechaLimite)}
                </span>
              </>
            )}
          </p>
        </div>
        <EstadoSolicitud estado={s.estado} />
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
              <h2 className="panel__titulo">Solicitante</h2>
            </div>
            <dl className="datos">
              <div>
                <dt>Nombre</dt>
                <dd>{s.ciudadano.nombre}</dd>
              </div>
              <div>
                <dt>Identificación</dt>
                <dd>{s.ciudadano.documento}</dd>
              </div>
              <div>
                <dt>Correo</dt>
                <dd>{s.ciudadano.correo}</dd>
              </div>
              <div>
                <dt>Mensaje</dt>
                <dd>{s.observaciones || <span className="texto-suave">Sin mensaje</span>}</dd>
              </div>
            </dl>
          </section>

          <section className="panel separado">
            <div className="panel__cabecera">
              <h2 className="panel__titulo">Documentos</h2>
              <span className="texto-suave">
                {s.adjuntos.length} de {s.tramite.requisitos.length} requisitos
              </span>
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
          {puedeDecidir ? (
            <form className="panel decision" onSubmit={decidir} noValidate>
              <div className="panel__cabecera">
                <h2 className="panel__titulo">Tu decisión</h2>
              </div>
              <div className="panel__cuerpo pila">
                <div className="segmentado segmentado--bloque" role="group" aria-label="Decisión">
                  {Object.entries(DECISIONES).map(([clave, d]) => (
                    <button
                      key={clave}
                      type="button"
                      aria-pressed={decision === clave}
                      onClick={() => {
                        setDecision(clave)
                        setErrorTexto(null)
                      }}
                    >
                      {d.texto}
                    </button>
                  ))}
                </div>
                <Campo etiqueta={config.etiqueta} ayuda={config.ayuda} error={errorTexto}>
                  <textarea
                    className="control control--area"
                    rows={4}
                    maxLength={1000}
                    value={texto}
                    onChange={(e) => {
                      setTexto(e.target.value)
                      setErrorTexto(null)
                    }}
                  />
                </Campo>
                <Boton type="submit" bloque variante={decision === 'RECHAZAR' ? 'peligro' : 'primario'} cargando={enviando}>
                  {config.boton}
                </Boton>
              </div>
            </form>
          ) : (
            <section className="panel">
              <div className="panel__cuerpo">
                {s.estado === 'PENDIENTE_PAGO' && <p>Esperando el pago del ciudadano. Podrás revisarla cuando se confirme.</p>}
                {s.estado === 'REQUIERE_INFORMACION' && (
                  <p>
                    Esperando la respuesta del ciudadano a: <em>«{s.informacionSolicitada}»</em>
                  </p>
                )}
                {s.estado === 'APROBADA' && s.documentoOficial && (
                  <div className="pila pila--compacta">
                    <p>
                      Aprobada el {fechaCorta(s.fechaResolucion)}. Código de verificación{' '}
                      <strong>{s.documentoOficial.codigoVerificacion}</strong>.
                    </p>
                    <Boton variante="secundario" onClick={() => descargarDocumento(sesion.token, s.id, s.documentoOficial).catch(manejarError)}>
                      <Icono nombre="descargar" tamano={16} />
                      Descargar documento oficial
                    </Boton>
                  </div>
                )}
                {s.estado === 'RECHAZADA' && (
                  <p>
                    Rechazada el {fechaCorta(s.fechaResolucion)}. Motivo: {s.motivoResolucion}
                  </p>
                )}
              </div>
            </section>
          )}

          <section className="panel separado">
            <dl className="datos">
              <div>
                <dt>Costo</dt>
                <dd>{Number(s.costo) > 0 ? moneda(s.costo) : 'Gratuito'}</dd>
              </div>
              <div>
                <dt>Pago</dt>
                <dd>
                  {Number(s.costo) === 0
                    ? 'No aplica'
                    : s.pagos.find((p) => p.estado === 'APROBADO')
                      ? `Aprobado · ${s.pagos.find((p) => p.estado === 'APROBADO').referencia}`
                      : 'Pendiente'}
                </dd>
              </div>
              <div>
                <dt>Dependencia</dt>
                <dd>{s.tramite.dependencia}</dd>
              </div>
            </dl>
          </section>
        </aside>
      </div>
    </DisenoApp>
  )
}
