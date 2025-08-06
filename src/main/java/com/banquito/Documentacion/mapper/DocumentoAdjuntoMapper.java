package com.banquito.Documentacion.mapper;

import com.banquito.Documentacion.dto.DocumentoAdjuntoDTO;
import com.banquito.Documentacion.dto.DocumentoAdjuntoResponseDTO;
import com.banquito.Documentacion.model.DocumentoAdjunto;
import org.mapstruct.Mapper;




@Mapper(componentModel = "spring")
public interface DocumentoAdjuntoMapper {
    DocumentoAdjunto toEntity(DocumentoAdjuntoDTO dto);
    DocumentoAdjuntoResponseDTO toResponseDTO(DocumentoAdjunto entity);

}