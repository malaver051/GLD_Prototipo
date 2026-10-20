import InicioUsuario from './InicioUsuario'

export default function PortalCiudadano() {
  return (
    <InicioUsuario
      seccion="Portal ciudadano"
      saludo={(municipio) =>
        `Desde aquí podrás solicitar trámites${municipio ? ` de ${municipio}` : ''}, pagarlos en línea y seguir su estado.`
      }
      vacioTitulo="Aún no tienes solicitudes"
      vacioTexto="Cuando solicites un trámite, aquí verás su número de radicado y en qué etapa va."
    />
  )
}
