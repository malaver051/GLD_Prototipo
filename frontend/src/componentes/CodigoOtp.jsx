import { useEffect, useRef } from 'react'

// Seis casillas para el código de Microsoft Authenticator.
// Avanza solo, acepta pegar el código completo y llama a alCompletar con los 6 dígitos.
export default function CodigoOtp({ valor, alCambiar, alCompletar, error = false, deshabilitado = false }) {
  const casillas = useRef([])
  const digitos = Array.from({ length: 6 }, (_, i) => valor[i] || '')

  useEffect(() => {
    casillas.current[0]?.focus()
  }, [])

  function actualizar(nuevo) {
    const limpio = nuevo.replace(/\D/g, '').slice(0, 6)
    alCambiar(limpio)
    if (limpio.length === 6) alCompletar?.(limpio)
    return limpio
  }

  function alEscribir(indice, texto) {
    const solo = texto.replace(/\D/g, '')
    if (!solo) return
    // Autocompletado del sistema o pegado en una casilla: llega el código completo
    if (solo.length >= 6) {
      actualizar(solo)
      casillas.current[5]?.focus()
      return
    }
    const nuevo = digitos.slice()
    nuevo[indice] = solo.slice(-1)
    const unido = actualizar(nuevo.join(''))
    casillas.current[Math.min(unido.length, 5)]?.focus()
  }

  function alTeclear(indice, evento) {
    if (evento.key === 'Backspace') {
      evento.preventDefault()
      const nuevo = digitos.slice()
      if (nuevo[indice]) {
        nuevo[indice] = ''
      } else if (indice > 0) {
        nuevo[indice - 1] = ''
        casillas.current[indice - 1]?.focus()
      }
      alCambiar(nuevo.join(''))
    } else if (evento.key === 'ArrowLeft' && indice > 0) {
      casillas.current[indice - 1]?.focus()
    } else if (evento.key === 'ArrowRight' && indice < 5) {
      casillas.current[indice + 1]?.focus()
    }
  }

  return (
    <div className="codigo" data-error={error} role="group" aria-label="Código de 6 dígitos">
      {digitos.map((d, i) => (
        <input
          key={i}
          ref={(el) => (casillas.current[i] = el)}
          className="codigo__casilla"
          inputMode="numeric"
          autoComplete={i === 0 ? 'one-time-code' : 'off'}
          maxLength={6}
          value={d}
          disabled={deshabilitado}
          aria-label={`Dígito ${i + 1} de 6`}
          onChange={(e) => alEscribir(i, e.target.value)}
          onKeyDown={(e) => alTeclear(i, e)}
          onFocus={(e) => e.target.select()}
          onPaste={(e) => {
            e.preventDefault()
            const limpio = actualizar(e.clipboardData.getData('text'))
            casillas.current[Math.min(limpio.length, 5)]?.focus()
          }}
        />
      ))}
    </div>
  )
}
