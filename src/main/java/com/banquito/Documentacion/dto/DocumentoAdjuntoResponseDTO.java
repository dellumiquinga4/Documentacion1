package com.banquito.Documentacion.dto;

import lombok.Data;
import java.time.LocalDateTime;

import com.banquito.Documentacion.enums.EstadoDocumentoEnum;

@Data
public class DocumentoAdjuntoResponseDTO {
    private String id;
    private String numeroSolicitud;
    private String tipoDocumento;
    private String nombreArchivo;
    private String rutaStorage;
    private LocalDateTime fechaCarga;
    private EstadoDocumentoEnum estado;
    private LocalDateTime fechaActualizacion;
    private Long version;
    private String observacion;
}