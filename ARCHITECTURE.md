# ARCHITECTURE.md
# Proyecto: Sistema Bancario — Prueba Técnica Junior
# Stack: Java 17 + Spring Boot 3.x + SQL Server + Docker

---

## 1. VISIÓN GENERAL

Este proyecto es una **API REST monolítica por capas** (apropiada para perfil Junior).
Expone 3 recursos principales: `/clientes`, `/cuentas`, `/movimientos`.

No se requiere separación en microservicios (eso es SemiSenior/Senior).

---

## 2. ESTRUCTURA DE CARPETAS

```
banking-api/
├── src/
│   └── main/
│       ├── java/com/bank/api/
│       │   ├── BankingApiApplication.java       ← Entry point
│       │   │
│       │   ├── config/
│       │   │   └── GlobalExceptionHandler.java  ← @ControllerAdvice
│       │   │
│       │   ├── model/                           ← Entidades JPA
│       │   │   ├── Persona.java                 ← @MappedSuperclass
│       │   │   ├── Cliente.java                 ← @Entity hereda Persona
│       │   │   ├── Cuenta.java                  ← @Entity
│       │   │   └── Movimiento.java              ← @Entity
│       │   │
│       │   ├── repository/                      ← Patrón Repository (JPA)
│       │   │   ├── ClienteRepository.java
│       │   │   ├── CuentaRepository.java
│       │   │   └── MovimientoRepository.java
│       │   │
│       │   ├── service/                         ← Lógica de negocio
│       │   │   ├── ClienteService.java          ← interface
│       │   │   ├── ClienteServiceImpl.java
│       │   │   ├── CuentaService.java
│       │   │   ├── CuentaServiceImpl.java
│       │   │   ├── MovimientoService.java
│       │   │   └── MovimientoServiceImpl.java
│       │   │
│       │   ├── controller/                      ← Endpoints REST
│       │   │   ├── ClienteController.java
│       │   │   ├── CuentaController.java
│       │   │   ├── MovimientoController.java
│       │   │   └── ReporteController.java
│       │   │
│       │   ├── dto/                             ← Data Transfer Objects
│       │   │   ├── ClienteDTO.java
│       │   │   ├── CuentaDTO.java
│       │   │   ├── MovimientoDTO.java
│       │   │   └── ReporteDTO.java
│       │   │
│       │   └── exception/                       ← Excepciones personalizadas
│       │       ├── SaldoInsuficienteException.java
│       │       ├── RecursoNoEncontradoException.java
│       │       └── ErrorResponse.java           ← Wrapper JSON de errores
│       │
│       └── resources/
│           ├── application.yml                  ← Config Spring Boot
│           └── BaseDatos.sql                    ← Script DDL entregable
│
├── src/test/java/com/bank/api/
│   ├── service/
│   │   └── MovimientoServiceTest.java           ← Prueba unitaria 1
│   └── controller/
│       └── ClienteControllerTest.java           ← Prueba unitaria 2
│
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

---

## 3. MODELO DE DATOS

### Relaciones entre entidades

```
Persona (superclase, no tiene tabla propia)
    └── Cliente (@Entity, tabla: clientes)
            └── tiene muchas → Cuenta (tabla: cuentas)
                                    └── tiene muchos → Movimiento (tabla: movimientos)
```

### Diagrama de tablas SQL Server

```sql
-- CLIENTES (hereda campos de Persona)
clientes:
  cliente_id      BIGINT IDENTITY PK
  nombre          VARCHAR(100) NOT NULL
  genero          VARCHAR(20)
  edad            INT
  identificacion  VARCHAR(20) UNIQUE NOT NULL
  direccion       VARCHAR(200)
  telefono        VARCHAR(20)
  contrasena      VARCHAR(100) NOT NULL
  estado          BIT NOT NULL DEFAULT 1

-- CUENTAS
cuentas:
  id              BIGINT IDENTITY PK
  numero_cuenta   VARCHAR(20) UNIQUE NOT NULL
  tipo_cuenta     VARCHAR(20) NOT NULL   -- 'Ahorro' | 'Corriente'
  saldo_inicial   DECIMAL(18,2) NOT NULL DEFAULT 0
  saldo_disponible DECIMAL(18,2) NOT NULL DEFAULT 0
  estado          BIT NOT NULL DEFAULT 1
  cliente_id      BIGINT FK → clientes(cliente_id)

-- MOVIMIENTOS
movimientos:
  id              BIGINT IDENTITY PK
  fecha           DATETIME NOT NULL DEFAULT GETDATE()
  tipo_movimiento VARCHAR(20) NOT NULL   -- 'Deposito' | 'Retiro'
  valor           DECIMAL(18,2) NOT NULL -- positivo=depósito, negativo=retiro
  saldo           DECIMAL(18,2) NOT NULL -- saldo resultante después del movimiento
  cuenta_id       BIGINT FK → cuentas(id)
