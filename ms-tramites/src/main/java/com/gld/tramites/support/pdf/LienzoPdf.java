package com.gld.tramites.support.pdf;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Escritor mínimo de PDF (una página tamaño carta) sin librerías externas.
 * Usa las fuentes estándar Helvetica y Helvetica-Bold con codificación WinAnsi,
 * que incluye los acentos y la ñ del español.
 *
 * Coordenadas en puntos (1/72 de pulgada), con el origen en la esquina inferior izquierda.
 */
public class LienzoPdf {

    public static final float ANCHO = 612f;
    public static final float ALTO = 792f;
    private static final Charset WIN_ANSI = Charset.forName("windows-1252");

    public enum Fuente {
        NORMAL("F1"), NEGRITA("F2");

        private final String recurso;

        Fuente(String recurso) {
            this.recurso = recurso;
        }
    }

    private final StringBuilder contenido = new StringBuilder();

    // ------------------------------------------------------------------ dibujo

    public void texto(float x, float y, Fuente fuente, float tamano, Color color, String texto) {
        contenido.append("BT\n")
                .append(color.relleno()).append('\n')
                .append('/').append(fuente.recurso).append(' ').append(num(tamano)).append(" Tf\n")
                .append(num(x)).append(' ').append(num(y)).append(" Td\n")
                .append('(').append(escapar(texto)).append(") Tj\nET\n");
    }

    public void rectangulo(float x, float y, float ancho, float alto, Color relleno) {
        contenido.append(relleno.relleno()).append('\n')
                .append(num(x)).append(' ').append(num(y)).append(' ')
                .append(num(ancho)).append(' ').append(num(alto)).append(" re f\n");
    }

    public void marco(float x, float y, float ancho, float alto, float grosor, Color color) {
        contenido.append(color.trazo()).append('\n')
                .append(num(grosor)).append(" w\n")
                .append(num(x)).append(' ').append(num(y)).append(' ')
                .append(num(ancho)).append(' ').append(num(alto)).append(" re S\n");
    }

    public void linea(float x1, float y1, float x2, float y2, float grosor, Color color) {
        contenido.append(color.trazo()).append('\n')
                .append(num(grosor)).append(" w\n")
                .append(num(x1)).append(' ').append(num(y1)).append(" m ")
                .append(num(x2)).append(' ').append(num(y2)).append(" l S\n");
    }

    /** Divide un texto en líneas que caben en el ancho indicado. */
    public static List<String> ajustar(String texto, Fuente fuente, float tamano, float anchoMaximo) {
        List<String> lineas = new ArrayList<>();
        for (String parrafo : (texto == null ? "" : texto).split("\n")) {
            StringBuilder actual = new StringBuilder();
            for (String palabra : parrafo.split(" ")) {
                String prueba = actual.isEmpty() ? palabra : actual + " " + palabra;
                if (anchoTexto(prueba, fuente, tamano) <= anchoMaximo || actual.isEmpty()) {
                    actual = new StringBuilder(prueba);
                } else {
                    lineas.add(actual.toString());
                    actual = new StringBuilder(palabra);
                }
            }
            lineas.add(actual.toString());
        }
        return lineas;
    }

    /** Ancho aproximado según las métricas de Helvetica (milésimas de em). */
    public static float anchoTexto(String texto, Fuente fuente, float tamano) {
        float total = 0;
        for (char c : texto.toCharArray()) {
            total += anchoCaracter(c);
        }
        float factor = fuente == Fuente.NEGRITA ? 1.06f : 1f;
        return total * tamano / 1000f * factor;
    }

    private static int anchoCaracter(char c) {
        char base = quitarAcento(c);
        if (base == ' ') return 278;
        if ("ijl".indexOf(base) >= 0) return 222;
        if ("ft".indexOf(base) >= 0) return 278;
        if (base == 'r') return 333;
        if (base == 'm') return 833;
        if (base == 'w') return 722;
        if ("csvxyz".indexOf(base) >= 0) return 500;
        if (base == 'k') return 500;
        if (Character.isLowerCase(base)) return 556;
        if (base == 'I') return 278;
        if (base == 'J') return 500;
        if ("MW".indexOf(base) >= 0) return base == 'M' ? 833 : 944;
        if ("CDGHNOQRU".indexOf(base) >= 0) return 750;
        if ("FTZL".indexOf(base) >= 0) return 600;
        if (Character.isUpperCase(base)) return 667;
        if (Character.isDigit(base)) return 556;
        if (".,:;'!|".indexOf(base) >= 0) return 278;
        if ("()-/".indexOf(base) >= 0) return 320;
        if (base == '%') return 889;
        if (base == '@') return 1015;
        return 556;
    }

