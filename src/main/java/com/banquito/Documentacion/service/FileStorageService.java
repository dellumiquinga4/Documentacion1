package com.banquito.Documentacion.service;

import com.banquito.Documentacion.exception.CreateEntityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URL;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jakarta.annotation.PostConstruct;

@Service
@Slf4j
public class FileStorageService {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.access-key-id}")
    private String accessKeyId;

    @Value("${aws.s3.secret-access-key}")
    private String secretAccessKey;

    @Value("${aws.s3.endpoint-url:}")
    private String endpointUrl;

    private S3Client s3Client;
    private S3Presigner s3Presigner;

    @PostConstruct
    public void initializeS3() {
        // Validar que las credenciales de AWS estén configuradas
        if (accessKeyId == null || accessKeyId.isEmpty() || secretAccessKey == null || secretAccessKey.isEmpty()) {
            throw new RuntimeException(
                    "Credenciales de AWS S3 no configuradas. Es obligatorio configurar AWS_ACCESS_KEY_ID y AWS_SECRET_ACCESS_KEY");
        }

        try {
            this.s3Client = createS3Client();
            this.s3Presigner = createS3Presigner();
            log.info("S3 client inicializado exitosamente para bucket: {}", bucketName);
        } catch (Exception e) {
            log.error("Error al inicializar S3 client: {}", e.getMessage());
            throw new RuntimeException(
                    "No se pudo inicializar S3 client. Verifique las credenciales y configuración de AWS", e);
        }
    }

    private S3Client createS3Client() {
        S3ClientBuilder builder = S3Client.builder()
                .region(software.amazon.awssdk.regions.Region.of(region));

        builder.credentialsProvider(
                () -> software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create(accessKeyId, secretAccessKey));

        if (endpointUrl != null && !endpointUrl.isEmpty()) {
            builder.endpointOverride(java.net.URI.create(endpointUrl));
        }

        return builder.build();
    }

    private S3Presigner createS3Presigner() {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(software.amazon.awssdk.regions.Region.of(region));

        builder.credentialsProvider(
                () -> software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create(accessKeyId, secretAccessKey));

        if (endpointUrl != null && !endpointUrl.isEmpty()) {
            builder.endpointOverride(java.net.URI.create(endpointUrl));
        }

        return builder.build();
    }

    public String storeFile(MultipartFile file, String numeroSolicitud, String tipoDocumento) {
        try {
            if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                throw new CreateEntityException("FileStorage", "Solo se permiten archivos PDF");
            }

            if (file.getSize() > 20 * 1024 * 1024) {
                throw new CreateEntityException("FileStorage", "El archivo no debe superar los 20MB");
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = tipoDocumento + "_" + timestamp + ".pdf";
            String s3Key = "solicitud_" + numeroSolicitud + "/" + fileName;

            // Subir archivo a S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType("application/pdf")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Archivo guardado exitosamente en S3: s3://{}/{}", bucketName, s3Key);
            return s3Key;

        } catch (S3Exception e) {
            log.error("Error al subir archivo a S3: {}", e.getMessage());
            throw new CreateEntityException("FileStorage", "Error al subir archivo a S3: " + e.getMessage());
        } catch (IOException ex) {
            throw new CreateEntityException("FileStorage", "No se pudo procesar el archivo: " + ex.getMessage());
        }
    }

    public Resource loadFileAsResource(String s3Key) {
        try {
            // Generar URL presignada para acceso temporal
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(60)) // URL válida por 1 hora
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Key)
                            .build())
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            URL presignedUrl = presignedRequest.url();

            log.info("URL presignada generada para S3: {}", presignedUrl);
            return new UrlResource(presignedUrl);

        } catch (S3Exception e) {
            log.error("Error al generar URL presignada para S3: {}", e.getMessage());
            throw new CreateEntityException("FileStorage", "Error al acceder al archivo en S3: " + e.getMessage());
        }
    }

    public void deleteFile(String s3Key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Archivo eliminado de S3: s3://{}/{}", bucketName, s3Key);

        } catch (S3Exception e) {
            log.error("Error al eliminar archivo de S3: {}", e.getMessage());
            throw new CreateEntityException("FileStorage", "Error al eliminar archivo de S3: " + e.getMessage());
        }
    }

    /**
     * Método para cerrar los recursos de S3 cuando la aplicación se detenga
     */
    public void close() {
        try {
            if (s3Client != null) {
                s3Client.close();
            }
            if (s3Presigner != null) {
                s3Presigner.close();
            }
        } catch (Exception e) {
            log.warn("Error al cerrar recursos de S3: {}", e.getMessage());
        }
    }
}