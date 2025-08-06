# Microservicio de Documentación - Banco Banquito

Este microservicio se encarga de la gestión de documentos adjuntos para las solicitudes de crédito del Banco Banquito.

## Características

- **Almacenamiento de documentos**: Almacenamiento obligatorio en AWS S3
- **Base de datos**: MongoDB para almacenar metadatos de documentos
- **API REST**: Endpoints para cargar, descargar, listar y eliminar documentos
- **Validación**: Validación de tipos de archivo (solo PDF) y tamaño máximo (20MB)
- **Documentación**: API documentada con OpenAPI/Swagger
- **Logging**: Logs detallados para auditoría y debugging

## Tecnologías Utilizadas

- **Spring Boot 3.5.4**
- **Spring Data MongoDB**
- **Spring Security**
- **MapStruct** para mapeo de objetos
- **Lombok** para reducir código boilerplate
- **AWS SDK** para integración con S3
- **OpenAPI/Swagger** para documentación
- **Java 21**

## Configuración

### Variables de Entorno OBLIGATORIAS

```properties
# MongoDB
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=documentacion_db

# AWS S3 (OBLIGATORIO - sin estas credenciales la aplicación no iniciará)
AWS_ACCESS_KEY_ID=tu_access_key
AWS_SECRET_ACCESS_KEY=tu_secret_key
AWS_ENDPOINT_URL=tu_endpoint_url (opcional)

# Puerto del servidor
server.port=8084
```

### Configuración de AWS S3

**IMPORTANTE**: Las credenciales de AWS S3 son **OBLIGATORIAS**. Si no se configuran, la aplicación no iniciará y mostrará un error.

## Endpoints de la API

### Documentos

- `POST /api/v1/solicitudes/{numeroSolicitud}/documentos` - Cargar documento
- `GET /api/v1/solicitudes/{numeroSolicitud}/documentos` - Listar documentos
- `GET /api/v1/solicitudes/{numeroSolicitud}/documentos/{idDocumento}/descargar` - Descargar documento
- `GET /api/v1/solicitudes/{numeroSolicitud}/documentos/{idDocumento}/ver` - Ver documento
- `DELETE /api/v1/solicitudes/{numeroSolicitud}/documentos/{idDocumento}` - Eliminar documento
- `DELETE /api/v1/solicitudes/{numeroSolicitud}/documentos` - Eliminar todos los documentos

### Utilidades

- `GET /swagger-ui.html` - Documentación de la API

## Tipos de Documentos Soportados

- `CEDULA_IDENTIDAD` - Cédula de Identidad
- `ROL_PAGOS` - Rol de Pagos
- `ESTADO_CUENTA_BANCARIA` - Estado de Cuenta Bancaria

## Ejecución

### Requisitos Previos

- Java 21
- Maven 3.6+
- MongoDB 4.4+ (opcional, se puede usar MongoDB Atlas)
- **AWS S3 configurado con credenciales válidas**

### Comandos

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar tests
mvn test

# Ejecutar la aplicación
mvn spring-boot:run

# Construir JAR
mvn clean package
```

### Docker

```bash
# Construir imagen
docker build -t banquito-documentacion .

# Ejecutar contenedor (asegúrate de pasar las variables de entorno de AWS)
docker run -p 8084:8084 \
  -e AWS_ACCESS_KEY_ID=tu_access_key \
  -e AWS_SECRET_ACCESS_KEY=tu_secret_key \
  banquito-documentacion
```

## Estructura del Proyecto

```
src/main/java/com/banquito/documentacion/
├── config/                 # Configuraciones
├── controller/             # Controladores REST
├── dto/                   # Objetos de transferencia de datos
├── enums/                 # Enumeraciones
├── exception/             # Manejo de excepciones
├── mapper/                # Mapeadores MapStruct
├── model/                 # Entidades de MongoDB
├── repository/            # Repositorios de datos
└── service/               # Lógica de negocio
```

## Base de Datos

### Colección: documentos_adjuntos

```json
{
  "_id": "ObjectId",
  "numeroSolicitud": "String",
  "tipoDocumento": "String",
  "nombreArchivo": "String",
  "rutaStorage": "String (S3 key)",
  "fechaCarga": "LocalDateTime",
  "fechaActualizacion": "LocalDateTime",
  "version": "Long"
}
```

## Logs

El servicio genera logs detallados para:
- Carga de documentos a S3
- Descarga de documentos desde S3
- Errores de validación
- Operaciones de almacenamiento en S3
- Errores de S3

## Monitoreo

- Logs estructurados para análisis
- URLs presignadas de S3 para acceso temporal

## Seguridad

- CSRF deshabilitado para APIs REST
- Endpoints de documentación públicos
- Validación de entrada en todos los endpoints
- Sanitización de nombres de archivo
- URLs presignadas de S3 con expiración

## Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles. 