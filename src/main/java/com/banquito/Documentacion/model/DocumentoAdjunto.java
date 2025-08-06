package com.banquito.Documentacion.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.banquito.Documentacion.enums.EstadoDocumentoEnum;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Objects;

@Document(collection = "documentos_adjuntos")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class DocumentoAdjunto {

    @Id
    private String id;

    @Field("numero_solicitud")
    private String numeroSolicitud;

    @Field("tipo_documento")
    private String tipoDocumento;

    @Field("nombre_archivo")
    private String nombreArchivo;

    @Field("ruta_storage")
    private String rutaStorage;

    @Field("fecha_carga")
    @CreatedDate
    private LocalDateTime fechaCarga;

    @Field("fecha_actualizacion")
    @LastModifiedDate
    private LocalDateTime fechaActualizacion;

    @Field("estado")
    private EstadoDocumentoEnum estado = EstadoDocumentoEnum.CARGADO;

    @Field("observacion")
    private String observacion;

    @Field("version")
    private Long version;

    public DocumentoAdjunto(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        DocumentoAdjunto that = (DocumentoAdjunto) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}