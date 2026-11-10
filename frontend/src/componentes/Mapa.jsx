import { useEffect, useRef } from 'react'
import L from 'leaflet'

// Las teselas de OpenStreetMap se piden a través del proxy de Vite (/teselas, ver vite.config.js).
// Así salen desde el servidor con un User-Agent propio, como pide la política de uso de OSM,
// y no desde dominios que OSM bloquea, como *.app.github.dev de Codespaces.
const TESELAS = '/teselas/{z}/{x}/{y}.png'
const ATRIBUCION = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'

// Pin del punto elegido (dibujado con CSS: no depende de imágenes de Leaflet)
const iconoPunto = () =>
  L.divIcon({ className: 'mapa__pin', html: '<span></span>', iconSize: [28, 36], iconAnchor: [14, 34] })

/**
 * Mapa de OpenStreetMap con Leaflet.
 * - marcadores: [{ id, lat, lng, color, titulo, activo }] → círculos de color (mapa del funcionario)
 * - punto: [lat, lng] → pin del lugar elegido; alElegirPunto(lat, lng) se llama al tocar el mapa
 * - ajustar: encuadra todos los marcadores la primera vez que llegan
 * - enfocar: { lat, lng } → acerca el mapa a ese punto cada vez que cambia (ubicación del celular,
 *   marcador elegido en la lista)
 */
export default function Mapa({
  centro,
  zoom = 13,
  marcadores = [],
  alSeleccionarMarcador,
  punto,
  alElegirPunto,
  ajustar = false,
  enfocar,
  altura = 360,
  etiqueta = 'Mapa',
}) {
  const contenedor = useRef(null)
  const mapa = useRef(null)
  const capaMarcadores = useRef(null)
  const capaPunto = useRef(null)
  const yaAjustado = useRef(false)
  const alElegir = useRef(alElegirPunto)
  const alSeleccionar = useRef(alSeleccionarMarcador)
  alElegir.current = alElegirPunto
  alSeleccionar.current = alSeleccionarMarcador

  // Crear el mapa una sola vez
  useEffect(() => {
    const m = L.map(contenedor.current, { zoomControl: true, attributionControl: true })
    m.setView(centro || [23.6345, -102.5528], centro ? zoom : 5)
    L.tileLayer(TESELAS, { maxZoom: 19, attribution: ATRIBUCION }).addTo(m)
    capaMarcadores.current = L.layerGroup().addTo(m)
    capaPunto.current = L.layerGroup().addTo(m)
    m.on('click', (e) => alElegir.current?.(redondear(e.latlng.lat), redondear(e.latlng.lng)))
    mapa.current = m
    const temporizador = setTimeout(() => m.invalidateSize(), 0)
    return () => {
      clearTimeout(temporizador)
      m.remove()
      mapa.current = null
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // Centrar cuando llega el centro del municipio
  const latCentro = centro?.[0]
  const lngCentro = centro?.[1]
  useEffect(() => {
    if (mapa.current && latCentro != null && !punto && !(ajustar && marcadores.length > 0)) {
      mapa.current.setView([latCentro, lngCentro], zoom)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [latCentro, lngCentro, zoom])

  // Marcadores de incidencias
  useEffect(() => {
    const capa = capaMarcadores.current
    if (!capa) return
    capa.clearLayers()
    marcadores.forEach((mk) => {
      L.circleMarker([mk.lat, mk.lng], {
        radius: mk.activo ? 11 : 8,
        color: '#ffffff',
        weight: mk.activo ? 3 : 2,
        fillColor: mk.color,
        fillOpacity: 0.95,
      })
        .bindTooltip(mk.titulo, { direction: 'top', offset: [0, -8] })
        .on('click', () => alSeleccionar.current?.(mk.id))
        .addTo(capa)
    })
    if (ajustar && !yaAjustado.current && marcadores.length > 0 && mapa.current) {
      yaAjustado.current = true
      mapa.current.fitBounds(
        L.latLngBounds(marcadores.map((mk) => [mk.lat, mk.lng])),
        { padding: [40, 40], maxZoom: 15 },
      )
    }
  }, [marcadores, ajustar])

  // Acercar a un punto concreto
  useEffect(() => {
    if (mapa.current && enfocar) {
      mapa.current.setView([enfocar.lat, enfocar.lng], Math.max(mapa.current.getZoom(), 16))
    }
  }, [enfocar])

  // Punto elegido
  const latPunto = punto?.[0]
  const lngPunto = punto?.[1]
  useEffect(() => {
    const capa = capaPunto.current
    if (!capa) return
    capa.clearLayers()
    if (latPunto != null) {
      L.marker([latPunto, lngPunto], { icon: iconoPunto(), keyboard: false }).addTo(capa)
    }
  }, [latPunto, lngPunto])

  return <div ref={contenedor} className="mapa" style={{ height: altura }} role="region" aria-label={etiqueta} />
}

const redondear = (valor) => Math.round(valor * 1e6) / 1e6
