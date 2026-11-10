package com.gld.tramites.support.pdf;

import com.gld.tramites.support.pdf.LienzoPdf.Color;
import com.gld.tramites.support.pdf.LienzoPdf.Fuente;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Arma el PDF del documento oficial que se entrega al aprobar una solicitud.
 * GRASP · Fabricación pura: la presentación del documento no pertenece al dominio.
 */
public class GeneradorDocumentoOficial {

    private static final Color TINTA = Color.hex("1E2B2F");
    private static final Color TINTA_SUAVE = Color.hex("55636A");
    private static final Color PRIMARIO = Color.hex("2D5B5E");
    private static final Color PRIMARIO_SUAVE = Color.hex("E2ECEB");
    private static final Color LINEA = Color.hex("C3CBC8");
    private static final Color BLANCO = Color.hex("FFFFFF");
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-MX"));
    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, HH:mm 'h'", Locale.forLanguageTag("es-MX"));

    private static final float MARGEN = 56f;
    private static final float ANCHO_UTIL = LienzoPdf.ANCHO - 2 * MARGEN;

    public byte[] generar(DatosDocumentoOficial d) {
        LienzoPdf pdf = new LienzoPdf();
        float y = LienzoPdf.ALTO;

        // Encabezado institucional
        pdf.rectangulo(0, y - 92, LienzoPdf.ANCHO, 92, PRIMARIO);
        pdf.texto(MARGEN, y - 44, Fuente.NEGRITA, 15, BLANCO, "H. Ayuntamiento de " + d.municipio());
        pdf.texto(MARGEN, y - 63, Fuente.NORMAL, 10, Color.hex("D6E3E1"), d.estado() + "  |  " + d.dependencia());
        String sello = "GLD";
        pdf.marco(LienzoPdf.ANCHO - MARGEN - 40, y - 66, 40, 40, 1.2f, BLANCO);
        pdf.texto(LienzoPdf.ANCHO - MARGEN - 31, y - 51, Fuente.NEGRITA, 11, BLANCO, sello);
        y -= 130;

        // Título
        for (String linea : LienzoPdf.ajustar(d.tramite().toUpperCase(Locale.forLanguageTag("es-MX")), Fuente.NEGRITA, 17, ANCHO_UTIL)) {
            pdf.texto(MARGEN, y, Fuente.NEGRITA, 17, TINTA, linea);
            y -= 22;
        }
        pdf.texto(MARGEN, y, Fuente.NORMAL, 10.5f, TINTA_SUAVE,
                "Documento oficial  |  Radicado " + d.numeroRadicado() + "  |  " + FECHA.format(d.fechaEmision()));
        y -= 30;

        // Texto de la constancia
        String cuerpo = "El H. Ayuntamiento de " + d.municipio() + ", " + d.estado() + ", por conducto de "
                + d.dependencia() + ", hace constar que la solicitud con número de radicado " + d.numeroRadicado()
                + ", presentada por " + d.solicitante() + ", identificado(a) con " + d.documentoSolicitante()
                + ", fue revisada y APROBADA. En consecuencia, se expide el presente documento correspondiente al trámite «"
                + d.tramite() + "» para los fines legales que al interesado convengan.";
        for (String linea : LienzoPdf.ajustar(cuerpo, Fuente.NORMAL, 11, ANCHO_UTIL)) {
            pdf.texto(MARGEN, y, Fuente.NORMAL, 11, TINTA, linea);
            y -= 16;
        }
        y -= 14;

        // Datos de la solicitud
        List<String[]> filas = new ArrayList<>();
        filas.add(new String[]{"Solicitante", d.solicitante()});
        filas.add(new String[]{"Identificación", d.documentoSolicitante()});
        filas.add(new String[]{"Correo electrónico", d.correoSolicitante()});
        filas.add(new String[]{"Trámite", d.tramite()});
        filas.add(new String[]{"Fecha de radicación", FECHA_HORA.format(d.fechaRadicacion())});
        filas.add(new String[]{"Fecha de emisión", FECHA_HORA.format(d.fechaEmision())});
        filas.add(new String[]{"Pago", d.resumenPago()});
        if (tieneTexto(d.observacionesSolicitante())) {
            filas.add(new String[]{"Motivo de la solicitud", recortar(d.observacionesSolicitante(), 280)});
        }
        if (tieneTexto(d.comentarioFuncionario())) {
            filas.add(new String[]{"Observaciones de la resolución", recortar(d.comentarioFuncionario(), 280)});
        }
        float anchoEtiqueta = 150;
        float anchoValor = ANCHO_UTIL - anchoEtiqueta - 12;
        pdf.linea(MARGEN, y + 10, MARGEN + ANCHO_UTIL, y + 10, 0.6f, LINEA);
        for (String[] fila : filas) {
            List<String> valor = LienzoPdf.ajustar(fila[1], Fuente.NORMAL, 10, anchoValor);
            List<String> etiqueta = LienzoPdf.ajustar(fila[0], Fuente.NEGRITA, 9.5f, anchoEtiqueta);
            float yFila = y;
            for (String l : etiqueta) {
                pdf.texto(MARGEN, yFila - 4, Fuente.NEGRITA, 9.5f, TINTA_SUAVE, l);
                yFila -= 13;
            }
            float yValor = y;
            for (String l : valor) {
                pdf.texto(MARGEN + anchoEtiqueta + 12, yValor - 4, Fuente.NORMAL, 10, TINTA, l);
                yValor -= 13;
            }
            y = Math.min(yFila, yValor) - 8;
            pdf.linea(MARGEN, y + 10, MARGEN + ANCHO_UTIL, y + 10, 0.6f, LINEA);
        }
        y -= 26;

        // Firma electrónica
        float yFirma = Math.max(y, 230);
        pdf.linea(MARGEN, yFirma, MARGEN + 230, yFirma, 0.8f, TINTA);
        pdf.texto(MARGEN, yFirma - 15, Fuente.NEGRITA, 10.5f, TINTA, d.funcionario());
        pdf.texto(MARGEN, yFirma - 29, Fuente.NORMAL, 9.5f, TINTA_SUAVE, "Funcionario municipal, " + d.municipio());
        pdf.texto(MARGEN, yFirma - 43, Fuente.NORMAL, 9.5f, TINTA_SUAVE,
                "Firmado electrónicamente el " + FECHA_HORA.format(d.fechaEmision()));

        // Verificación
        float alto = 92;
        float yCaja = 70;
        pdf.rectangulo(MARGEN, yCaja, ANCHO_UTIL, alto, PRIMARIO_SUAVE);
        pdf.texto(MARGEN + 14, yCaja + alto - 22, Fuente.NEGRITA, 10, PRIMARIO, "Código de verificación");
        pdf.texto(MARGEN + 14, yCaja + alto - 42, Fuente.NEGRITA, 16, TINTA, d.codigoVerificacion());
        pdf.texto(MARGEN + 210, yCaja + alto - 22, Fuente.NORMAL, 9, TINTA_SUAVE, "Verifica la autenticidad de este documento en:");
        // La dirección se parte en líneas para que no se salga del recuadro (en Codespaces es larga)
        float yUrl = yCaja + alto - 36;
        for (String linea : partirEnlace(d.urlVerificacion(), Fuente.NORMAL, 9, ANCHO_UTIL - 210 - 14)) {
            pdf.texto(MARGEN + 210, yUrl, Fuente.NORMAL, 9, TINTA, linea);
            yUrl -= 11;
        }
        pdf.texto(MARGEN + 14, yCaja + 26, Fuente.NEGRITA, 8, TINTA_SUAVE, "Sello digital (SHA-256)");
        pdf.texto(MARGEN + 14, yCaja + 13, Fuente.NORMAL, 8, TINTA_SUAVE, d.selloSha256());

        pdf.texto(MARGEN, 44, Fuente.NORMAL, 7.5f, TINTA_SUAVE,
                "Documento emitido por la plataforma Gobiernos Locales Digitales (GLD). Prototipo académico: la firma electrónica es simulada.");

        return pdf.construir(d.tramite() + " - " + d.numeroRadicado());
    }

