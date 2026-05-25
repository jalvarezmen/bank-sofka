# banking-api (bank-sofka)

API REST bancaria — prueba técnica Junior Sofka. Java 17, Spring Boot 3.x, SQL Server.

## Descripción

Sistema bancario expuesto como **API REST** con arquitectura monolítica por capas (Controller → Service → Repository → JPA). Permite gestionar el ciclo de vida de clientes, sus cuentas y los movimientos de dinero, además de generar un reporte de estado de cuenta por cliente y rango de fechas.

### Funcionalidades principales

| Recurso | Ruta base | Qué hace |
|---------|-----------|----------|
| **Clientes** | `/api/clientes` | Alta, consulta, actualización y baja **lógica** (`estado=false`) de personas que usan el banco. |
| **Cuentas** | `/api/cuentas` | Creación y administración de cuentas (ahorro/corriente) asociadas a un cliente, con saldo inicial y saldo disponible. |
| **Movimientos** | `/api/movimientos` | Registro de **depósitos** (valor positivo) y **retiros** (valor negativo). Los movimientos son **inmutables** (solo lectura y alta; no PUT ni DELETE). |
| **Reportes** | `/api/reportes` | Estado de cuenta: movimientos de todas las cuentas activas de un cliente entre `fechaInicio` y `fechaFin`. |

### Reglas de negocio relevantes

- Un retiro solo se acepta si el **saldo disponible** de la cuenta lo permite; si no, responde **400** con el mensaje *"Saldo no disponible"*.
- Clientes y cuentas eliminados no se borran físicamente: quedan inactivos y dejan de aparecer en los listados.
- Las respuestas de la API usan **DTOs** (no se exponen entidades JPA directamente).
- Errores uniformes en JSON (`timestamp`, `status`, `mensaje`, `path`) vía `GlobalExceptionHandler`.

### Datos de ejemplo (PDF / enunciado)

El script `src/main/resources/BaseDatos.sql` carga clientes de prueba (Jose Lema, Marianela Montalvo, Juan Osorio), cuatro cuentas y movimientos con fechas en **febrero 2022** para validar el reporte de Marianela. Conviene ejecutarlo antes de probar con Postman si la base ya fue modificada.

### Casos de uso del enunciado cubiertos

1. **CRUD de clientes** — crear, listar, actualizar y desactivar clientes.  
2. **CRUD de cuentas** — asociar cuentas a clientes y mantener saldos.  
3. **Movimientos** — registrar depósitos/retiros y validar saldo.  
4. **Reporte** — consulta por `clienteId` y rango de fechas (`GET /api/reportes?fechaInicio=...&fechaFin=...&clienteId=...`).

## Requisitos

- JDK 17+
- Maven 3.9+
- Docker (opcional, para SQL Server)

## Ejecución local

```bash
mvn clean install
mvn spring-boot:run
```

Base URL: `http://localhost:8080/api`

**Swagger UI:** [http://localhost:8080/api/swagger-ui/index.html](http://localhost:8080/api/swagger-ui/index.html)  
**OpenAPI JSON:** `http://localhost:8080/api/v3/api-docs`

Variables de entorno (opcionales): `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

Sin SQL Server local, los tests usan H2 en memoria (`mvn test`).

## Docker

```bash
docker-compose up --build
```

Tras levantar SQL Server, ejecuta manualmente `src/main/resources/BaseDatos.sql` si necesitas los datos semilla (o deja que Hibernate cree el esquema con `ddl-auto=update`).

## Postman

Importa `banking-api.postman_collection.json` en Postman. La colección está ordenada en carpetas (**01 solo lectura → 02 movimientos/reporte → 03 CRUD**). Tras ejecutar pruebas que alteran datos, vuelve a cargar `BaseDatos.sql`.

```powershell
Get-Content src\main\resources\BaseDatos.sql -Raw | docker exec -i banking-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Banking@2024" -C
```

Pruebas automatizadas de endpoints (con Docker en marcha): `powershell -File scripts\test-endpoints.ps1`

## Documentación del proyecto

- [ARCHITECTURE.md](ARCHITECTURE.md) — diseño y endpoints
- [CONVENTIONS.md](CONVENTIONS.md) — reglas de código
- [TASKS.md](TASKS.md) — tareas de implementación ordenadas
- [.cursorrules](.cursorrules) — reglas para el agente en Cursor
