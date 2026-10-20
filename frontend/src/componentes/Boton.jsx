export default function Boton({ variante = 'primario', bloque = false, pequeno = false, cargando = false, children, ...resto }) {
  const clases = ['boton', `boton--${variante}`, bloque && 'boton--bloque', pequeno && 'boton--pequeno']
    .filter(Boolean)
    .join(' ')
  return (
    <button type="button" className={clases} disabled={cargando || resto.disabled} aria-busy={cargando || undefined} {...resto}>
      {cargando && <span className="boton__giro" aria-hidden="true" />}
      {children}
    </button>
  )
}
