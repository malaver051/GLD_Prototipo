import { Link } from 'react-router-dom'

export default function Marca({ destino = '/ingresar' }) {
  return (
    <Link to={destino} className="marca" aria-label="GLD, Gobiernos Locales Digitales">
      <span className="marca__sello" aria-hidden="true">GLD</span>
      <span className="marca__nombre">
        Gobiernos Locales
        <br />
        Digitales
      </span>
    </Link>
  )
}
