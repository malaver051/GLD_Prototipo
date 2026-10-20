import { useEffect, useState } from 'react'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import Campo from '../../componentes/Campo'
import Icono from '../../componentes/Icono'
import { useSesion } from '../../contexto/SesionContext'
import { actualizarTipoTramite, crearTipoTramite } from '../../servicios/tramitesService'

const REQUISITO_VACIO = { id: null, nombre: '', descripcion: '', obligatorio: true }

function inicial(tramite) {
  if (!tramite) {
    return { nombre: '', descripcion: '', dependencia: '', costo: '0', plazoDias: '5', requisitos: [{ ...REQUISITO_VACIO, clave: 1 }] }
  }
  return {
    nombre: tramite.nombre,
    descripcion: tramite.descripcion,
    dependencia: tramite.dependencia,
    costo: String(tramite.costo),
    plazoDias: String(tramite.plazoDias),
    requisitos: tramite.requisitos.map((r) => ({ ...r, clave: r.id })),
  }
}

function validar(d) {
  const e = {}
  if (!d.nombre.trim()) e.nombre = 'Escribe el nombre del trámite.'
  if (!d.descripcion.trim()) e.descripcion = 'Escribe una descripción para el ciudadano.'
  if (!d.dependencia.trim()) e.dependencia = 'Escribe la dependencia que lo atiende.'
  if (!/^\d+(\.\d{1,2})?$/.test(d.costo)) e.costo = 'Escribe el costo en pesos, por ejemplo 226.00 (0 si es gratuito).'
  const plazo = Number(d.plazoDias)
  if (!Number.isInteger(plazo) || plazo < 1 || plazo > 90) e.plazoDias = 'El plazo debe estar entre 1 y 90 días hábiles.'
  if (d.requisitos.length === 0) e.requisitos = 'Agrega al menos un requisito.'
  else if (d.requisitos.some((r) => !r.nombre.trim())) e.requisitos = 'Cada requisito necesita un nombre.'
  return e
}

