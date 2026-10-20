// Teléfono de México: prefijo +52 fijo y 10 dígitos. Recibe id y aria-* desde <Campo>.
export default function CampoTelefono({ valor, alCambiar, ...resto }) {
  return (
    <div className="control-prefijo">
      <span className="control-prefijo__prefijo" aria-hidden="true">
        +52
      </span>
      <input
        {...resto}
        className="control"
        inputMode="numeric"
        autoComplete="tel-national"
        maxLength={10}
        placeholder="10 dígitos"
        value={valor}
        onChange={(e) => alCambiar(e.target.value.replace(/\D/g, '').slice(0, 10))}
      />
    </div>
  )
}
