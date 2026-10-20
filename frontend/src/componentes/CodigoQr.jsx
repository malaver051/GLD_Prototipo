import { useEffect, useState } from 'react'
import QRCode from 'qrcode'

// Dibuja el código QR (otpauth://...) que se escanea con Microsoft Authenticator.
export default function CodigoQr({ texto, descripcion }) {
  const [imagen, setImagen] = useState(null)

  useEffect(() => {
    let vigente = true
    QRCode.toDataURL(texto, { width: 336, margin: 1, errorCorrectionLevel: 'M', color: { dark: '#1e2b2f', light: '#ffffff' } })
      .then((url) => vigente && setImagen(url))
      .catch(() => vigente && setImagen(null))
    return () => {
      vigente = false
    }
  }, [texto])

  if (!imagen) return <div className="qr__imagen" aria-hidden="true" />
  return <img className="qr__imagen" src={imagen} alt={descripcion} width="168" height="168" />
}
