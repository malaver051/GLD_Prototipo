import { useEffect, useState } from 'react'
import Aviso from '../../componentes/Aviso'
import Boton from '../../componentes/Boton'
import Campo from '../../componentes/Campo'
import CampoTelefono from '../../componentes/CampoTelefono'
import Icono from '../../componentes/Icono'
import { crearFuncionario } from '../../servicios/identidadService'
import { agruparPorEstado } from '../../utilidades/formato'

const INICIAL = {
  nombreCompleto: '',
  tipoDocumento: 'CURP',
  numeroDocumento: '',
  correo: '',
  telefono: '',
  idMunicipio: '',
  idRol: '',
}

const NOMBRE_ROL_ASIGNABLE = { FUNCIONARIO: 'Funcionario municipal' }

function validar(d) {
  const errores = {}
  if (!d.nombreCompleto.trim()) errores.nombreCompleto = 'Escribe el nombre completo.'
  if (!d.numeroDocumento.trim()) errores.numeroDocumento = 'Escribe el número de documento.'
  if (!/^[^@\s]+@gobierno\.com$/i.test(d.correo.trim())) errores.correo = 'El correo debe terminar en @gobierno.com.'
  if (!/^\d{10}$/.test(d.telefono)) errores.telefono = 'El teléfono debe tener 10 dígitos.'
  if (!d.idMunicipio) errores.idMunicipio = 'Elige el municipio.'
  if (!d.idRol) errores.idRol = 'Elige el rol.'
  return errores
}

// Cajón lateral para que el administrador cree la cuenta de un funcionario.
export default function FormularioFuncionario({ token, municipios, roles, alCerrar, alCrear, alErrorSesion }) {
  const [datos, setDatos] = useState(INICIAL)
  const [errores, setErrores] = useState({})
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  // Si solo hay un rol asignable, queda elegido
  useEffect(() => {
    if (roles.length === 1) setDatos((d) => ({ ...d, idRol: String(roles[0].id) }))
  }, [roles])

  useEffect(() => {
    const alPresionar = (e) => e.key === 'Escape' && !enviando && alCerrar()
    window.addEventListener('keydown', alPresionar)
    return () => window.removeEventListener('keydown', alPresionar)
  }, [alCerrar, enviando])

  const cambiar = (campo, valor) => {
    setDatos((d) => ({ ...d, [campo]: valor }))
    setErrores((er) => ({ ...er, [campo]: undefined }))
  }

  async function guardar(evento) {
    evento.preventDefault()
    const encontrados = validar(datos)
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) return

    setEnviando(true)
    setError(null)
    try {
      const creado = await crearFuncionario(token, {
        ...datos,
        nombreCompleto: datos.nombreCompleto.trim(),
        correo: datos.correo.trim().toLowerCase(),
        idMunicipio: Number(datos.idMunicipio),
        idRol: Number(datos.idRol),
      })
      alCrear(creado)
    } catch (e) {
      if (!alErrorSesion(e)) setError(e)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <>
      <div className="cajon-fondo" onClick={() => !enviando && alCerrar()} />
      <aside className="cajon" role="dialog" aria-modal="true" aria-labelledby="titulo-cajon">
        <form className="cajon__formulario" onSubmit={guardar} noValidate>
          <div className="cajon__cabecera">
            <div>
              <h2 id="titulo-cajon">Crear funcionario</h2>
              <p>El sistema genera una contraseña temporal y la envía al correo institucional.</p>
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

            <Campo etiqueta="Nombre completo" error={errores.nombreCompleto}>
              <input className="control" autoFocus value={datos.nombreCompleto} onChange={(e) => cambiar('nombreCompleto', e.target.value)} />
            </Campo>

            <div className="fila-dos">
              <Campo etiqueta="Documento">
                <select className="control" value={datos.tipoDocumento} onChange={(e) => cambiar('tipoDocumento', e.target.value)}>
                  <option value="CURP">CURP</option>
                  <option value="INE">Credencial INE</option>
                  <option value="PASAPORTE">Pasaporte</option>
                </select>
              </Campo>
              <Campo etiqueta="Número de documento" error={errores.numeroDocumento}>
                <input
                  className="control"
                  spellCheck="false"
                  value={datos.numeroDocumento}
                  onChange={(e) => cambiar('numeroDocumento', e.target.value.toUpperCase())}
                />
              </Campo>
            </div>

            <Campo etiqueta="Correo institucional" ayuda="Debe terminar en @gobierno.com. Será su usuario para ingresar." error={errores.correo}>
              <input
                className="control"
                type="email"
                autoCapitalize="none"
                placeholder="nombre.apellido@gobierno.com"
                value={datos.correo}
                onChange={(e) => cambiar('correo', e.target.value)}
              />
            </Campo>

            <Campo etiqueta="Teléfono" error={errores.telefono}>
              <CampoTelefono valor={datos.telefono} alCambiar={(v) => cambiar('telefono', v)} />
            </Campo>

            <Campo etiqueta="Municipio" error={errores.idMunicipio}>
              <select className="control" value={datos.idMunicipio} onChange={(e) => cambiar('idMunicipio', e.target.value)}>
                <option value="">Elige el municipio</option>
                {agruparPorEstado(municipios).map(([estado, lista]) => (
                  <optgroup key={estado} label={estado}>
                    {lista.map((m) => (
                      <option key={m.id} value={m.id}>
                        {m.nombre}
                      </option>
                    ))}
                  </optgroup>
                ))}
              </select>
            </Campo>

            <Campo etiqueta="Rol" error={errores.idRol}>
              <select className="control" value={datos.idRol} onChange={(e) => cambiar('idRol', e.target.value)}>
                <option value="">Elige el rol</option>
                {roles.map((r) => (
                  <option key={r.id} value={r.id}>
                    {NOMBRE_ROL_ASIGNABLE[r.nombre] || r.nombre}
                  </option>
                ))}
              </select>
            </Campo>
          </div>

          <div className="cajon__pie">
            <Boton variante="secundario" onClick={alCerrar} disabled={enviando}>
              Cancelar
            </Boton>
            <Boton type="submit" cargando={enviando}>
              Crear funcionario
            </Boton>
          </div>
        </form>
      </aside>
    </>
  )
}