```

---

## 4. PRINCIPIOS SOLID APLICADOS

### S — Single Responsibility
- Cada clase tiene UNA responsabilidad:
  - `Controller` → solo recibe/responde HTTP
  - `Service` → solo contiene lógica de negocio
  - `Repository` → solo accede a la base de datos
  - `DTO` → solo transporta datos entre capas

### O — Open/Closed
- Los servicios se definen como **interfaces** (`ClienteService`)
- Las implementaciones (`ClienteServiceImpl`) pueden extenderse sin modificar la interfaz
- `GlobalExceptionHandler` es abierto a nuevas excepciones sin modificar las existentes

### L — Liskov Substitution
- `Cliente` extiende `Persona` correctamente usando `@MappedSuperclass`
- `ClienteServiceImpl` puede reemplazar a `ClienteService` sin romper el contrato

### I — Interface Segregation
- Cada servicio tiene su propia interfaz específica
- No existe una interfaz "dios" que agrupe todo

### D — Dependency Inversion
- Los controladores dependen de la **interfaz** `ClienteService`, NO de `ClienteServiceImpl`
- Spring inyecta la implementación vía `@Autowired` / constructor injection

---

## 5. PATRONES DE DISEÑO APLICADOS

| Patrón | Dónde | Por qué |
|--------|-------|---------|
| Repository | `*Repository.java` | Abstrae el acceso a datos, facilita testing |
| DTO | `dto/` package | Evita exponer entidades JPA directamente en la API |
| Service Layer | `*Service.java` | Centraliza la lógica de negocio |
| Exception Handler | `GlobalExceptionHandler` | Manejo uniforme y limpio de errores |

---

## 6. FLUJO DE UNA PETICIÓN HTTP

```
HTTP Request
    ↓
[Controller]        ← Valida entrada con @Valid, mapea a DTO
    ↓
[Service]           ← Ejecuta lógica de negocio, lanza excepciones si aplica
    ↓
[Repository]        ← Consulta/persiste en BD con JPA
    ↓
[Base de Datos]     ← SQL Server
    ↑
[Service]           ← Mapea entidad a DTO de respuesta
    ↑
[Controller]        ← Retorna ResponseEntity con código HTTP correcto
    ↑
HTTP Response (JSON)
```

---

## 7. MANEJO DE ERRORES

Todas las excepciones se centralizan en `GlobalExceptionHandler.java` (@ControllerAdvice).

### Estructura de respuesta de error (JSON):
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "mensaje": "Saldo no disponible",
  "path": "/api/movimientos"
}
```

### Tabla de excepciones:
| Excepción | HTTP Status | Mensaje |
|-----------|-------------|---------|
| `SaldoInsuficienteException` | 400 | "Saldo no disponible" |
| `RecursoNoEncontradoException` | 404 | "Recurso no encontrado: {id}" |
| `MethodArgumentNotValidException` | 400 | Detalle de campos inválidos |
| `Exception` (genérica) | 500 | "Error interno del servidor" |

---

## 8. ENDPOINTS COMPLETOS

### Base URL: `http://localhost:8080/api`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/clientes` | Listar todos los clientes |
| GET | `/clientes/{id}` | Obtener cliente por ID |
| POST | `/clientes` | Crear cliente |
| PUT | `/clientes/{id}` | Actualizar cliente completo |
| PATCH | `/clientes/{id}` | Actualizar cliente parcial |
| DELETE | `/clientes/{id}` | Eliminar cliente |
| GET | `/cuentas` | Listar todas las cuentas |
| GET | `/cuentas/{id}` | Obtener cuenta por ID |
| POST | `/cuentas` | Crear cuenta |
| PUT | `/cuentas/{id}` | Actualizar cuenta |
| DELETE | `/cuentas/{id}` | Eliminar cuenta |
| GET | `/movimientos` | Listar todos los movimientos |
| GET | `/movimientos/{id}` | Obtener movimiento por ID |
| POST | `/movimientos` | Registrar movimiento (con lógica de saldo) |
| PUT | `/movimientos/{id}` | Actualizar movimiento |
| DELETE | `/movimientos/{id}` | Eliminar movimiento |
| GET | `/reportes?fechaInicio=yyyy-MM-dd&fechaFin=yyyy-MM-dd&clienteId={id}` | Reporte estado de cuenta |

---

## 9. CONFIGURACIÓN DOCKER

### Servicios en docker-compose:
- `banking-api` → aplicación Spring Boot (puerto 8080)
- `sqlserver` → SQL Server 2022 (puerto 1433)

### Variables de entorno requeridas:
```
SPRING_DATASOURCE_URL=jdbc:sqlserver://sqlserver:1433;databaseName=banking_db
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=Banking@2024
```

---

## 10. DEPENDENCIAS MAVEN (pom.xml)

```xml
<!-- Spring Boot Starter Web -->
<!-- Spring Boot Starter Data JPA -->
<!-- Spring Boot Starter Validation -->
<!-- Microsoft SQL Server JDBC Driver: com.microsoft.sqlserver:mssql-jdbc -->
<!-- Spring Boot Starter Test (JUnit 5 + Mockito) -->
<!-- Lombok (opcional, para reducir boilerplate) -->
```
