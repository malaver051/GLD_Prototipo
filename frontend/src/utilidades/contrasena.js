// Misma regla que PoliticaContrasena.java: 8+ caracteres, mayúscula, número y símbolo.
export const REQUISITOS_CONTRASENA = [
  { clave: 'longitud', texto: 'Al menos 8 caracteres', cumple: (v) => v.length >= 8 },
  { clave: 'mayuscula', texto: 'Una letra mayúscula', cumple: (v) => /\p{Lu}/u.test(v) },
  { clave: 'numero', texto: 'Un número', cumple: (v) => /\p{Nd}/u.test(v) },
  { clave: 'simbolo', texto: 'Un símbolo, por ejemplo ! # $ % & * ?', cumple: (v) => /[^\p{L}\p{N}\s]/u.test(v) },
]

export const contrasenaValida = (valor) => REQUISITOS_CONTRASENA.every((r) => r.cumple(valor))
