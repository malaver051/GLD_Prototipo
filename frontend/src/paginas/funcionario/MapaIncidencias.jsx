import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import DisenoApp from '../../componentes/DisenoApp'
import EstadoIncidencia, { CATEGORIAS, COLOR_ESTADO, ETIQUETA_INCIDENCIA } from '../../componentes/EstadoIncidencia'
import Mapa from '../../componentes/Mapa'
import { useSesion } from '../../contexto/SesionContext'
import {
  consultarMapaMunicipio,
  listarDependencias,
  listarIncidencias,
  resumenIncidencias,
} from '../../servicios/comunidadService'
import { fechaCorta } from '../../utilidades/formato'
import { NAV_FUNCIONARIO } from '../../utilidades/navegacion'
import { useErrorSesion } from '../../utilidades/useErrorSesion'

const ESTADOS = Object.keys(ETIQUETA_INCIDENCIA)

// Orden de la lista: primero lo que espera acción del municipio, y dentro de eso lo más antiguo.
const PRIORIDAD = { REPORTADA: 0, ASIGNADA: 1, EN_ATENCION: 2, RESUELTA: 3, CERRADA: 4 }

// HU-10 · El funcionario ve las incidencias del municipio en un mapa, las filtra y abre una para atenderla.
export default function MapaIncidencias() {
  const { sesion } = useSesion()
  const atenderSesion = useErrorSesion()
  const [mapa, setMapa] = useState(null)
  const [incidencias, setIncidencias] = useState(null)
  const [resumen, setResumen] = useState(null)
  const [dependencias, setDependencias] = useState([])
  const [filtros, setFiltros] = useState({ estado: '', categoria: '', idDependencia: '' })
  const [seleccionada, setSeleccionada] = useState(null)
  const [enfocar, setEnfocar] = useState(null)
  const [error, setError] = useState(null)

  const manejarError = useCallback(
    (e) => {
      if (!atenderSesion(e)) setError(e)
    },
    [atenderSesion],
  )

  useEffect(() => {
    consultarMapaMunicipio(sesion.token).then(setMapa).catch(manejarError)
    listarDependencias(sesion.token).then(setDependencias).catch(manejarError)
    resumenIncidencias(sesion.token).then(setResumen).catch(manejarError)
  }, [sesion.token, manejarError])

  useEffect(() => {
    listarIncidencias(sesion.token, filtros)
      .then((lista) =>
        setIncidencias(
          [...lista].sort(
            (a, b) => PRIORIDAD[a.estado] - PRIORIDAD[b.estado] || a.fechaReporte.localeCompare(b.fechaReporte),
          ),
        ),
      )
      .catch(manejarError)
  }, [sesion.token, filtros, manejarError])

  const marcadores = useMemo(
    () =>
      (incidencias || []).map((i) => ({
        id: i.id,
        lat: i.latitud,
        lng: i.longitud,
        color: COLOR_ESTADO[i.estado],
        titulo: `${i.categoriaEtiqueta} · ${ETIQUETA_INCIDENCIA[i.estado]}`,
        activo: i.id === seleccionada,
      })),
    [incidencias, seleccionada],
  )

  function elegir(id, acercar) {
    setSeleccionada(id)
    const i = incidencias.find((x) => x.id === id)
    if (acercar && i) setEnfocar({ lat: i.latitud, lng: i.longitud })
    document.getElementById(`incidencia-${id}`)?.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
  }

  const cambiarFiltro = (campo, valor) => setFiltros((f) => ({ ...f, [campo]: valor }))
  const hayFiltros = Object.values(filtros).some(Boolean)

  return (
    <DisenoApp seccion="Consola municipal" navegacion={NAV_FUNCIONARIO}>
      <div className="encabezado-pagina">
        <div>
          <h1>Incidencias de {mapa?.municipio || sesion.usuario?.municipio}</h1>
          <p>Toca un punto del mapa o una fila para ver el reporte. Las que esperan asignación aparecen primero.</p>
        </div>
      </div>

      {resumen && (
        <dl className="cifras" aria-label="Resumen del municipio">
          <div>
            <dt>Abiertas</dt>
            <dd>{resumen.abiertas}</dd>
          </div>
          {['REPORTADA', 'ASIGNADA', 'EN_ATENCION', 'RESUELTA'].map((e) => (
            <div key={e}>
              <dt>
                <span className="estado-punto" style={{ background: COLOR_ESTADO[e] }} aria-hidden="true" />
                {ETIQUETA_INCIDENCIA[e]}
              </dt>
              <dd>{resumen.porEstado[e] ?? 0}</dd>
            </div>
          ))}
          <div>
            <dt>Calificación promedio</dt>
            <dd>
              {resumen.promedioCalificacion == null ? '—' : resumen.promedioCalificacion.toFixed(1)}
              <span className="cifras__nota">{resumen.calificadas} calificadas</span>
            </dd>
          </div>
        </dl>
      )}

      {error && (
        <div className="acceso__mensaje">
          <Aviso tono="error">{error.message}</Aviso>
        </div>
      )}

      <div className="filtros" role="group" aria-label="Filtros">
        <label>
          <span>Estado</span>
          <select className="control" value={filtros.estado} onChange={(e) => cambiarFiltro('estado', e.target.value)}>
            <option value="">Todos</option>
            {ESTADOS.map((e) => (
              <option key={e} value={e}>
                {ETIQUETA_INCIDENCIA[e]}
              </option>
            ))}
          </select>
        </label>
        <label>
          <span>Categoría</span>
          <select className="control" value={filtros.categoria} onChange={(e) => cambiarFiltro('categoria', e.target.value)}>
            <option value="">Todas</option>
            {CATEGORIAS.map((c) => (
              <option key={c.valor} value={c.valor}>
                {c.texto}
              </option>
            ))}
          </select>
        </label>
        <label>
          <span>Dependencia</span>
          <select
            className="control"
            value={filtros.idDependencia}
            onChange={(e) => cambiarFiltro('idDependencia', e.target.value)}
          >
            <option value="">Todas</option>
            {dependencias.map((d) => (
              <option key={d.id} value={d.id}>
                {d.nombre}
              </option>
            ))}
          </select>
        </label>
        {hayFiltros && (
          <button type="button" className="boton boton--texto" onClick={() => setFiltros({ estado: '', categoria: '', idDependencia: '' })}>
            Limpiar filtros
          </button>
        )}
      </div>

      <div className="tablero-mapa">
        <section className="panel tablero-mapa__mapa">
          {mapa ? (
            <Mapa
              centro={[mapa.latitud, mapa.longitud]}
              zoom={mapa.zoom}
              marcadores={marcadores}
              alSeleccionarMarcador={(id) => elegir(id, false)}
              ajustar
              enfocar={enfocar}
              altura={560}
              etiqueta={`Mapa de incidencias de ${mapa.municipio}`}
            />
          ) : (
            <div className="mapa mapa--cargando" style={{ height: 560 }}>
              Cargando mapa…
            </div>
          )}
          <ul className="leyenda" aria-label="Leyenda">
            {ESTADOS.map((e) => (
              <li key={e}>
                <span className="estado-punto" style={{ background: COLOR_ESTADO[e] }} aria-hidden="true" />
                {ETIQUETA_INCIDENCIA[e]}
              </li>
            ))}
          </ul>
        </section>

        <section className="panel tablero-mapa__lista" aria-label="Lista de incidencias">
          <div className="panel__cabecera">
            <h2 className="panel__titulo">{incidencias ? `${incidencias.length} incidencias` : 'Incidencias'}</h2>
          </div>
          {incidencias === null ? (
            <div className="vacio">
              <span>Cargando…</span>
            </div>
          ) : incidencias.length === 0 ? (
            <div className="vacio">
              <strong>{hayFiltros ? 'Ninguna incidencia coincide con los filtros' : 'No hay incidencias reportadas'}</strong>
              <span>{hayFiltros ? 'Prueba con otros filtros.' : 'Los reportes de los ciudadanos aparecerán aquí y en el mapa.'}</span>
            </div>
          ) : (
            <ul className="lista-incidencias">
              {incidencias.map((i) => (
                <li key={i.id} id={`incidencia-${i.id}`} data-activa={i.id === seleccionada}>
                  <button type="button" className="lista-incidencias__fila" onClick={() => elegir(i.id, true)}>
                    <span className="lista-incidencias__cabecera">
                      <strong>{i.categoriaEtiqueta}</strong>
                      <EstadoIncidencia estado={i.estado} />
                    </span>
                    <span className="lista-incidencias__descripcion">{i.descripcion}</span>
                    <span className="lista-incidencias__meta">
                      {i.numeroSeguimiento} · {fechaCorta(i.fechaReporte)}
                      {i.dependencia ? ` · ${i.dependencia}` : ''}
                    </span>
                  </button>
                  <Link to={`/consola/incidencias/${i.id}`} className="boton boton--secundario boton--pequeno">
                    {['RESUELTA', 'CERRADA'].includes(i.estado) ? 'Ver' : 'Atender'}
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </DisenoApp>
  )
}
