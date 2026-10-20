import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import Campo from '../../componentes/Campo'
import DisenoApp from '../../componentes/DisenoApp'
import Icono from '../../componentes/Icono'
import SelectorArchivo, { validarArchivo } from '../../componentes/SelectorArchivo'
import { useSesion } from '../../contexto/SesionContext'
import { consultarTipoTramite, radicarSolicitud } from '../../servicios/tramitesService'
import { moneda } from '../../utilidades/formato'
import { NAV_CIUDADANO } from '../../utilidades/navegacion'
import { useErrorSesion } from '../../utilidades/useErrorSesion'

// BPMN: el ciudadano diligencia la solicitud y adjunta los requisitos; si falta alguno obligatorio no se radica.
export default function SolicitarTramite() {
  const { id } = useParams()
  const { sesion } = useSesion()
  const navegar = useNavigate()
  const atenderSesion = useErrorSesion()
  const [tramite, setTramite] = useState(null)
  const [archivos, setArchivos] = useState({})
  const [errores, setErrores] = useState({})
  const [observaciones, setObservaciones] = useState('')
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    consultarTipoTramite(sesion.token, id)
      .then(setTramite)
      .catch((e) => {
        if (!atenderSesion(e)) setError(e)
      })
  }, [sesion.token, id, atenderSesion])

  function elegir(requisito, archivo) {
    const problema = validarArchivo(archivo)
    setErrores((e) => ({ ...e, [requisito.id]: problema }))
    if (!problema) setArchivos((a) => ({ ...a, [requisito.id]: archivo }))
  }

  function quitar(requisito) {
    setArchivos(({ [requisito.id]: _, ...resto }) => resto)
  }

  async function radicar(evento) {
    evento.preventDefault()
    const faltantes = {}
    tramite.requisitos.forEach((r) => {
      if (r.obligatorio && !archivos[r.id]) faltantes[r.id] = 'Este documento es obligatorio.'
    })
    setErrores(faltantes)
    if (Object.keys(faltantes).length > 0) {
      setError({ message: 'Faltan documentos obligatorios. Adjúntalos para radicar la solicitud.' })
      return
    }
    setEnviando(true)
    setError(null)
    try {
      const solicitud = await radicarSolicitud(sesion.token, tramite.id, observaciones, archivos)
      navegar(`/portal/solicitudes/${solicitud.id}`, { replace: true, state: { recienRadicada: true } })
    } catch (e) {
      if (!atenderSesion(e)) setError(e)
    } finally {
      setEnviando(false)
    }
  }

  const adjuntos = tramite ? tramite.requisitos.filter((r) => archivos[r.id]).length : 0
  const obligatorios = tramite ? tramite.requisitos.filter((r) => r.obligatorio).length : 0
  const obligatoriosListos = tramite ? tramite.requisitos.filter((r) => r.obligatorio && archivos[r.id]).length : 0

  return (
    <DisenoApp seccion="Portal ciudadano" navegacion={NAV_CIUDADANO}>
      <Link to="/portal/tramites" className="volver">
        <Icono nombre="atras" tamano={16} /> Volver al catálogo
      </Link>

      {!tramite ? (
        error ? <Aviso tono="error">{error.message}</Aviso> : <p className="texto-suave">Cargando trámite…</p>
      ) : (
        <form className="detalle" onSubmit={radicar} noValidate>
          <div className="detalle__principal">
            <header className="encabezado-pagina encabezado-pagina--compacto">
              <div>
                <h1>{tramite.nombre}</h1>
                <p>{tramite.descripcion}</p>
              </div>
            </header>

            {error && (
              <div className="acceso__mensaje">
                <Aviso tono="error" errores={error.errores}>
                  {error.message}
                </Aviso>
              </div>
            )}

            <section className="panel">
              <div className="panel__cabecera">
                <h2 className="panel__titulo">Requisitos</h2>
                <span className="texto-suave">
                  {obligatoriosListos} de {obligatorios} obligatorios adjuntos
                </span>
              </div>
              <div className="requisitos-lista">
                {tramite.requisitos.map((r) => (
                  <SelectorArchivo
                    key={r.id}
                    requisito={r}
                    archivo={archivos[r.id]}
                    error={errores[r.id]}
                    alElegir={(a) => elegir(r, a)}
                    alQuitar={() => quitar(r)}
                  />
                ))}
              </div>
            </section>

            <section className="panel separado">
              <div className="panel__cuerpo">
                <Campo etiqueta="Motivo o detalles de tu solicitud (opcional)" ayuda="Por ejemplo: para qué necesitas el documento.">
                  <textarea
                    className="control control--area"
                    rows={3}
                    maxLength={1000}
                    value={observaciones}
                    onChange={(e) => setObservaciones(e.target.value)}
                  />
                </Campo>
              </div>
            </section>
          </div>

          <aside className="detalle__lateral">
            <section className="panel resumen">
              <dl className="datos">
                <div>
                  <dt>Costo</dt>
                  <dd className="resumen__cifra">{tramite.requierePago ? moneda(tramite.costo) : 'Gratuito'}</dd>
                </div>
                <div>
                  <dt>Respuesta</dt>
                  <dd>{tramite.plazoDias} días hábiles</dd>
                </div>
                <div>
                  <dt>Atiende</dt>
                  <dd>{tramite.dependencia}</dd>
                </div>
                <div>
                  <dt>Adjuntos</dt>
                  <dd>
                    {adjuntos} de {tramite.requisitos.length}
                  </dd>
                </div>
              </dl>
              <div className="resumen__pie">
                <p className="texto-suave">
                  {tramite.requierePago
                    ? 'Después de radicar pagarás en línea. El plazo de respuesta empieza a contar cuando se confirma el pago.'
                    : 'Este trámite no tiene costo: al radicarlo pasa directo a revisión.'}
                </p>
                <Boton type="submit" bloque cargando={enviando}>
                  Radicar solicitud
                </Boton>
              </div>
            </section>
          </aside>
        </form>
      )}
    </DisenoApp>
  )
}
