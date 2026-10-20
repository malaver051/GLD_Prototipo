import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import Campo from '../../componentes/Campo'
import DisenoApp from '../../componentes/DisenoApp'
import { CATEGORIAS } from '../../componentes/EstadoIncidencia'
import Icono from '../../componentes/Icono'
import Mapa from '../../componentes/Mapa'
import { useSesion } from '../../contexto/SesionContext'
import { consultarMapaMunicipio, reportarIncidencia } from '../../servicios/comunidadService'
import { tamanoArchivo } from '../../utilidades/formato'
import { NAV_CIUDADANO } from '../../utilidades/navegacion'
import { useErrorSesion } from '../../utilidades/useErrorSesion'

const TIPOS_FOTO = ['image/jpeg', 'image/png']
const TAMANO_MAXIMO = 5 * 1024 * 1024

function validarFoto(archivo) {
  if (!TIPOS_FOTO.includes(archivo.type)) return 'La foto debe ser JPG o PNG.'
  if (archivo.size > TAMANO_MAXIMO) return 'La foto supera los 5 MB.'
  return null
}

// HU-08 · El ciudadano reporta una incidencia con categoría, ubicación en el mapa y foto opcional.
export default function ReportarIncidencia() {
  const { sesion } = useSesion()
  const navegar = useNavigate()
  const atenderSesion = useErrorSesion()
  const entradaFoto = useRef(null)
  const [mapa, setMapa] = useState(null)
  const [datos, setDatos] = useState({ categoria: '', descripcion: '', referencia: '' })
  const [punto, setPunto] = useState(null)
  const [enfocar, setEnfocar] = useState(null)
  const [foto, setFoto] = useState(null)
  const [vistaFoto, setVistaFoto] = useState(null)
  const [errores, setErrores] = useState({})
  const [error, setError] = useState(null)
  const [ubicando, setUbicando] = useState(false)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    consultarMapaMunicipio(sesion.token)
      .then(setMapa)
      .catch((e) => {
        if (!atenderSesion(e)) setError(e)
      })
  }, [sesion.token, atenderSesion])

  // Vista previa de la foto elegida
  useEffect(() => {
    if (!foto) {
      setVistaFoto(null)
      return undefined
    }
    const url = URL.createObjectURL(foto)
    setVistaFoto(url)
    return () => URL.revokeObjectURL(url)
  }, [foto])

  const cambiar = (campo, valor) => {
    setDatos((d) => ({ ...d, [campo]: valor }))
    setErrores((e) => ({ ...e, [campo]: undefined }))
  }

  function elegirPunto(lat, lng) {
    setPunto([lat, lng])
    setErrores((e) => ({ ...e, ubicacion: undefined }))
  }

  function usarMiUbicacion() {
    if (!navigator.geolocation) {
      setErrores((e) => ({ ...e, ubicacion: 'Tu navegador no permite obtener la ubicación. Márcala en el mapa.' }))
      return
    }
    setUbicando(true)
    navigator.geolocation.getCurrentPosition(
      (posicion) => {
        const lat = Math.round(posicion.coords.latitude * 1e6) / 1e6
        const lng = Math.round(posicion.coords.longitude * 1e6) / 1e6
        elegirPunto(lat, lng)
        setEnfocar({ lat, lng })
        setUbicando(false)
      },
      () => {
        setErrores((e) => ({ ...e, ubicacion: 'No pudimos obtener tu ubicación. Márcala tocando el mapa.' }))
        setUbicando(false)
      },
      { enableHighAccuracy: true, timeout: 10000 },
    )
  }

  function elegirFoto(archivo) {
    const problema = validarFoto(archivo)
    setErrores((e) => ({ ...e, foto: problema }))
    if (!problema) setFoto(archivo)
  }

  async function enviar(evento) {
    evento.preventDefault()
    const encontrados = {}
    if (!datos.categoria) encontrados.categoria = 'Elige la categoría del problema.'
    if (!datos.descripcion.trim()) encontrados.descripcion = 'Describe el problema.'
    if (!punto) encontrados.ubicacion = 'Marca en el mapa dónde está el problema.'
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) {
      setError({ message: 'Revisa los datos del reporte.' })
      return
    }
    setEnviando(true)
    setError(null)
    try {
      const incidencia = await reportarIncidencia(sesion.token, {
        ...datos,
        latitud: punto[0],
        longitud: punto[1],
        foto,
      })
      navegar(`/portal/incidencias/${incidencia.id}`, { replace: true, state: { recienReportada: true } })
    } catch (e) {
      if (!atenderSesion(e)) setError(e)
    } finally {
      setEnviando(false)
    }
  }

  const categoria = CATEGORIAS.find((c) => c.valor === datos.categoria)

  return (
    <DisenoApp seccion="Portal ciudadano" navegacion={NAV_CIUDADANO}>
      <Link to="/portal/incidencias" className="volver">
        <Icono nombre="atras" tamano={16} /> Mis incidencias
      </Link>

      <form className="detalle" onSubmit={enviar} noValidate>
        <div className="detalle__principal">
          <header className="encabezado-pagina encabezado-pagina--compacto">
            <div>
              <h1>Reportar una incidencia</h1>
              <p>Cuéntale a {mapa?.municipio || 'tu municipio'} qué problema encontraste y dónde está.</p>
            </div>
          </header>

          {error && (
            <div className="acceso__mensaje">
              <Aviso tono="error" errores={error.errores}>
                {error.message}
              </Aviso>
            </div>
          )}

          <section className="panel">
            <div className="panel__cabecera">
              <h2 className="panel__titulo">¿Qué pasa?</h2>
            </div>
            <div className="panel__cuerpo pila">
              <fieldset className="categorias" aria-describedby={errores.categoria ? 'error-categoria' : undefined}>
                <legend className="campo__etiqueta">Categoría</legend>
                <div className="categorias__opciones">
                  {CATEGORIAS.map((c) => (
                    <label key={c.valor} className="categoria" data-elegida={datos.categoria === c.valor}>
                      <input
                        type="radio"
                        name="categoria"
                        value={c.valor}
                        checked={datos.categoria === c.valor}
                        onChange={() => cambiar('categoria', c.valor)}
                      />
                      <span className="categoria__punto" data-categoria={c.valor} aria-hidden="true" />
                      <strong>{c.texto}</strong>
                      <span>{c.ayuda}</span>
                    </label>
                  ))}
                </div>
                {errores.categoria && (
                  <p className="campo__error" id="error-categoria">
                    {errores.categoria}
                  </p>
                )}
              </fieldset>
              <Campo etiqueta="Descripción" ayuda="Qué viste, desde cuándo y si representa un riesgo." error={errores.descripcion}>
                <textarea
                  className="control control--area"
                  rows={3}
                  maxLength={1000}
                  value={datos.descripcion}
                  onChange={(e) => cambiar('descripcion', e.target.value)}
                />
              </Campo>
            </div>
          </section>

          <section className="panel separado" data-error={Boolean(errores.ubicacion)}>
            <div className="panel__cabecera">
              <h2 className="panel__titulo">¿Dónde está?</h2>
              <Boton variante="secundario" pequeno onClick={usarMiUbicacion} cargando={ubicando}>
                <Icono nombre="ubicacion" tamano={16} />
                Usar mi ubicación
              </Boton>
            </div>
            <div className="panel__cuerpo pila">
              <p className="texto-suave">Toca el mapa en el lugar exacto del problema. Puedes acercar el mapa con los botones + y −.</p>
              {mapa ? (
                <Mapa
                  centro={[mapa.latitud, mapa.longitud]}
                  zoom={mapa.zoom}
                  punto={punto}
                  alElegirPunto={elegirPunto}
                  enfocar={enfocar}
                  altura={340}
                  etiqueta="Mapa para marcar la ubicación de la incidencia"
                />
              ) : (
                <div className="mapa mapa--cargando" style={{ height: 340 }}>
                  Cargando mapa…
                </div>
              )}
              {errores.ubicacion ? (
                <p className="campo__error">{errores.ubicacion}</p>
              ) : (
                punto && (
                  <p className="coordenadas">
                    <Icono nombre="marca" tamano={14} /> Ubicación marcada: {punto[0].toFixed(5)}, {punto[1].toFixed(5)}
                  </p>
                )
              )}
              <Campo etiqueta="Referencia (opcional)" ayuda="Por ejemplo: frente a la escuela primaria, esquina con Juárez.">
                <input
                  className="control"
                  maxLength={200}
                  value={datos.referencia}
                  onChange={(e) => cambiar('referencia', e.target.value)}
                />
              </Campo>
            </div>
          </section>

          <section className="panel separado">
            <div className="panel__cabecera">
              <h2 className="panel__titulo">
                Foto <span className="texto-suave">(opcional)</span>
              </h2>
            </div>
            <div className="panel__cuerpo">
              <div className="foto-elegida">
                {vistaFoto ? (
                  <img src={vistaFoto} alt="Vista previa de la foto adjunta" />
                ) : (
                  <div className="foto-elegida__vacia" aria-hidden="true">
                    <Icono nombre="camara" tamano={28} />
                  </div>
                )}
                <div className="pila pila--compacta">
                  {foto ? (
                    <span>
                      {foto.name} · {tamanoArchivo(foto.size)}
                    </span>
                  ) : (
                    <span className="texto-suave">Una foto ayuda a la dependencia a ubicar y dimensionar el problema. JPG o PNG, máximo 5 MB.</span>
                  )}
                  {errores.foto && <p className="campo__error">{errores.foto}</p>}
                  <div className="fila-botones">
                    <input
                      ref={entradaFoto}
                      type="file"
                      accept=".jpg,.jpeg,.png,image/jpeg,image/png"
                      className="visualmente-oculto"
                      aria-label="Elegir foto"
                      onChange={(e) => {
                        const elegido = e.target.files?.[0]
                        if (elegido) elegirFoto(elegido)
                        e.target.value = ''
                      }}
                    />
                    <Boton variante="secundario" pequeno onClick={() => entradaFoto.current?.click()}>
                      {foto ? 'Cambiar foto' : 'Adjuntar foto'}
                    </Boton>
                    {foto && (
                      <Boton variante="texto" onClick={() => setFoto(null)}>
                        Quitar
                      </Boton>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>

        <aside className="detalle__lateral">
          <section className="panel resumen">
            <dl className="datos">
              <div>
                <dt>Categoría</dt>
                <dd>{categoria ? categoria.texto : <span className="texto-suave">Sin elegir</span>}</dd>
              </div>
              <div>
                <dt>Ubicación</dt>
                <dd>{punto ? 'Marcada en el mapa' : <span className="texto-suave">Sin marcar</span>}</dd>
              </div>
              <div>
                <dt>Foto</dt>
                <dd>{foto ? 'Adjunta' : <span className="texto-suave">Sin foto</span>}</dd>
              </div>
            </dl>
            <div className="resumen__pie">
              <p className="texto-suave">
                Recibirás un número de seguimiento. El municipio asignará el reporte a la dependencia que corresponda.
              </p>
              <Boton type="submit" bloque cargando={enviando}>
                Enviar reporte
              </Boton>
            </div>
          </section>
        </aside>
      </form>
    </DisenoApp>
  )
}
