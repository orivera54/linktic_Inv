# Microservicio de Inventario

Microservicio Spring Boot para la gestión de inventario de productos con implementación de JSON API, Docker, MySQL y comunicación con microservicio de productos.

## 🚀 Características

- ✅ **JSON API** - Implementación completa del estándar JSON API
- ✅ **Docker** - Containerización optimizada con multi-stage build
- ✅ **MySQL** - Base de datos relacional con triggers y procedimientos almacenados
- ✅ **Swagger/OpenAPI** - Documentación automática de la API
- ✅ **Circuit Breaker** - Patrón de resiliencia con Resilience4j
- ✅ **Retry Pattern** - Reintentos automáticos para llamadas externas
- ✅ **API Key Authentication** - Autenticación entre servicios
- ✅ **Health Checks** - Monitoreo de salud del servicio
- ✅ **Logging Estructurado** - Logs organizados y trazables
- ✅ **Pruebas Unitarias** - Cobertura ≥ 80%
- ✅ **Historial de Compras** - Registro de operaciones (opcional)

## 🏗️ Arquitectura

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Cliente       │    │  Inventario      │    │  Productos      │
│   (Frontend)    │◄──►│  Service         │◄──►│  Service        │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌──────────────────┐
                       │  MySQL Database  │
                       │  (ltprods)       │
                       └──────────────────┘
```

## 📋 Prerrequisitos

- Java 17 o superior
- Maven 3.6+
- Docker y Docker Compose
- MySQL 8.0 (opcional, se incluye en docker-compose)

## 🛠️ Instalación

### Opción 1: Docker Compose (Recomendado)

1. **Clonar el repositorio:**
```bash
git clone <repository-url>
cd inventario-service
```

2. **Configurar variables de entorno:**
```bash
# Editar docker-compose.yml y actualizar:
# - APP_PRODUCTOS_SERVICE_BASE_URL
# - APP_PRODUCTOS_SERVICE_API_KEY
# - APP_API_KEY
```

3. **Ejecutar con Docker Compose:**
```bash
docker-compose up -d
```

4. **Verificar que los servicios estén funcionando:**
```bash
docker-compose ps
```

### Opción 2: Desarrollo Local

1. **Configurar base de datos MySQL:**
```sql
CREATE DATABASE ltprods;
-- Ejecutar el script init.sql
```

2. **Configurar application.yml:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ltprods
    username: root
    password: Valery2005*

app:
  productos-service:
    base-url: http://localhost:8080
    api-key: tu-clave-de-api
  api-key: inventario-api-key
```

3. **Ejecutar la aplicación:**
```bash
mvn spring-boot:run
```

## 📚 API Documentation

### Swagger UI
- **URL:** http://localhost:8081/swagger-ui.html
- **API Docs:** http://localhost:8081/api-docs

### Endpoints Principales

#### 1. Consultar Cantidad Disponible
```http
GET /api/v1/inventario/{productoId}
X-API-Key: inventario-api-key
Accept: application/json
```

**Respuesta:**
```json
{
  "data": {
    "type": "inventario",
    "id": "1",
    "attributes": {
      "cantidad": 25,
      "fecha_creacion": "2024-01-15T10:30:00",
      "fecha_actualizacion": "2024-01-15T10:30:00"
    },
    "relationships": {
      "producto": {
        "data": {
          "type": "productos",
          "id": "1"
        }
      }
    }
  }
}
```

#### 2. Actualizar Cantidad
```http
PATCH /api/v1/inventario/{productoId}
X-API-Key: inventario-api-key
Content-Type: application/json

{
  "data": {
    "type": "inventario",
    "id": "1",
    "attributes": {
      "cantidad": 150
    }
  }
}
```

#### 3. Incrementar Cantidad (Compra)
```http
POST /api/v1/inventario/{productoId}/incrementar?cantidad=25&precioUnitario=10.50
X-API-Key: inventario-api-key
```

#### 4. Decrementar Cantidad (Venta)
```http
POST /api/v1/inventario/{productoId}/decrementar?cantidad=10&precioUnitario=15.00
X-API-Key: inventario-api-key
```

#### 5. Productos con Stock Bajo
```http
GET /api/v1/inventario/stock-bajo?cantidadMinima=10
X-API-Key: inventario-api-key
```

