import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import DisenoApp from '../../componentes/DisenoApp'
import EstadoIncidencia from '../../componentes/EstadoIncidencia'
import Icono from '../../componentes/Icono'
import { useSesion } from '../../contexto/SesionContext'
import { listarIncidencias } from '../../servicios/comunidadService'
import { fechaCorta } from '../../utilidades/formato'
import { NAV_CIUDADANO } from '../../utilidades/navegacion'
import { useErrorSesion } from '../../utilidades/useErrorSesion'

// Qué está pasando con la incidencia, en palabras del ciudadano.
function siguiente(i) {
  switch (i.estado) {
    case 'REPORTADA':
      return 'En espera de que el municipio la asigne'
    case 'ASIGNADA':
      return `Asignada a ${i.dependencia}`
    case 'EN_ATENCION':
      return `${i.dependencia} la está atendiendo`
    case 'RESUELTA':
      return 'Resuelta. Califica la atención'
    case 'CERRADA':
      return i.calificacion != null ? `Calificaste la atención con ${i.calificacion} de 10` : 'Cerrada'
    default:
      return ''
  }
}

// HU-09 · El ciudadano consulta el estado de sus incidencias.
export default function MisIncidencias() {
  const { sesion } = useSesion()
  const atenderSesion = useErrorSesion()
  const [incidencias, setIncidencias] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    listarIncidencias(sesion.token)
      .then(setIncidencias)
      .catch((e) => {
        if (!atenderSesion(e)) setError(e)
      })
  }, [sesion.token, atenderSesion])

  const porCalificar = (incidencias || []).filter((i) => i.estado === 'RESUELTA').length

  return (
    <DisenoApp seccion="Portal ciudadano" navegacion={NAV_CIUDADANO}>
      <div className="encabezado-pagina">
        <div>
          <h1>Incidencias</h1>
          <p>
            {porCalificar > 0
              ? `Tienes ${porCalificar} ${porCalificar === 1 ? 'incidencia resuelta' : 'incidencias resueltas'} por calificar.`
              : `Reporta baches, luminarias apagadas, basura u otros problemas de ${sesion.usuario?.municipio || 'tu municipio'} y sigue su atención.`}
          </p>
        </div>
        <Link to="/portal/incidencias/nueva" className="boton boton--primario">
          <Icono nombre="mas" tamano={16} />
          Reportar incidencia
        </Link>
      </div>

      {error && (
        <div className="acceso__mensaje">
          <Aviso tono="error">{error.message}</Aviso>
        </div>
      )}

      <section className="panel" aria-labelledby="titulo-incidencias">
        <div className="panel__cabecera">
          <h2 id="titulo-incidencias" className="panel__titulo">
            Mis reportes
          </h2>
        </div>
        {incidencias === null ? (
          <div className="vacio">
            <span>Cargando incidencias…</span>
          </div>
        ) : incidencias.length === 0 ? (
          <div className="vacio">
            <strong>Aún no has reportado incidencias</strong>
            <span>Cuando reportes una, aquí verás su número de seguimiento y en qué etapa va.</span>
            <Link to="/portal/incidencias/nueva" className="boton boton--secundario boton--pequeno">
              Reportar la primera
            </Link>
          </div>
        ) : (
          <ul className="lista-solicitudes">
            {incidencias.map((i) => (
              <li key={i.id}>
                <Link to={`/portal/incidencias/${i.id}`}>
                  <div className="lista-solicitudes__principal">
                    <strong>
                      <span className="categoria__punto" data-categoria={i.categoria} aria-hidden="true" />
                      {i.categoriaEtiqueta}
                    </strong>
                    <span className="lista-incidencias__descripcion">{i.descripcion}</span>
                    <span>
                      {i.numeroSeguimiento} · {fechaCorta(i.fechaReporte)}
                    </span>
                  </div>
                  <div className="lista-solicitudes__estado">
                    <EstadoIncidencia estado={i.estado} />
                    <span>{siguiente(i)}</span>
                  </div>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </DisenoApp>
  )
}
