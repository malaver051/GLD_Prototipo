import { useEffect, useState } from 'react'
import Aviso from '../../componentes/Aviso'
import DisenoApp from '../../componentes/DisenoApp'
import { useSesion } from '../../contexto/SesionContext'
import { obtenerPerfil } from '../../servicios/identidadService'
import { fechaCorta, telefonoLegible } from '../../utilidades/formato'
import { NOMBRE_DOCUMENTO } from '../../utilidades/rutas'
import { useErrorSesion } from '../../utilidades/useErrorSesion'

// Pantalla de inicio compartida por el portal ciudadano y la consola del funcionario.
export default function InicioUsuario({ seccion, saludo, vacioTitulo, vacioTexto }) {
  const { sesion } = useSesion()
  const atenderSesion = useErrorSesion()
  const [perfil, setPerfil] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    obtenerPerfil(sesion.token)
      .then(setPerfil)
      .catch((e) => {
        if (!atenderSesion(e)) setError(e)
      })
  }, [sesion.token, atenderSesion])

  const primerNombre = (sesion.usuario?.nombreCompleto || '').split(' ')[0]

  return (
    <DisenoApp seccion={seccion}>
      {error && <Aviso tono="error">{error.message}</Aviso>}
      <div className="bienvenida">
        <section className="bienvenida__saludo">
          <h1>Hola, {primerNombre}.</h1>
          <p>{saludo(perfil?.municipio?.nombre || sesion.usuario?.municipio)}</p>
        </section>

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
      </div>

      <section className="panel separado">
        <div className="vacio">
          <strong>{vacioTitulo}</strong>
          <span>{vacioTexto}</span>
        </div>
      </section>
    </DisenoApp>
  )
}
