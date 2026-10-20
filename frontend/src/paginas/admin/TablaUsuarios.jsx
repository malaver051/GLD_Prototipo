import Boton from '../../componentes/Boton'
import { fechaCorta } from '../../utilidades/formato'
import { NOMBRE_DOCUMENTO } from '../../utilidades/rutas'

function Estado({ usuario }) {
  if (usuario.estado === 'INACTIVO') return <span className="pastilla pastilla--inactivo">Inactiva</span>
  if (usuario.estado === 'PENDIENTE_MFA') return <span className="pastilla pastilla--pendiente">Sin autenticador</span>
  if (usuario.debeCambiarClave) return <span className="pastilla pastilla--pendiente">Clave temporal</span>
  return <span className="pastilla pastilla--activo">Activa</span>
}

export default function TablaUsuarios({ usuarios, rol, busqueda, alRestablecer, alCambiarEstado, alCrear }) {
  if (usuarios === null) {
    return (
      <div className="vacio">
        <span>Cargando cuentas…</span>
      </div>
    )
  }

  if (usuarios.length === 0) {
    if (busqueda) {
      return (
        <div className="vacio">
          <strong>Sin resultados para «{busqueda}»</strong>
          <span>Revisa la ortografía o busca por correo.</span>
        </div>
      )
    }
    return rol === 'FUNCIONARIO' ? (
      <div className="vacio">
        <strong>Todavía no hay funcionarios</strong>
        <span>Crea la primera cuenta; el funcionario recibirá una contraseña temporal en su correo institucional.</span>
        <Boton variante="secundario" pequeno onClick={alCrear}>
          Crear funcionario
        </Boton>
      </div>
    ) : (
      <div className="vacio">
        <strong>Todavía no hay ciudadanos registrados</strong>
        <span>Los ciudadanos crean su cuenta desde «Crea tu cuenta ciudadana» en la pantalla de ingreso.</span>
      </div>
    )
  }

  return (
    <div className="tabla-contenedor">
      <table className="tabla">
        <thead>
          <tr>
            <th scope="col">Nombre</th>
            <th scope="col">Municipio</th>
            <th scope="col">Documento</th>
            <th scope="col">Estado</th>
            <th scope="col">Último acceso</th>
            <th scope="col">
              <span className="visualmente-oculto">Acciones</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {usuarios.map((u) => (
            <tr key={u.id}>
              <td>
                <div className="tabla__principal">
                  <strong>{u.nombreCompleto}</strong>
                  <span className="tabla__secundario">{u.correo}</span>
                </div>
              </td>
              <td>
                <div className="tabla__principal">
                  <span>{u.municipio?.nombre}</span>
                  <span className="tabla__secundario">{u.municipio?.estado}</span>
                </div>
              </td>
              <td>
                <div className="tabla__principal">
                  <span>{u.numeroDocumento}</span>
                  <span className="tabla__secundario">{NOMBRE_DOCUMENTO[u.tipoDocumento]}</span>
                </div>
              </td>
              <td>
                <Estado usuario={u} />
              </td>
              <td className="tabla__secundario">{fechaCorta(u.ultimoAcceso)}</td>
              <td>
                <div className="tabla__acciones">
                  {u.rol === 'FUNCIONARIO' && u.estado !== 'INACTIVO' && (
                    <Boton variante="texto" onClick={() => alRestablecer(u)}>
                      Restablecer contraseña
                    </Boton>
                  )}
                  <Boton variante="texto" onClick={() => alCambiarEstado(u)}>
                    {u.estado === 'INACTIVO' ? 'Activar' : 'Desactivar'}
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
