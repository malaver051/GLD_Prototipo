package com.gld.tramites.model;

/**
 * Patrón State: cada estado de la solicitud define qué transiciones permite.
 * Una acción no válida lanza TransicionInvalidaException (HTTP 409).
 *
 * Flujo (BPMN "Gestión de un trámite municipal en línea"):
 *   radicar ─┬─ con costo ──► PENDIENTE_PAGO ── pago confirmado ──► EN_REVISION
 *            └─ sin costo ──────────────────────────────────────► EN_REVISION
 *   EN_REVISION ── pedir información ──► REQUIERE_INFORMACION ── corrección ──► EN_REVISION
 *   EN_REVISION ── aprobar ──► APROBADA        EN_REVISION ── rechazar ──► RECHAZADA
 */
public enum EstadoSolicitud {

    PENDIENTE_PAGO("Pendiente de pago") {
        @Override
        public EstadoSolicitud confirmarPago() {
            return EN_REVISION;
        }

        @Override
        public boolean admitePago() {
            return true;
        }
    },

    EN_REVISION("En revisión") {
        @Override
        public EstadoSolicitud solicitarInformacion() {
            return REQUIERE_INFORMACION;
        }

        @Override
        public EstadoSolicitud aprobar() {
            return APROBADA;
        }

        @Override
        public EstadoSolicitud rechazar() {
            return RECHAZADA;
        }
    },

    REQUIERE_INFORMACION("Requiere información") {
        @Override
        public EstadoSolicitud reenviarCorreccion() {
            return EN_REVISION;
        }
    },

    APROBADA("Aprobada") {
        @Override
        public boolean esFinal() {
            return true;
        }
    },

    RECHAZADA("Rechazada") {
        @Override
        public boolean esFinal() {
            return true;
        }
    };

    private final String etiqueta;

    EstadoSolicitud(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Estado inicial según el BPMN: ¿el trámite tiene costo? */
    public static EstadoSolicitud inicial(boolean tieneCosto) {
        return tieneCosto ? PENDIENTE_PAGO : EN_REVISION;
    }

    public EstadoSolicitud confirmarPago() {
        throw noPermitida("registrar un pago");
    }

    public EstadoSolicitud solicitarInformacion() {
        throw noPermitida("pedir información");
    }

    public EstadoSolicitud reenviarCorreccion() {
        throw noPermitida("enviar una corrección");
    }

    public EstadoSolicitud aprobar() {
        throw noPermitida("aprobar");
    }

    public EstadoSolicitud rechazar() {
        throw noPermitida("rechazar");
    }

    public boolean admitePago() {
        return false;
    }

    public boolean esFinal() {
        return false;
    }

    protected TransicionInvalidaException noPermitida(String accion) {
        return new TransicionInvalidaException(
                "No se puede " + accion + " una solicitud en estado «" + etiqueta + "».");
    }
}
