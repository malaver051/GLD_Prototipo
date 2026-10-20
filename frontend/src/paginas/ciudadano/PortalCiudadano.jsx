import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import DisenoApp from '../../componentes/DisenoApp'
import EstadoSolicitud from '../../componentes/EstadoSolicitud'
import Icono from '../../componentes/Icono'
import PanelCuenta from '../../componentes/PanelCuenta'
import { useSesion } from '../../contexto/SesionContext'
import { listarSolicitudes } from '../../servicios/tramitesService'
import { fechaCorta, fechaDia, moneda } from '../../utilidades/formato'
import { NAV_CIUDADANO } from '../../utilidades/navegacion'
import { useErrorSesion } from '../../utilidades/useErrorSesion'

// Siguiente paso que le toca al ciudadano según el estado.
const SIGUIENTE = {
  PENDIENTE_PAGO: 'Realiza el pago para que la revisen',
  REQUIERE_INFORMACION: 'Responde a la solicitud de información',
  EN_REVISION: 'El municipio la está revisando',
  APROBADA: 'Descarga tu documento oficial',
  RECHAZADA: 'Consulta el motivo del rechazo',
}

export default function PortalCiudadano() {
  const { sesion } = useSesion()
  const atenderSesion = useErrorSesion()
  const [solicitudes, setSolicitudes] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    listarSolicitudes(sesion.token)
      .then(setSolicitudes)
      .catch((e) => {
        if (!atenderSesion(e)) setError(e)
      })
  }, [sesion.token, atenderSesion])

  const primerNombre = (sesion.usuario?.nombreCompleto || '').split(' ')[0]
  const pendientes = (solicitudes || []).filter((s) => ['PENDIENTE_PAGO', 'REQUIERE_INFORMACION'].includes(s.estado))

  return (
    <DisenoApp seccion="Portal ciudadano" navegacion={NAV_CIUDADANO}>
      <div className="encabezado-pagina">
        <div>
          <h1>Hola, {primerNombre}.</h1>
          <p>
            {pendientes.length > 0
              ? `Tienes ${pendientes.length} ${pendientes.length === 1 ? 'solicitud que necesita' : 'solicitudes que necesitan'} una acción tuya.`
              : `Solicita trámites de ${sesion.usuario?.municipio || 'tu municipio'} y sigue su estado desde aquí.`}
          </p>
        </div>
        <div className="fila-botones">
          <Link to="/portal/incidencias/nueva" className="boton boton--secundario">
            <Icono nombre="ubicacion" tamano={16} />
            Reportar incidencia
          </Link>
          <Link to="/portal/tramites" className="boton boton--primario">
            <Icono nombre="mas" tamano={16} />
            Solicitar un trámite
          </Link>
        </div>
      </div>

      {error && (
        <div className="acceso__mensaje">
          <Aviso tono="error">{error.message}</Aviso>
        </div>
      )}

      <div className="columnas">
        <section className="panel" aria-labelledby="titulo-solicitudes">
          <div className="panel__cabecera">
            <h2 id="titulo-solicitudes" className="panel__titulo">
              Mis solicitudes
            </h2>
          </div>
          {solicitudes === null ? (
            <div className="vacio">
              <span>Cargando solicitudes…</span>
            </div>
          ) : solicitudes.length === 0 ? (
            <div className="vacio">
              <strong>Aún no tienes solicitudes</strong>
              <span>Elige un trámite del catálogo, adjunta los requisitos y síguelo desde aquí.</span>
              <Link to="/portal/tramites" className="boton boton--secundario boton--pequeno">
                Ver trámites disponibles
              </Link>
            </div>
          ) : (
            <ul className="lista-solicitudes">
              {solicitudes.map((s) => (
                <li key={s.id}>
                  <Link to={`/portal/solicitudes/${s.id}`}>
                    <div className="lista-solicitudes__principal">
                      <strong>{s.tramite}</strong>
                      <span>
                        {s.numeroRadicado} · {fechaCorta(s.fechaRadicacion)}
                      </span>
                    </div>
                    <div className="lista-solicitudes__estado">
                      <EstadoSolicitud estado={s.estado} />
                      <span>
                        {SIGUIENTE[s.estado]}
                        {s.estado === 'PENDIENTE_PAGO' ? ` (${moneda(s.costo)})` : ''}
                        {s.estado === 'EN_REVISION' && s.fechaLimite ? `, respuesta a más tardar el ${fechaDia(s.fechaLimite)}` : ''}
                      </span>
                    </div>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </section>
        <PanelCuenta />
      </div>
    </DisenoApp>
  )
}