#### 6. Productos Sin Stock
```http
GET /api/v1/inventario/sin-stock
X-API-Key: inventario-api-key
```

#### 7. Estadísticas del Inventario
```http
GET /api/v1/inventario/estadisticas
X-API-Key: inventario-api-key
```

#### 8. Health Check
```http
GET /api/v1/inventario/health
```

## 🔧 Configuración

### Variables de Entorno

| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `SPRING_DATASOURCE_URL` | URL de conexión a MySQL | `jdbc:mysql://localhost:3306/ltprods` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de MySQL | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de MySQL | `Valery2005*` |
| `APP_PRODUCTOS_SERVICE_BASE_URL` | URL del servicio de productos | `http://localhost:8080` |
| `APP_PRODUCTOS_SERVICE_API_KEY` | API Key para servicio de productos | `tu-clave-de-api` |
| `APP_API_KEY` | API Key para este servicio | `inventario-api-key` |

### Configuración de Circuit Breaker

```yaml
spring:
  cloud:
    circuitbreaker:
      resilience4j:
        instances:
          productos-service:
            sliding-window-size: 10
            minimum-number-of-calls: 5
            failure-rate-threshold: 50
            wait-duration-in-open-state: 5000
            permitted-number-of-calls-in-half-open-state: 3
```

## 🧪 Pruebas

### Ejecutar Pruebas Unitarias
```bash
mvn test
```

### Ejecutar Pruebas con Cobertura
```bash
mvn clean test jacoco:report
```

### Verificar Cobertura
```bash
# Abrir en navegador:
# target/site/jacoco/index.html
```

## 📊 Monitoreo

### Health Checks
- **Endpoint:** `/actuator/health`
- **Health Check del Contenedor:** Verifica `/api/v1/inventario/health`

### Métricas
- **Endpoint:** `/actuator/metrics`
- **Prometheus:** `/actuator/prometheus`

### Logs
- **Archivo:** `logs/inventario-service.log`
- **Formato:** JSON estructurado
- **Nivel:** INFO por defecto

## 🐳 Docker

### Construir Imagen
```bash
docker build -t inventario-service .
```

### Ejecutar Contenedor
```bash
docker run -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/ltprods \
  -e APP_PRODUCTOS_SERVICE_BASE_URL=http://host.docker.internal:8080 \
  inventario-service
```

### Docker Compose
```bash
# Iniciar servicios
docker-compose up -d

# Ver logs
docker-compose logs -f inventario-service

# Detener servicios
docker-compose down
```

## 🔒 Seguridad

### Autenticación
- **Método:** API Key en header `X-API-Key`
- **Configuración:** Variable de entorno `APP_API_KEY`

### Comunicación entre Servicios
- **Protocolo:** HTTP/HTTPS
- **Autenticación:** API Key mutua
- **Timeout:** Configurable (default: 5s)
- **Retry:** Configurable (default: 3 intentos)

## 📈 Escalabilidad

### Configuración de JVM
```bash
# Variables de entorno para optimización
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"
```

### Base de Datos
- **Índices:** Optimizados para consultas frecuentes
- **Vistas:** Para reportes comunes
- **Procedimientos Almacenados:** Para operaciones complejas

## 🚨 Troubleshooting

### Problemas Comunes

1. **Error de conexión a MySQL:**
   ```bash
   # Verificar que MySQL esté ejecutándose
   docker-compose ps mysql
   
   # Ver logs de MySQL
   docker-compose logs mysql
   ```

2. **Error de comunicación con servicio de productos:**
   ```bash
   # Verificar que el servicio esté disponible
   curl -H "X-API-Key: tu-clave-de-api" \
        http://localhost:8080/api/v1/products/1
   ```

3. **Error de autenticación:**
   ```bash
   # Verificar API Key
   curl -H "X-API-Key: inventario-api-key" \
        http://localhost:8081/api/v1/inventario/health
   ```

### Logs de Debug
```yaml
logging:
  level:
    com.linktic.inventario: DEBUG
    org.springframework.web: DEBUG
```

## 🤝 Contribución

1. Fork el proyecto
2. Crear una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## 📞 Soporte

Para soporte técnico o preguntas:
- Crear un issue en el repositorio
- Contactar al equipo de desarrollo

---

**Desarrollado con ❤️ usando Spring Boot, Docker y MySQL**