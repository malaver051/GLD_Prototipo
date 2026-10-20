import Boton from '../../componentes/Boton'
import { fechaCorta } from '../../utilidades/formato'

export default function TablaSolicitudes({ solicitudes, alAtender }) {
  if (solicitudes.length === 0) {
    return (
      <div className="vacio">
        <strong>No hay solicitudes pendientes</strong>
        <span>Cuando un funcionario use «¿Olvidaste tu contraseña?», su solicitud aparecerá aquí.</span>
      </div>
    )
  }

  return (
    <div className="tabla-contenedor">
      <table className="tabla">
        <thead>
          <tr>
            <th scope="col">Funcionario</th>
            <th scope="col">Municipio</th>
            <th scope="col">Solicitada</th>
            <th scope="col">
              <span className="visualmente-oculto">Acciones</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {solicitudes.map((s) => (
            <tr key={s.id}>
              <td>
                <div className="tabla__principal">
                  <strong>{s.nombreCompleto}</strong>
                  <span className="tabla__secundario">{s.correo}</span>
                </div>
              </td>
              <td>{s.municipio}</td>
              <td className="tabla__secundario">{fechaCorta(s.fechaSolicitud)}</td>
              <td>
                <div className="tabla__acciones">
                  <Boton variante="secundario" pequeno onClick={() => alAtender(s)}>
                    Enviar contraseña temporal
                  </Boton>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
