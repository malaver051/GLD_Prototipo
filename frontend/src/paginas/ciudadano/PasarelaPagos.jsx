import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import Campo from '../../componentes/Campo'
import Icono from '../../componentes/Icono'
import { useSesion } from '../../contexto/SesionContext'
import { consultarOrdenPago, registrarResultadoPago } from '../../servicios/tramitesService'
import { fechaCorta, moneda } from '../../utilidades/formato'
import { useErrorSesion } from '../../utilidades/useErrorSesion'

// Bancos ficticios: la pasarela es una simulación para el prototipo.
const BANCOS = ['Banco del Valle', 'Banca Pacífico', 'Banco Azul', 'Caja Popular Centro']

/**
 * Pasarela de pagos SIMULADA (BPMN: participante externo «Pasarela de pagos»).
 * En producción esta pantalla sería del proveedor de pagos y el resultado llegaría por webhook.
 */
export default function PasarelaPagos() {
  const [parametros] = useSearchParams()
  const referencia = parametros.get('referencia') || ''
  const { sesion } = useSesion()
  const atenderSesion = useErrorSesion()
  const [orden, setOrden] = useState(null)
  const [banco, setBanco] = useState('')
  const [tipoPersona, setTipoPersona] = useState('FISICA')
  const [errorBanco, setErrorBanco] = useState(null)
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(null)

  useEffect(() => {
    consultarOrdenPago(sesion.token, referencia)
      .then(setOrden)
      .catch((e) => {
        if (!atenderSesion(e)) setError(e)
      })
  }, [sesion.token, referencia, atenderSesion])

  async function responder(aprobado) {
    if (!banco) {
      setErrorBanco('Elige tu banco.')
      return
    }
    setEnviando(aprobado ? 'aprobar' : 'rechazar')
    setError(null)
    try {
      setOrden(await registrarResultadoPago(sesion.token, referencia, aprobado, banco))
    } catch (e) {
      if (!atenderSesion(e)) setError(e)
    } finally {
      setEnviando(null)
    }
  }

  const procesada = orden && orden.estado !== 'PENDIENTE'

  return (
    <div className="pasarela">
      <header className="pasarela__barra">
        <span className="pasarela__marca">
          <Icono nombre="candado" tamano={18} /> Pago en línea
        </span>
        <span className="pasarela__simulacion">Simulación para el prototipo · no se realiza ningún cargo</span>
      </header>

      <main className="pasarela__contenido">
        {!orden ? (
          error ? <Aviso tono="error">{error.message}</Aviso> : <p className="texto-suave">Cargando la orden de pago…</p>
        ) : (
          <div className="pasarela__tarjeta">
            <section className="pasarela__orden">
              <span className="texto-suave">Pagas a</span>
              <strong>H. Ayuntamiento de {orden.municipio}</strong>
              <p className="pasarela__total">{moneda(orden.valor)}</p>
              <dl className="datos datos--compactos">
                <div>
                  <dt>Concepto</dt>
                  <dd>{orden.tramite}</dd>
                </div>
                <div>
                  <dt>Radicado</dt>
                  <dd>{orden.numeroRadicado}</dd>
                </div>
                <div>
                  <dt>Referencia</dt>
                  <dd>{orden.referencia}</dd>
                </div>
                <div>
                  <dt>Generada</dt>
                  <dd>{fechaCorta(orden.fechaCreacion)}</dd>
                </div>
              </dl>
            </section>

            <section className="pasarela__formulario">
              {procesada ? (
                <div className="pila">
                  {orden.estado === 'APROBADO' ? (
                    <Aviso tono="exito">
                      <strong>Pago aprobado.</strong> Tu solicitud pasó a revisión. Te enviamos el comprobante por correo.
                    </Aviso>
                  ) : orden.estado === 'RECHAZADO' ? (
                    <Aviso tono="error">
                      <strong>El banco no confirmó el pago.</strong> No se realizó ningún cargo. Puedes intentarlo de nuevo desde tu
                      solicitud.
                    </Aviso>
                  ) : (
                    <Aviso tono="atencion">Esta orden fue reemplazada por una más reciente.</Aviso>
                  )}
                  <Link to={`/portal/solicitudes/${orden.idSolicitud}`} className="boton boton--primario boton--bloque">
                    Volver a mi solicitud
                  </Link>
                </div>
              ) : (
                <div className="pila">
                  <h1 className="pasarela__titulo">Paga con transferencia bancaria</h1>
                  {error && <Aviso tono="error">{error.message}</Aviso>}
                  <Campo etiqueta="Tipo de persona">
                    <select className="control" value={tipoPersona} onChange={(e) => setTipoPersona(e.target.value)}>
                      <option value="FISICA">Persona física</option>
                      <option value="MORAL">Persona moral</option>
                    </select>
                  </Campo>
                  <Campo etiqueta="Banco" error={errorBanco}>
                    <select
                      className="control"
                      value={banco}
                      onChange={(e) => {
                        setBanco(e.target.value)
                        setErrorBanco(null)
                      }}
                    >
                      <option value="">Elige tu banco</option>
                      {BANCOS.map((b) => (
                        <option key={b} value={b}>
                          {b}
                        </option>
                      ))}
                    </select>
                  </Campo>
                  <Boton bloque onClick={() => responder(true)} cargando={enviando === 'aprobar'} disabled={Boolean(enviando)}>
                    Autorizar pago de {moneda(orden.valor)}
                  </Boton>
                  <div className="pasarela__prueba">
                    <Boton variante="texto" onClick={() => responder(false)} disabled={Boolean(enviando)}>
                      Simular pago rechazado
                    </Boton>
                  </div>
                  <Link to={`/portal/solicitudes/${orden.idSolicitud}`} className="pasarela__cancelar">
                    Cancelar y volver
                  </Link>
                </div>
              )}
            </section>
          </div>
        )}
      </main>
    </div>
  )
}
