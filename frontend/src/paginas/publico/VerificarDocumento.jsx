import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import Campo from '../../componentes/Campo'
import DisenoAcceso from '../../componentes/DisenoAcceso'
import Icono from '../../componentes/Icono'
import { verificarDocumento } from '../../servicios/tramitesService'
import { fechaCorta } from '../../utilidades/formato'

// Página pública: cualquiera puede comprobar que un documento oficial es auténtico.
export default function VerificarDocumento() {
  const [parametros] = useSearchParams()
  const [codigo, setCodigo] = useState(parametros.get('codigo') || '')
  const [resultado, setResultado] = useState(null)
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  async function verificar(valor = codigo) {
    if (!valor.trim()) return
    setEnviando(true)
    setError(null)
    try {
      setResultado(await verificarDocumento(valor.trim().toUpperCase()))
    } catch (e) {
      setError(e)
    } finally {
      setEnviando(false)
    }
  }

  useEffect(() => {
    if (parametros.get('codigo')) verificar(parametros.get('codigo'))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <DisenoAcceso>
      <Link to="/ingresar" className="volver">
        <Icono nombre="atras" tamano={16} /> Ir al ingreso
      </Link>
      <header className="acceso__encabezado">
        <h1>Verifica un documento oficial</h1>
        <p>Escribe el código que aparece en la parte inferior del documento.</p>
      </header>
      <form
        className="pila"
        onSubmit={(e) => {
          e.preventDefault()
          verificar()
        }}
        noValidate
      >
        {error && <Aviso tono="error">{error.message}</Aviso>}
        <Campo etiqueta="Código de verificación">
          <input
            className="control"
            placeholder="GLD-XXXX-XXXX"
            autoCapitalize="characters"
            spellCheck="false"
            value={codigo}
            onChange={(e) => {
              setCodigo(e.target.value.toUpperCase())
              setResultado(null)
            }}
          />
        </Campo>
        <Boton type="submit" bloque cargando={enviando}>
          Verificar
        </Boton>
        {resultado &&
          (resultado.valido ? (
            <div className="verificacion">
              <Aviso tono="exito">
                <strong>Documento auténtico.</strong> Fue emitido por la plataforma GLD.
              </Aviso>
              <dl className="datos">
                <div>
                  <dt>Trámite</dt>
                  <dd>{resultado.tramite}</dd>
                </div>
                <div>
                  <dt>Radicado</dt>
                  <dd>{resultado.numeroRadicado}</dd>
                </div>
                <div>
                  <dt>Titular</dt>
                  <dd>{resultado.titular}</dd>
                </div>
                <div>
                  <dt>Emitido por</dt>
                  <dd>
                    H. Ayuntamiento de {resultado.municipio}, {resultado.estado}
                  </dd>
                </div>
                <div>
                  <dt>Fecha</dt>
                  <dd>{fechaCorta(resultado.fechaEmision)}</dd>
                </div>
              </dl>
            </div>
          ) : (
            <Aviso tono="error">No encontramos un documento con ese código. Revisa que esté bien escrito.</Aviso>
          ))}
      </form>
    </DisenoAcceso>
  )
}