    /** Limita los textos libres para que el documento quepa en una página. */
    private static String recortar(String valor, int maximo) {
        String limpio = valor.strip().replaceAll("\\s+", " ");
        return limpio.length() <= maximo ? limpio : limpio.substring(0, maximo - 1).strip() + "…";
    }

    private static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    /**
     * Parte un enlace (sin espacios) en líneas de hasta {@code anchoMaximo}, cortando de preferencia
     * después de "/", "?", "&" o "=". Como máximo 3 líneas.
     */
    static List<String> partirEnlace(String enlace, Fuente fuente, float tamano, float anchoMaximo) {
        List<String> lineas = new ArrayList<>();
        String resto = enlace == null ? "" : enlace;
        while (!resto.isEmpty() && lineas.size() < 3) {
            if (LienzoPdf.anchoTexto(resto, fuente, tamano) <= anchoMaximo || lineas.size() == 2) {
                lineas.add(resto);
                break;
            }
            int cabe = 1;
            while (cabe < resto.length() && LienzoPdf.anchoTexto(resto.substring(0, cabe + 1), fuente, tamano) <= anchoMaximo) {
                cabe++;
            }
            int corte = cabe;
            for (int i = cabe; i > cabe / 2; i--) {
                if ("/?&=".indexOf(resto.charAt(i - 1)) >= 0) {
                    corte = i;
                    break;
                }
            }
            lineas.add(resto.substring(0, corte));
            resto = resto.substring(corte);
        }
        return lineas;
    }
}
