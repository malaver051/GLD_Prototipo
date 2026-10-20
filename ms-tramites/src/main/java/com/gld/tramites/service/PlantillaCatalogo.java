package com.gld.tramites.service;

import com.gld.tramites.model.TipoTramite;
import com.gld.tramites.repository.TipoTramiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Copia la plantilla del catálogo a un municipio la primera vez que se consulta.
 * Corre en su propia transacción: si dos peticiones llegan a la vez, el índice único
 * evita duplicados y la segunda simplemente usa lo que creó la primera.
 */
@Component
public class PlantillaCatalogo {

    private static final Logger log = LoggerFactory.getLogger(PlantillaCatalogo.class);

    private final TipoTramiteRepository tipos;
    private final TransactionTemplate transaccionNueva;

    public PlantillaCatalogo(TipoTramiteRepository tipos, PlatformTransactionManager transacciones) {
        this.tipos = tipos;
        this.transaccionNueva = new TransactionTemplate(transacciones);
        this.transaccionNueva.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void asegurarCatalogo(Integer municipio) {
        if (tipos.existsByIdMunicipio(municipio)) {
            return;
        }
        try {
            transaccionNueva.executeWithoutResult(estado -> {
                for (TipoTramite modelo : tipos.plantilla()) {
                    tipos.save(modelo.copiarPara(municipio));
                }
                tipos.flush();
            });
            log.info("Catálogo de trámites creado para el municipio {} a partir de la plantilla.", municipio);
        } catch (DataIntegrityViolationException e) {
            log.debug("Otra petición ya creó el catálogo del municipio {}.", municipio);
        }
    }
}
