import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import DisenoApp from '../../componentes/DisenoApp'
import EstadoSolicitud from '../../componentes/EstadoSolicitud'
import { useSesion } from '../../contexto/SesionContext'
import { listarSolicitudes } from '../../servicios/tramitesService'
import { fechaCorta, fechaDia, moneda } from '../../utilidades/formato'
import { NAV_FUNCIONARIO } from '../../utilidades/navegacion'
import { useErrorSesion } from '../../utilidades/useErrorSesion'

const GRUPOS = [
  { clave: 'POR_REVISAR', texto: 'Por revisar', vacio: 'No tienes solicitudes por revisar. Las nuevas aparecerán aquí, ordenadas por fecha de vencimiento.' },
  { clave: 'EN_ESPERA', texto: 'Esperando al ciudadano', vacio: 'Ninguna solicitud está esperando un pago o una corrección.' },
  { clave: 'RESUELTAS', texto: 'Resueltas', vacio: 'Todavía no hay solicitudes aprobadas ni rechazadas.' },
]

// Cualquier funcionario del municipio ve todas las solicitudes del municipio.
export default function BandejaFuncionario() {
  const { sesion } = useSesion()
  const atenderSesion = useErrorSesion()
  const [grupo, setGrupo] = useState('POR_REVISAR')
  const [solicitudes, setSolicitudes] = useState(null)
  const [conteo, setConteo] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    setSolicitudes(null)
    listarSolicitudes(sesion.token, grupo)
      .then(setSolicitudes)
      .catch((e) => {
        if (!atenderSesion(e)) setError(e)
      })
  }, [sesion.token, grupo, atenderSesion])

  useEffect(() => {
    listarSolicitudes(sesion.token, 'POR_REVISAR')
      .then((lista) => setConteo(lista.length))
      .catch(() => {})
  }, [sesion.token, solicitudes])

  const actual = GRUPOS.find((g) => g.clave === grupo)
  const vencidas = (solicitudes || []).filter((s) => s.vencida).length

  return (
    <DisenoApp seccion="Consola municipal" navegacion={NAV_FUNCIONARIO}>
      <div className="encabezado-pagina">
        <div>
          <h1>Bandeja de {sesion.usuario?.municipio}</h1>
          <p>
            {grupo === 'POR_REVISAR' && solicitudes
              ? vencidas > 0
                ? `${vencidas} ${vencidas === 1 ? 'solicitud venció' : 'solicitudes vencieron'}. Atiéndelas primero.`
                : 'Las solicitudes están ordenadas por fecha de vencimiento.'
              : 'Solicitudes de todos los ciudadanos del municipio.'}
          </p>
        </div>
      </div>

      <div className="pestanas" role="tablist">
        {GRUPOS.map((g) => (
          <button key={g.clave} type="button" role="tab" aria-selected={grupo === g.clave} onClick={() => setGrupo(g.clave)}>
            {g.texto}
            {g.clave === 'POR_REVISAR' && conteo > 0 && <span className="contador">{conteo}</span>}
          </button>
        ))}
      </div>

      {error && (
        <div className="acceso__mensaje">
          <Aviso tono="error">{error.message}</Aviso>
        </div>
      )}

      <section className="panel">
        {solicitudes === null ? (
          <div className="vacio">
            <span>Cargando solicitudes…</span>
          </div>
        ) : solicitudes.length === 0 ? (
          <div className="vacio">
            <strong>Nada por aquí</strong>
            <span>{actual.vacio}</span>
          </div>
        ) : (
          <div className="tabla-contenedor">
            <table className="tabla">
              <thead>
                <tr>
                  <th scope="col">Solicitud</th>
                  <th scope="col">Ciudadano</th>
                  <th scope="col">Radicada</th>
                  <th scope="col">{grupo === 'RESUELTAS' ? 'Costo' : 'Vence'}</th>
                  <th scope="col">Estado</th>
                  <th scope="col">
                    <span className="visualmente-oculto">Acciones</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {solicitudes.map((s) => (
                  <tr key={s.id} data-vencida={s.vencida}>
                    <td>
                      <div className="tabla__principal">
                        <strong>{s.tramite}</strong>
                        <span className="tabla__secundario">{s.numeroRadicado}</span>
                      </div>
                    </td>
                    <td>
                      <div className="tabla__principal">
                        <span>{s.nombreCiudadano}</span>
                        <span className="tabla__secundario">{s.correoCiudadano}</span>
                      </div>
                    </td>
                    <td className="tabla__secundario">{fechaCorta(s.fechaRadicacion)}</td>
                    <td>
                      {grupo === 'RESUELTAS' ? (
                        Number(s.costo) > 0 ? moneda(s.costo) : 'Gratuito'
                      ) : s.fechaLimite ? (
                        <span className={s.vencida ? 'vence vence--vencida' : 'vence'}>
                          {fechaDia(s.fechaLimite)}
                          {s.vencida && ' · vencida'}
                        </span>
                      ) : (
                        <span className="tabla__secundario">Tras el pago</span>
                      )}
                    </td>
                    <td>
                      <EstadoSolicitud estado={s.estado} />
                    </td>
                    <td>
                      <div className="tabla__acciones">
                        <Link to={`/consola/solicitudes/${s.id}`} className="boton boton--secundario boton--pequeno">
                          {s.estado === 'EN_REVISION' ? 'Revisar' : 'Ver'}
                        </Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </DisenoApp>
  )
}
