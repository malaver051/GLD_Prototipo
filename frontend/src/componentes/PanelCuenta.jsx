import { useEffect, useState } from 'react'
import { useSesion } from '../contexto/SesionContext'
import { obtenerPerfil } from '../servicios/identidadService'
import { fechaCorta, telefonoLegible } from '../utilidades/formato'
import { NOMBRE_DOCUMENTO } from '../utilidades/rutas'
import { useErrorSesion } from '../utilidades/useErrorSesion'

// Datos de la cuenta del usuario en sesión (ms-identidad).
export default function PanelCuenta() {
  const { sesion } = useSesion()
  const atenderSesion = useErrorSesion()
  const [perfil, setPerfil] = useState(null)

  useEffect(() => {
    obtenerPerfil(sesion.token)
      .then(setPerfil)
      .catch((e) => atenderSesion(e))
  }, [sesion.token, atenderSesion])

  return (
    <section className="panel" aria-labelledby="titulo-cuenta">
      <div className="panel__cabecera">
        <h2 id="titulo-cuenta" className="panel__titulo">
          Tu cuenta
        </h2>
      </div>
      <dl className="datos">
        <div>
          <dt>Correo</dt>
          <dd>{perfil?.correo ?? '…'}</dd>
        </div>
        <div>
          <dt>Documento</dt>
          <dd>{perfil ? `${NOMBRE_DOCUMENTO[perfil.tipoDocumento]} ${perfil.numeroDocumento}` : '…'}</dd>
        </div>
        <div>
          <dt>Teléfono</dt>
          <dd>{perfil ? `+52 ${telefonoLegible(perfil.telefono)}` : '…'}</dd>
        </div>
        <div>
          <dt>Municipio</dt>
          <dd>{perfil?.municipio ? `${perfil.municipio.nombre}, ${perfil.municipio.estado}` : '…'}</dd>
        </div>
        <div>
          <dt>Último acceso</dt>
          <dd>{perfil ? fechaCorta(perfil.ultimoAcceso) : '…'}</dd>
        </div>
      </dl>
    </section>
  )
}
