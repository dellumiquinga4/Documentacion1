package com.banquito.Documentacion.repository;

import com.banquito.Documentacion.model.DocumentoAdjunto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoAdjuntoRepository extends MongoRepository<DocumentoAdjunto, String> {
    
    List<DocumentoAdjunto> findByNumeroSolicitud(String numeroSolicitud);
    
    List<DocumentoAdjunto> findByNumeroSolicitudAndTipoDocumento(String numeroSolicitud, String tipoDocumento);
    
    boolean existsByNumeroSolicitudAndTipoDocumento(String numeroSolicitud, String tipoDocumento);
    
    void deleteByNumeroSolicitud(String numeroSolicitud);

    long countByNumeroSolicitud(String numeroSolicitud);
} 