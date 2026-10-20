import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import DisenoApp from '../../componentes/DisenoApp'
import { useSesion } from '../../contexto/SesionContext'
import { consultarCatalogo } from '../../servicios/tramitesService'
import { moneda } from '../../utilidades/formato'
import { NAV_CIUDADANO } from '../../utilidades/navegacion'
import { useErrorSesion } from '../../utilidades/useErrorSesion'

export default function CatalogoTramites() {
  const { sesion } = useSesion()
  const atenderSesion = useErrorSesion()
  const [tramites, setTramites] = useState(null)
  const [busqueda, setBusqueda] = useState('')
  const [error, setError] = useState(null)

  useEffect(() => {
    consultarCatalogo(sesion.token)
      .then(setTramites)
      .catch((e) => {
        if (!atenderSesion(e)) setError(e)
      })
  }, [sesion.token, atenderSesion])

  const texto = busqueda.trim().toLowerCase()
  const visibles = (tramites || []).filter(
    (t) => !texto || t.nombre.toLowerCase().includes(texto) || t.descripcion.toLowerCase().includes(texto),
  )

  return (
    <DisenoApp seccion="Portal ciudadano" navegacion={NAV_CIUDADANO}>
      <div className="encabezado-pagina">
        <div>
          <h1>Trámites de {sesion.usuario?.municipio || 'tu municipio'}</h1>
          <p>Elige el trámite, revisa los requisitos y adjúntalos en PDF, JPG o PNG.</p>
        </div>
        <input
          className="control buscador"
          type="search"
          placeholder="Buscar trámite"
          aria-label="Buscar trámite"
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
        />
      </div>

      {error && <Aviso tono="error">{error.message}</Aviso>}

      <section className="panel">
        {tramites === null ? (
          <div className="vacio">
            <span>Cargando catálogo…</span>
          </div>
        ) : visibles.length === 0 ? (
          <div className="vacio">
            <strong>{texto ? `Ningún trámite coincide con «${busqueda}»` : 'Tu municipio aún no publica trámites en línea'}</strong>
            <span>{texto ? 'Prueba con otra palabra.' : 'Vuelve a consultar más adelante.'}</span>
          </div>
        ) : (
          <ul className="catalogo">
            {visibles.map((t) => (
              <li key={t.id}>
                <div className="catalogo__texto">
                  <h2>{t.nombre}</h2>
                  <p>{t.descripcion}</p>
                  <span className="texto-suave">
                    {t.dependencia} · {t.requisitos.length} requisitos
                  </span>
                </div>
                <dl className="catalogo__cifras">
                  <div>
                    <dt>Costo</dt>
                    <dd>{t.requierePago ? moneda(t.costo) : 'Gratuito'}</dd>
                  </div>
                  <div>
                    <dt>Respuesta</dt>
                    <dd>
                      {t.plazoDias} {t.plazoDias === 1 ? 'día hábil' : 'días hábiles'}
                    </dd>
                  </div>
                </dl>
                <Link to={`/portal/tramites/${t.id}`} className="boton boton--secundario">
                  Solicitar
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </DisenoApp>
  )
}