    private static char quitarAcento(char c) {
        String acentos = "áéíóúüñÁÉÍÓÚÜÑ";
        String bases = "aeiouunAEIOUUN";
        int i = acentos.indexOf(c);
        return i >= 0 ? bases.charAt(i) : c;
    }

    // ------------------------------------------------------------------ armado

    /** Construye el archivo PDF completo con una sola página. */
    public byte[] construir(String titulo) {
        byte[] flujo = contenido.toString().getBytes(WIN_ANSI);
        List<byte[]> objetos = new ArrayList<>();
        objetos.add(ascii("<< /Type /Catalog /Pages 2 0 R >>"));
        objetos.add(ascii("<< /Type /Pages /Kids [3 0 R] /Count 1 >>"));
        objetos.add(ascii("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + num(ANCHO) + " " + num(ALTO) + "]"
                + " /Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>"));
        objetos.add(ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"));
        objetos.add(ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>"));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.writeBytes(ascii("<< /Length " + flujo.length + " >>\nstream\n"));
        stream.writeBytes(flujo);
        stream.writeBytes(ascii("\nendstream"));
        objetos.add(stream.toByteArray());
        ByteArrayOutputStream info = new ByteArrayOutputStream();
        info.writeBytes(ascii("<< /Title ("));
        info.writeBytes(escapar(titulo).getBytes(WIN_ANSI));
        info.writeBytes(ascii(") /Producer (GLD ms-tramites) >>"));
        objetos.add(info.toByteArray());

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        pdf.writeBytes(ascii("%PDF-1.4\n%âãÏÓ\n"));
        List<Integer> posiciones = new ArrayList<>();
        for (int i = 0; i < objetos.size(); i++) {
            posiciones.add(pdf.size());
            pdf.writeBytes(ascii((i + 1) + " 0 obj\n"));
            pdf.writeBytes(objetos.get(i));
            pdf.writeBytes(ascii("\nendobj\n"));
        }
        int inicioXref = pdf.size();
        StringBuilder xref = new StringBuilder();
        xref.append("xref\n0 ").append(objetos.size() + 1).append('\n');
        xref.append("0000000000 65535 f \n");
        for (int p : posiciones) {
            xref.append(String.format(Locale.ROOT, "%010d 00000 n \n", p));
        }
        xref.append("trailer\n<< /Size ").append(objetos.size() + 1)
                .append(" /Root 1 0 R /Info ").append(objetos.size()).append(" 0 R >>\n")
                .append("startxref\n").append(inicioXref).append("\n%%EOF\n");
        pdf.writeBytes(ascii(xref.toString()));
        return pdf.toByteArray();
    }

    private static byte[] ascii(String s) {
        return s.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static String escapar(String texto) {
        StringBuilder sb = new StringBuilder();
        for (char c : (texto == null ? "" : texto).toCharArray()) {
            if (c == '\\' || c == '(' || c == ')') {
                sb.append('\\');
            }
            sb.append(WIN_ANSI.newEncoder().canEncode(c) ? c : '?');
        }
        return sb.toString();
    }

    private static String num(float valor) {
        return String.format(Locale.ROOT, "%.2f", valor);
    }

    /** Color RGB con componentes de 0 a 255. */
    public record Color(int r, int g, int b) {

        public static Color hex(String hex) {
            int v = Integer.parseInt(hex.replace("#", ""), 16);
            return new Color((v >> 16) & 0xFF, (v >> 8) & 0xFF, v & 0xFF);
        }

        String relleno() {
            return componentes() + " rg";
        }

        String trazo() {
            return componentes() + " RG";
        }

        private String componentes() {
            return String.format(Locale.ROOT, "%.3f %.3f %.3f", r / 255f, g / 255f, b / 255f);
        }
    }
}
