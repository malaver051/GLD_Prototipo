package com.gld.tramites.service;

import com.gld.tramites.support.OrigenFrontend;
import com.gld.tramites.config.PropiedadesGld;
import com.gld.tramites.dto.Verificacion;
import com.gld.tramites.model.Documento;
import com.gld.tramites.model.SolicitudTramite;
import com.gld.tramites.repository.DocumentoRepository;
import com.gld.tramites.support.AlmacenamientoArchivos;
import com.gld.tramites.support.Codigos;
import com.gld.tramites.support.pdf.DatosDocumentoOficial;
import com.gld.tramites.support.pdf.GeneradorDocumentoOficial;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Emisión y verificación del documento oficial (BPMN: «Emitir y firmar documento oficial»).
 * La firma electrónica es simulada: se imprime el funcionario, la fecha y un sello SHA-256
 * calculado con los datos del documento.
 */
@Service
@Transactional
public class DocumentoService {

    private static final DateTimeFormatter FECHA_CORTA =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es-MX"));

    private final DocumentoRepository documentos;
    private final AlmacenamientoArchivos almacenamiento;
    private final GeneradorDocumentoOficial generador;
    private final String urlFrontend;

    public DocumentoService(DocumentoRepository documentos, AlmacenamientoArchivos almacenamiento,
                            GeneradorDocumentoOficial generador, PropiedadesGld propiedades) {
        this.documentos = documentos;
        this.almacenamiento = almacenamiento;
        this.generador = generador;
        this.urlFrontend = propiedades.frontend().url();
    }

    public Documento emitirOficial(SolicitudTramite s, Integer idFuncionario, String nombreFuncionario, String comentario) {
        LocalDateTime emision = LocalDateTime.now();
        String codigo = Codigos.codigoVerificacion();
        String sello = Codigos.sha256(String.join("|", s.getNumeroRadicado(), codigo, s.getNombreCiudadano(),
                s.getDocumentoCiudadano(), s.getTipoTramite().getNombre(), emision.toString(), nombreFuncionario));
        String resumenPago = s.pagoAprobado()
                .map(p -> "Referencia " + p.getReferenciaOrden() + " | $" + p.getValor().toPlainString() + " MXN | "
                        + p.getBanco() + " | " + FECHA_CORTA.format(p.getFechaConfirmacion()))
                .orElse("Trámite gratuito");

        byte[] pdf = generador.generar(new DatosDocumentoOficial(
                s.getMunicipioNombre(),
                s.getMunicipioEstado(),
                s.getTipoTramite().getDependencia(),
                s.getTipoTramite().getNombre(),
                s.getNumeroRadicado(),
                s.getFechaRadicacion(),
                emision,
                s.getNombreCiudadano(),
                s.getDocumentoCiudadano(),
                s.getCorreoCiudadano(),
                s.getObservaciones(),
                resumenPago,
                comentario,
                nombreFuncionario,
                codigo,
                OrigenFrontend.resolver(urlFrontend) + "/verificar?codigo=" + codigo,
                sello));

        String nombreArchivo = "documento-oficial-" + s.getNumeroRadicado() + ".pdf";
        String ruta = almacenamiento.guardar(s.getIdSolicitud(), nombreArchivo, pdf);
        return s.emitirDocumentoOficial(nombreArchivo, ruta, pdf.length, idFuncionario, codigo, sello);
    }

    public byte[] contenido(Documento documento) {
        return almacenamiento.leer(documento.getRutaArchivo());
    }

    /** Verificación pública con el código impreso en el documento. */
    @Transactional(readOnly = true)
    public Verificacion verificar(String codigo) {
        return documentos.findByCodigoVerificacion(codigo.trim().toUpperCase(Locale.ROOT))
                .filter(Documento::esOficial)
                .map(d -> {
                    SolicitudTramite s = d.getSolicitud();
                    return new Verificacion(true, s.getTipoTramite().getNombre(), s.getNumeroRadicado(),
                            s.getMunicipioNombre(), s.getMunicipioEstado(), ocultarNombre(s.getNombreCiudadano()),
                            d.getFechaCarga(), d.getSelloSha256());
                })
                .orElse(Verificacion.invalido());
    }

    /** «Ana María Gómez Ruiz» → «Ana M. G. R.»: confirma el titular sin exponer el nombre completo. */
    static String ocultarNombre(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        StringBuilder sb = new StringBuilder(partes[0]);
        for (int i = 1; i < partes.length; i++) {
            sb.append(' ').append(partes[i].charAt(0)).append('.');
        }
        return sb.toString();
    }
}
