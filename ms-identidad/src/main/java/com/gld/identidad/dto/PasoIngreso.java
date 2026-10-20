package com.gld.identidad.dto;

/** Qué debe hacer el frontend después de cada paso de autenticación. */
public enum PasoIngreso {
    /** Sesión completa: el token es de acceso. */
    ACCESO_CONCEDIDO,
    /** Ciudadano con Authenticator vinculado: debe escribir el código de 6 dígitos. */
    VERIFICAR_MFA,
    /** Ciudadano sin Authenticator vinculado: debe escanear el QR. */
    CONFIGURAR_MFA,
    /** Funcionario con clave temporal: debe definir su propia contraseña. */
    CAMBIAR_CLAVE
}
