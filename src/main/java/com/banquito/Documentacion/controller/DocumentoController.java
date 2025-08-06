package com.banquito.Documentacion.controller;

import com.banquito.Documentacion.dto.DocumentoAdjuntoResponseDTO;
import com.banquito.Documentacion.dto.RechazoDocumentoRequestDTO;
import com.banquito.Documentacion.service.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitudes/{numeroSolicitud}/documentos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Documentos de Solicitudes", description = "Operaciones para la gestión de documentos de solicitudes de crédito")
public class DocumentoController {

    private final DocumentoService documentoService;


    @Operation(summary = "Cargar documento a solicitud", description = "Sube y valida un documento PDF para la solicitud (máximo 20MB). Tipos válidos: CEDULA_IDENTIDAD, ROL_PAGOS, ESTADO_CUENTA_BANCARIA")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Documento cargado exitosamente", content = @Content(schema = @Schema(implementation = DocumentoAdjuntoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Archivo inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content)
    })

    @PostMapping
    public ResponseEntity<DocumentoAdjuntoResponseDTO> cargarDocumento(
            @PathVariable String numeroSolicitud,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipoDocumento") String tipoDocumento) {
        // 1) sube el doc
        var dto = documentoService.cargarDocumento(numeroSolicitud, archivo, tipoDocumento);

        // si ya subimos el tercer documento, avisamos al service
        if (documentoService.countPorSolicitud(numeroSolicitud) == 3) {
            documentoService.notificarDocumentacionCargada(numeroSolicitud);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "Descargar documento", description = "Descarga un documento específico de la solicitud")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento descargado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado", content = @Content)
    })
    @GetMapping("/{idDocumento}/descargar")
    public ResponseEntity<Resource> descargarDocumento(
            @Parameter(description = "Número de la solicitud", required = true) @PathVariable String numeroSolicitud,
            @Parameter(description = "ID del documento", required = true) @PathVariable String idDocumento) {
        log.info("Descargando documento {} de solicitud {}", idDocumento, numeroSolicitud);
        Resource resource = documentoService.descargarDocumento(numeroSolicitud, idDocumento);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @Operation(summary = "Ver documento", description = "Visualiza un documento específico de la solicitud en el navegador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento visualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado", content = @Content)
    })
    @GetMapping("/{idDocumento}/ver")
    public ResponseEntity<Resource> verDocumento(
            @Parameter(description = "Número de la solicitud", required = true) @PathVariable String numeroSolicitud,
            @Parameter(description = "ID del documento", required = true) @PathVariable String idDocumento) {
        log.info("Visualizando documento {} de solicitud {}", idDocumento, numeroSolicitud);
        Resource resource = documentoService.descargarDocumento(numeroSolicitud, idDocumento);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @Operation(summary = "Listar documentos de solicitud", description = "Obtiene la lista de documentos cargados para una solicitud")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<DocumentoAdjuntoResponseDTO>> listarDocumentos(
            @Parameter(description = "Número de la solicitud", required = true) @PathVariable String numeroSolicitud) {
        log.info("Listando documentos de solicitud {}", numeroSolicitud);
        List<DocumentoAdjuntoResponseDTO> documentos = documentoService.listarDocumentos(numeroSolicitud);
        return ResponseEntity.ok(documentos);
    }

    @Operation(summary = "Eliminar documento", description = "Elimina un documento específico de la solicitud")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Documento eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado", content = @Content)
    })
    @DeleteMapping("/{idDocumento}")
    public ResponseEntity<Void> eliminarDocumento(
            @Parameter(description = "Número de la solicitud", required = true) @PathVariable String numeroSolicitud,
            @Parameter(description = "ID del documento", required = true) @PathVariable String idDocumento) {
        log.info("Eliminando documento {} de solicitud {}", idDocumento, numeroSolicitud);
        documentoService.eliminarDocumento(numeroSolicitud, idDocumento);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Validar documento", description = "Marca un documento como validado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento validado exitosamente", content = @Content(schema = @Schema(implementation = DocumentoAdjuntoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado", content = @Content)
    })
    @PatchMapping("/{idDocumento}/validar")
    public ResponseEntity<DocumentoAdjuntoResponseDTO> validarDocumento(
            @PathVariable String numeroSolicitud,
            @PathVariable String idDocumento) {
        DocumentoAdjuntoResponseDTO dto = documentoService.validarDocumento(numeroSolicitud, idDocumento);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Validar todos los documentos de una solicitud", description = "Marca todos los documentos de una solicitud como validados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Todos los documentos validados exitosamente"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content)
    })

    @PatchMapping("/validar-todos")
    public ResponseEntity<Void> validarTodos(
            @PathVariable String numeroSolicitud,
            @RequestParam String usuario) {
       

        documentoService.validarTodos(numeroSolicitud, usuario);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Rechazar documento", description = "Marca un documento como rechazado con una observación obligatoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento rechazado exitosamente", content = @Content(schema = @Schema(implementation = DocumentoAdjuntoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Observación inválida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado", content = @Content)
    })
    @PatchMapping("/{idDocumento}/rechazar")
    public ResponseEntity<DocumentoAdjuntoResponseDTO> rechazarDocumento(
            @PathVariable String numeroSolicitud,
            @PathVariable String idDocumento,
            @Valid @RequestBody RechazoDocumentoRequestDTO solicitud) {
        // Aquí recibimos la observación desde el cuerpo de la petición
        DocumentoAdjuntoResponseDTO dto = documentoService.rechazarDocumento(numeroSolicitud, idDocumento,
                solicitud.getObservacion());
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Eliminar todos los documentos de una solicitud", description = "Elimina todos los documentos asociados a una solicitud")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Documentos eliminados exitosamente"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content)
    })
    @DeleteMapping
    public ResponseEntity<Void> eliminarDocumentosPorSolicitud(
            @Parameter(description = "Número de la solicitud", required = true) @PathVariable String numeroSolicitud) {
        log.info("Eliminando todos los documentos de solicitud {}", numeroSolicitud);
        documentoService.eliminarDocumentosPorSolicitud(numeroSolicitud);
        return ResponseEntity.noContent().build();
    }

    // DocumentoController.java

    // en DocumentoController.java
    @PatchMapping("/contratos-cargados")
    public ResponseEntity<Void> contratosCargados(
            @PathVariable String numeroSolicitud,
            @RequestParam String usuario) {
        documentoService.notificarContratoCargado(numeroSolicitud, usuario);
        return ResponseEntity.ok().build();
    }

}