import Marca from './Marca'

// Claves geoestadísticas INEGI de los municipios precargados: textura del panel.
const MUNICIPIOS = [
  ['01001', 'Aguascalientes'], ['02002', 'Mexicali'], ['02004', 'Tijuana'], ['03003', 'La Paz'],
  ['04002', 'Campeche'], ['05030', 'Saltillo'], ['05035', 'Torreón'], ['06002', 'Colima'],
  ['07101', 'Tuxtla Gutiérrez'], ['08019', 'Chihuahua'], ['08037', 'Juárez'], ['09007', 'Iztapalapa'],
  ['09015', 'Cuauhtémoc'], ['10005', 'Durango'], ['11015', 'Guanajuato'], ['11020', 'León'],
  ['12001', 'Acapulco de Juárez'], ['12029', 'Chilpancingo'], ['13048', 'Pachuca de Soto'], ['14039', 'Guadalajara'],
  ['14120', 'Zapopan'], ['15033', 'Ecatepec'], ['15058', 'Nezahualcóyotl'], ['15106', 'Toluca'],
  ['16053', 'Morelia'], ['17007', 'Cuernavaca'], ['18017', 'Tepic'], ['19039', 'Monterrey'],
  ['20067', 'Oaxaca de Juárez'], ['21114', 'Puebla'], ['22014', 'Querétaro'], ['23004', 'Othón P. Blanco'],
  ['23005', 'Benito Juárez'], ['24028', 'San Luis Potosí'], ['25006', 'Culiacán'], ['26030', 'Hermosillo'],
  ['27004', 'Centro'], ['28041', 'Victoria'], ['29033', 'Tlaxcala'], ['30087', 'Xalapa'],
  ['30193', 'Veracruz'], ['31050', 'Mérida'], ['32056', 'Zacatecas'],
]

// Plantilla de las pantallas de acceso: panel institucional + formulario.
export default function DisenoAcceso({ children, ancho = false }) {
  return (
    <div className="acceso">
      <aside className="acceso__panel">
        <Marca />
        <div className="acceso__lema">
          <h2>La ventanilla de tu municipio, abierta todo el día.</h2>
          <p>Solicita trámites, paga y sigue tus solicitudes sin hacer fila en la presidencia municipal.</p>
        </div>
        <ul className="acceso__claves" aria-hidden="true">
          {MUNICIPIOS.map(([clave, nombre]) => (
            <li key={clave}>
              <span>{clave}</span>
              <span>{nombre}</span>
            </li>
          ))}
        </ul>
      </aside>
      <main className="acceso__contenido">
        <div className={`acceso__formulario${ancho ? ' acceso__formulario--ancho' : ''}`}>{children}</div>
      </main>
    </div>
  )
}
