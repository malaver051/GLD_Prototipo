package com.gld.identidad.model;

public enum EstadoUsuario {
    /** Ciudadano registrado que aún no vincula Microsoft Authenticator. */
    PENDIENTE_MFA,
    ACTIVO,
    INACTIVO
}
