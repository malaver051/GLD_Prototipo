import InicioUsuario from './InicioUsuario'

export default function ConsolaFuncionario() {
  return (
    <InicioUsuario
      seccion="Consola municipal"
      saludo={(municipio) =>
        `Aquí gestionarás las solicitudes ciudadanas${municipio ? ` de ${municipio}` : ''}: revisarlas, pedir información y emitir los documentos.`
      }
      vacioTitulo="Tu bandeja está vacía"
      vacioTexto="Las solicitudes que te asignen aparecerán aquí, ordenadas por fecha de vencimiento."
    />
  )
}
