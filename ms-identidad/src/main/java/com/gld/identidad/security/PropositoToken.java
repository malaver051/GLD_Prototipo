package com.gld.identidad.security;

/**
 * Para qué sirve un token. Solo ACCESO abre la aplicación; los demás son temporales
 * y únicamente permiten completar un paso de la autenticación.
 */
public enum PropositoToken {
    ACCESO,
    MFA,
    MFA_CONFIGURACION,
    CAMBIO_CLAVE
}
