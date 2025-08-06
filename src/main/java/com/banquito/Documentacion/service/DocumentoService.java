package com.banquito.Documentacion.service;

import com.banquito.Documentacion.client.CoreBancarioClient;
import com.banquito.Documentacion.client.ClientesClient;
import com.banquito.Documentacion.client.PrestamoClient;
import com.banquito.Documentacion.client.OriginacionClient;
import com.banquito.Documentacion.dto.ClienteDTO;
import com.banquito.Documentacion.dto.CrearPrestamoRequest;
import com.banquito.Documentacion.dto.DetalleSolicitudResponseDTO;
import com.banquito.Documentacion.dto.DocumentoAdjuntoDTO;
import com.banquito.Documentacion.dto.DocumentoAdjuntoResponseDTO;
import com.banquito.Documentacion.enums.EstadoDocumentoEnum;
import com.banquito.Documentacion.enums.TipoDocumentoEnum;
import com.banquito.Documentacion.exception.CreateEntityException;
import com.banquito.Documentacion.exception.ResourceNotFoundException;
import com.banquito.Documentacion.mapper.DocumentoAdjuntoMapper;
import com.banquito.Documentacion.model.DocumentoAdjunto;
import com.banquito.Documentacion.repository.DocumentoAdjuntoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentoService {

    private final DocumentoAdjuntoRepository documentoAdjuntoRepository;
    private final DocumentoAdjuntoMapper documentoAdjuntoMapper;
    private final FileStorageService fileStorageService;
    private final OriginacionClient originacionClient;

    private final CoreBancarioClient coreBancarioClient;
    private final PrestamoClient prestamoClient;
    private final ClientesClient clientesClient;

    /** nuevo método: contar cuántos hay */
    public long countPorSolicitud(String numeroSolicitud) {
        return documentoAdjuntoRepository.countByNumeroSolicitud(numeroSolicitud);
    }

    /** usar el mismo feign que ya hiciste */
    public DetalleSolicitudResponseDTO obtenerDetalleSolicitud(String numeroSolicitud) {
        return originacionClient.obtenerDetalle(numeroSolicitud);
    }

    public void notificarDocumentacionCargada(String numeroSolicitud) {
        DetalleSolicitudResponseDTO det = obtenerDetalleSolicitud(numeroSolicitud);
        originacionClient.cambiarEstado(
                det.getIdSolicitud(),
                "DOCUMENTACION_CARGADA",
                "Se subieron los 3 documentos",
                "vendedor");
    }

    public DocumentoAdjuntoResponseDTO cargarDocumento(String numeroSolicitud, MultipartFile archivo,
            String tipoDocumento) {
        // Validar que el tipo de documento es válido
        try {
            TipoDocumentoEnum.valueOf(tipoDocumento);
        } catch (IllegalArgumentException e) {
            throw new CreateEntityException("DocumentoAdjunto", "Tipo de documento no válido: " + tipoDocumento);
        }

        // Validar que no existe ya un documento de este tipo para esta solicitud
        if (documentoAdjuntoRepository.existsByNumeroSolicitudAndTipoDocumento(numeroSolicitud, tipoDocumento)) {
            throw new CreateEntityException("DocumentoAdjunto",
                    "Ya existe un documento de tipo " + tipoDocumento + " para esta solicitud");
        }

        String rutaArchivo = fileStorageService.storeFile(archivo, numeroSolicitud, tipoDocumento);

        // Extraer el nombre del archivo de la ruta generada
        String nombreArchivo = rutaArchivo.substring(rutaArchivo.lastIndexOf("/") + 1);

        // Crear DTO y usar mapper para convertir a entidad
        DocumentoAdjuntoDTO documentoDTO = new DocumentoAdjuntoDTO(
                numeroSolicitud,
                tipoDocumento,
                nombreArchivo,
                rutaArchivo,
                LocalDateTime.now(),
                1L,
                EstadoDocumentoEnum.CARGADO);

        DocumentoAdjunto doc = documentoAdjuntoMapper.toEntity(documentoDTO);
        DocumentoAdjunto saved = documentoAdjuntoRepository.save(doc);
        log.info("Documento cargado exitosamente: {} para solicitud {}", tipoDocumento, numeroSolicitud);
        return documentoAdjuntoMapper.toResponseDTO(saved);
    }

    public List<DocumentoAdjuntoResponseDTO> listarDocumentos(String numeroSolicitud) {
        List<DocumentoAdjunto> documentos = documentoAdjuntoRepository.findByNumeroSolicitud(numeroSolicitud);
        return documentos.stream()
                .map(documentoAdjuntoMapper::toResponseDTO)
                .toList();
    }

    public DocumentoAdjuntoResponseDTO validarDocumento(String numeroSolicitud, String idDocumento) {
        var doc = documentoAdjuntoRepository.findById(idDocumento)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado"));
        if (!doc.getNumeroSolicitud().equals(numeroSolicitud)) {
            throw new CreateEntityException("DocumentoAdjunto", "No pertenece a la solicitud");
        }
        doc.setEstado(EstadoDocumentoEnum.VALIDADO);
        DocumentoAdjunto saved = documentoAdjuntoRepository.save(doc);

        return documentoAdjuntoMapper.toResponseDTO(saved);
    }

    public DocumentoAdjuntoResponseDTO rechazarDocumento(
            String numeroSolicitud,
            String idDocumento,
            String observacion) {

        // 1) Recuperar entidad o lanzar 404
        DocumentoAdjunto doc = documentoAdjuntoRepository.findById(idDocumento)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado"));

        // 2) Validar que pertenece a la solicitud
        if (!doc.getNumeroSolicitud().equals(numeroSolicitud)) {
            throw new CreateEntityException("DocumentoAdjunto",
                    "El documento no pertenece a la solicitud especificada");
        }

        // 3) Actualizar estado y observación
        doc.setEstado(EstadoDocumentoEnum.RECHAZADO);
        doc.setObservacion(observacion); // necesitarás un campo `observacion` en tu modelo

        // 4) Guardar cambios
        DocumentoAdjunto actualizado = documentoAdjuntoRepository.save(doc);

        // 5) Convertir y devolver DTO
        return documentoAdjuntoMapper.toResponseDTO(actualizado);
    }

    // Valdiar todos los documentos de una solicitud
    @Transactional
    public void validarTodos(String numeroSolicitud, String usuario) {
        List<DocumentoAdjunto> docs = documentoAdjuntoRepository.findByNumeroSolicitud(numeroSolicitud);
        // 1) marcar cada uno como VALIDADO
        docs.forEach(d -> {
            if (d.getEstado() == EstadoDocumentoEnum.CARGADO) {
                d.setEstado(EstadoDocumentoEnum.VALIDADO);
                documentoAdjuntoRepository.save(d);
            }
        });
        // 2) notificar a Originación que ya validó TODOS los documentos
        DetalleSolicitudResponseDTO det = originacionClient.obtenerDetalle(numeroSolicitud);
        originacionClient.cambiarEstado(
                det.getIdSolicitud(),
                "DOCUMENTACION_VALIDADA",
                "Se validaron todos los documentos",
                usuario);

        // a) obtener idCliente
        coreBancarioClient
                .consultarPersonaPorIdentificacion("CEDULA", det.getCedulaSolicitante());

        // (3b) Recupera el cliente ya creado en el micro de Clientes
        List<ClienteDTO> clientes = clientesClient
                .findByIdentificacion("CEDULA", det.getCedulaSolicitante());
        if (clientes.isEmpty()) {
            throw new IllegalStateException("No existe cliente con esta cédula " + det.getCedulaSolicitante());
        }
        String idCliente = clientes.get(0).getId();

        // b) construir request
        CrearPrestamoRequest creq = new CrearPrestamoRequest(
                idCliente,
                det.getIdPrestamo(),
                det.getMontoSolicitado(),
                det.getPlazoMeses());

        // c) llamas al cliente Feign que expone el POST /prestamos
        prestamoClient.crearPrestamo(creq);
    }

    public void cargarTodosYMarcar(
            String numeroSolicitud,
            List<MultipartFile> archivos,
            List<String> tiposDocumento) {
        if (archivos.size() != tiposDocumento.size()) {
            throw new IllegalArgumentException("Debe enviar la misma cantidad de archivos que de tipos");
        }

        for (int i = 0; i < archivos.size(); i++) {
            MultipartFile file = archivos.get(i);
            String tipo = tiposDocumento.get(i);

            // 1) Validar tipo
            TipoDocumentoEnum.valueOf(tipo); // lanzará IllegalArgumentException si no existe

            // 2) Almacenar en S3 / sistema de ficheros
            String rutaStorage = fileStorageService.storeFile(file, numeroSolicitud, tipo);

            // 3) Extraer nombre de archivo
            String nombreArchivo = Paths.get(rutaStorage).getFileName().toString();

            // 4) Crear DTO
            DocumentoAdjuntoDTO dto = new DocumentoAdjuntoDTO(
                    numeroSolicitud,
                    tipo,
                    nombreArchivo,
                    rutaStorage,
                    LocalDateTime.now(),
                    1L,
                    EstadoDocumentoEnum.CARGADO);

            // 5) Mapear a entidad y setear estado
            DocumentoAdjunto entidad = documentoAdjuntoMapper.toEntity(dto);
            entidad.setEstado(EstadoDocumentoEnum.CARGADO);

            // 6) Guardar en Mongo
            documentoAdjuntoRepository.save(entidad);

            log.info("Documento tipo={} cargado y marcado en estado CARGADO para solicitud={}",
                    tipo, numeroSolicitud);

            // 7) Llamar al servicio de Originación para marcar como cargado
            DetalleSolicitudResponseDTO detalle = originacionClient.obtenerDetalle(numeroSolicitud);
            originacionClient.cambiarEstado(
                    detalle.getIdSolicitud(),
                    "DOCUMENTACION_CARGADA",
                    "Se subieron los 3 documentos",
                    "vendedor" // o toma el usuario autenticado
            );
            log.info("Solicitud {} marcada DOCUMENTACION_CARGADA", numeroSolicitud);
        }
    }

    public void eliminarDocumento(String numeroSolicitud, String idDocumento) {
        DocumentoAdjunto documento = documentoAdjuntoRepository.findById(idDocumento)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado"));

        if (!documento.getNumeroSolicitud().equals(numeroSolicitud)) {
            throw new CreateEntityException("DocumentoAdjunto",
                    "El documento no pertenece a la solicitud especificada");
        }

        // Eliminar archivo físico
        fileStorageService.deleteFile(documento.getRutaStorage());

        // Eliminar registro de la base de datos
        documentoAdjuntoRepository.delete(documento);
        log.info("Documento eliminado exitosamente: {} de solicitud {}", idDocumento, numeroSolicitud);
    }

    public void eliminarDocumentosPorSolicitud(String numeroSolicitud) {
        List<DocumentoAdjunto> documentos = documentoAdjuntoRepository.findByNumeroSolicitud(numeroSolicitud);

        // Eliminar archivos físicos
        documentos.forEach(doc -> fileStorageService.deleteFile(doc.getRutaStorage()));

        // Eliminar registros de la base de datos
        documentoAdjuntoRepository.deleteByNumeroSolicitud(numeroSolicitud);
        log.info("Documentos eliminados exitosamente para solicitud: {}", numeroSolicitud);
    }


}