// Cajón para agregar o editar un trámite del catálogo.
export default function FormularioTipoTramite({ tramite, alCerrar, alGuardar, alErrorSesion }) {
  const { sesion } = useSesion()
  const [datos, setDatos] = useState(() => inicial(tramite))
  const [errores, setErrores] = useState({})
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const [siguienteClave, setSiguienteClave] = useState(1000)

  useEffect(() => {
    const alPresionar = (e) => e.key === 'Escape' && !enviando && alCerrar()
    window.addEventListener('keydown', alPresionar)
    return () => window.removeEventListener('keydown', alPresionar)
  }, [alCerrar, enviando])

  const cambiar = (campo, valor) => {
    setDatos((d) => ({ ...d, [campo]: valor }))
    setErrores((e) => ({ ...e, [campo]: undefined }))
  }

  const cambiarRequisito = (clave, campo, valor) => {
    setDatos((d) => ({ ...d, requisitos: d.requisitos.map((r) => (r.clave === clave ? { ...r, [campo]: valor } : r)) }))
    setErrores((e) => ({ ...e, requisitos: undefined }))
  }

  const moverRequisito = (indice, paso) => {
    setDatos((d) => {
      const lista = [...d.requisitos]
      const destino = indice + paso
      if (destino < 0 || destino >= lista.length) return d
      ;[lista[indice], lista[destino]] = [lista[destino], lista[indice]]
      return { ...d, requisitos: lista }
    })
  }

  async function guardar(evento) {
    evento.preventDefault()
    const encontrados = validar(datos)
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) return

    const cuerpo = {
      nombre: datos.nombre.trim(),
      descripcion: datos.descripcion.trim(),
      dependencia: datos.dependencia.trim(),
      costo: Number(datos.costo),
      plazoDias: Number(datos.plazoDias),
      requisitos: datos.requisitos.map((r) => ({
        id: r.id,
        nombre: r.nombre.trim(),
        descripcion: r.descripcion.trim(),
        obligatorio: r.obligatorio,
      })),
    }
    setEnviando(true)
    setError(null)
    try {
      const guardado = tramite
        ? await actualizarTipoTramite(sesion.token, tramite.id, cuerpo)
        : await crearTipoTramite(sesion.token, cuerpo)
      alGuardar(guardado, !tramite)
    } catch (e) {
      if (!alErrorSesion(e)) setError(e)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <>
      <div className="cajon-fondo" onClick={() => !enviando && alCerrar()} />
      <aside className="cajon cajon--ancho" role="dialog" aria-modal="true" aria-labelledby="titulo-tramite">
        <form className="cajon__formulario" onSubmit={guardar} noValidate>
          <div className="cajon__cabecera">
            <div>
              <h2 id="titulo-tramite">{tramite ? 'Editar trámite' : 'Agregar trámite'}</h2>
              <p>Lo que escribas aquí es lo que verá el ciudadano en el catálogo.</p>
            </div>
            <button type="button" className="cerrar" onClick={alCerrar} aria-label="Cerrar" disabled={enviando}>
              <Icono nombre="cerrar" />
            </button>
          </div>

          <div className="cajon__cuerpo pila">
            {error && (
              <Aviso tono="error" errores={error.errores}>
                {error.message}
              </Aviso>
            )}
            <Campo etiqueta="Nombre" error={errores.nombre}>
              <input className="control" value={datos.nombre} maxLength={150} onChange={(e) => cambiar('nombre', e.target.value)} />
            </Campo>
            <Campo etiqueta="Descripción" error={errores.descripcion}>
              <textarea className="control control--area" rows={3} maxLength={600} value={datos.descripcion} onChange={(e) => cambiar('descripcion', e.target.value)} />
            </Campo>
            <Campo etiqueta="Dependencia que lo atiende" error={errores.dependencia}>
              <input className="control" value={datos.dependencia} maxLength={120} onChange={(e) => cambiar('dependencia', e.target.value)} />
            </Campo>
            <div className="fila-dos fila-dos--iguales">
              <Campo etiqueta="Costo (MXN)" ayuda="0 si es gratuito" error={errores.costo}>
                <input className="control" inputMode="decimal" value={datos.costo} onChange={(e) => cambiar('costo', e.target.value.replace(',', '.'))} />
              </Campo>
              <Campo etiqueta="Plazo (días hábiles)" error={errores.plazoDias}>
                <input className="control" inputMode="numeric" value={datos.plazoDias} onChange={(e) => cambiar('plazoDias', e.target.value.replace(/\D/g, ''))} />
              </Campo>
            </div>

            <fieldset className="editor-requisitos">
              <legend className="campo__etiqueta">Requisitos</legend>
              {errores.requisitos && <p className="campo__error">{errores.requisitos}</p>}
              {datos.requisitos.map((r, i) => (
                <div key={r.clave} className="editor-requisitos__fila">
                  <div className="pila pila--compacta">
                    <input
                      className="control"
                      placeholder="Nombre del requisito"
                      aria-label={`Nombre del requisito ${i + 1}`}
                      value={r.nombre}
                      maxLength={150}
                      onChange={(e) => cambiarRequisito(r.clave, 'nombre', e.target.value)}
                    />
                    <input
                      className="control"
                      placeholder="Indicaciones para el ciudadano (opcional)"
                      aria-label={`Indicaciones del requisito ${i + 1}`}
                      value={r.descripcion}
                      maxLength={400}
                      onChange={(e) => cambiarRequisito(r.clave, 'descripcion', e.target.value)}
                    />
                    <label className="casilla">
                      <input type="checkbox" checked={r.obligatorio} onChange={(e) => cambiarRequisito(r.clave, 'obligatorio', e.target.checked)} />
                      <span>Obligatorio para radicar</span>
                    </label>
                  </div>
                  <div className="editor-requisitos__acciones">
                    <button type="button" className="cerrar" aria-label="Subir" disabled={i === 0} onClick={() => moverRequisito(i, -1)}>
                      <Icono nombre="atras" tamano={16} style={{ transform: 'rotate(90deg)' }} />
                    </button>
                    <button type="button" className="cerrar" aria-label="Bajar" disabled={i === datos.requisitos.length - 1} onClick={() => moverRequisito(i, 1)}>
                      <Icono nombre="atras" tamano={16} style={{ transform: 'rotate(-90deg)' }} />
                    </button>
                    <button
                      type="button"
                      className="cerrar"
                      aria-label={`Quitar requisito ${i + 1}`}
                      onClick={() => setDatos((d) => ({ ...d, requisitos: d.requisitos.filter((x) => x.clave !== r.clave) }))}
                    >
                      <Icono nombre="cerrar" tamano={16} />
                    </button>
                  </div>
                </div>
              ))}
              <Boton
                variante="secundario"
                pequeno
                onClick={() => {
                  setDatos((d) => ({ ...d, requisitos: [...d.requisitos, { ...REQUISITO_VACIO, clave: siguienteClave }] }))
                  setSiguienteClave((c) => c + 1)
                }}
              >
                <Icono nombre="mas" tamano={14} />
                Agregar requisito
              </Boton>
            </fieldset>
          </div>

          <div className="cajon__pie">
            <Boton variante="secundario" onClick={alCerrar} disabled={enviando}>
              Cancelar
            </Boton>
            <Boton type="submit" cargando={enviando}>
              {tramite ? 'Guardar cambios' : 'Agregar trámite'}
            </Boton>
          </div>
        </form>
      </aside>
    </>
  )
}
