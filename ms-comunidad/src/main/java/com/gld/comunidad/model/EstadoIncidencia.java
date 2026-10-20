package com.gld.comunidad.model;

/**
 * Ciclo de vida de una incidencia (contrato 07-comunidad.yaml).
 *
 * <pre>
 * REPORTADA ──asignar──▶ ASIGNADA ──iniciar atención──▶ EN_ATENCION ──resolver──▶ RESUELTA ──calificar──▶ CERRADA
 *                           ▲  └─reasignar─┘                 │ reasignar                 │
 *                           └────────────────────────────────┘◀──────reabrir (una vez)──┘
 * </pre>
 *
 * Patrón State (GRASP · Polimorfismo): cada estado define qué transiciones admite.
 * Una acción no permitida lanza {@link TransicionInvalidaException} y la API responde 409.
 */
public enum EstadoIncidencia {

    REPORTADA("Reportada") {
        @Override
        public EstadoIncidencia asignar() {
            return ASIGNADA;
        }
    },

    ASIGNADA("Asignada") {
        @Override
        public EstadoIncidencia asignar() {
            return ASIGNADA;
        }

        @Override
        public EstadoIncidencia iniciarAtencion() {
            return EN_ATENCION;
        }
    },

    EN_ATENCION("En atención") {
        /** Si cambia la dependencia, la nueva debe iniciar la atención. */
        @Override
        public EstadoIncidencia asignar() {
            return ASIGNADA;
        }

        @Override
        public EstadoIncidencia resolver() {
            return RESUELTA;
        }
    },

    RESUELTA("Resuelta") {
        @Override
        public EstadoIncidencia calificar() {
            return CERRADA;
        }

        @Override
        public EstadoIncidencia reabrir() {
            return ASIGNADA;
        }
    },

    CERRADA("Cerrada");

    private final String etiqueta;

    EstadoIncidencia(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }

    public EstadoIncidencia asignar() {
        throw invalida("asignar a una dependencia");
    }

    public EstadoIncidencia iniciarAtencion() {
        throw invalida("iniciar la atención");
    }

    public EstadoIncidencia resolver() {
        throw invalida("marcar como resuelta");
    }

    public EstadoIncidencia calificar() {
        throw invalida("calificar la atención");
    }

    public EstadoIncidencia reabrir() {
        throw invalida("reabrir");
    }

    public boolean admiteAsignacion() {
        return this == REPORTADA || this == ASIGNADA || this == EN_ATENCION;
    }

    /** Incidencia que sigue pendiente para el municipio. */
    public boolean estaAbierta() {
        return this != RESUELTA && this != CERRADA;
    }

    private TransicionInvalidaException invalida(String accion) {
        return new TransicionInvalidaException(
                "No se puede " + accion + " una incidencia en estado «" + etiqueta + "».");
    }
}
