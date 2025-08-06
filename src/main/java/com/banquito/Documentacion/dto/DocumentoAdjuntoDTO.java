package com.banquito.Documentacion.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

import com.banquito.Documentacion.enums.EstadoDocumentoEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoAdjuntoDTO {
    private String numeroSolicitud;
    private String tipoDocumento;
    private String nombreArchivo;
    private String rutaStorage;
    private LocalDateTime fechaCarga;
    private Long version;
    private EstadoDocumentoEnum estado;
}