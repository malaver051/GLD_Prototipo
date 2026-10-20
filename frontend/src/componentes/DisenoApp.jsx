import { NavLink, useNavigate } from 'react-router-dom'
import { useSesion } from '../contexto/SesionContext'
import { NOMBRE_ROL } from '../utilidades/rutas'
import Boton from './Boton'
import Icono from './Icono'
import Marca from './Marca'

// Plantilla de las pantallas con sesión iniciada.
export default function DisenoApp({ seccion, navegacion = [], children }) {
  const { sesion, cerrarSesion } = useSesion()
  const navegar = useNavigate()
  const usuario = sesion?.usuario

  function salir() {
    cerrarSesion('Cerraste tu sesión.')
    navegar('/ingresar', { replace: true })
  }

  return (
    <div className="app">
      <header className="barra">
        <Marca destino="/" />
        <span className="barra__seccion">{seccion}</span>
        {navegacion.length > 0 && (
          <nav className="barra__nav" aria-label="Secciones">
            {navegacion.map((n) => (
              <NavLink key={n.ruta} to={n.ruta} end={n.exacta}>
                {n.texto}
              </NavLink>
            ))}
          </nav>
        )}
        <div className="barra__usuario">
          <div className="barra__identidad">
            <strong>{usuario?.nombreCompleto}</strong>
            <span>
              {usuario?.rol === 'ADMIN' ? 'Usuario admin' : NOMBRE_ROL[usuario?.rol]}
              {usuario?.municipio ? ` en ${usuario.municipio}` : ''}
            </span>
          </div>
          <Boton variante="secundario" pequeno onClick={salir}>
            <Icono nombre="salir" tamano={16} />
            Salir
          </Boton>
        </div>
      </header>
      <main className="app__contenido">{children}</main>
    </div>
  )
}
