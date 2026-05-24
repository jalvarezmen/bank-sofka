# banking-api (bank-sofka)

API REST bancaria — prueba técnica Junior. Java 17, Spring Boot 3.x, SQL Server.

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

Variables de entorno (opcionales): `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

## Documentación del proyecto

- [ARCHITECTURE.md](ARCHITECTURE.md) — diseño y endpoints
- [CONVENTIONS.md](CONVENTIONS.md) — reglas de código
- [TASKS.md](TASKS.md) — tareas de implementación ordenadas
- [.cursorrules](.cursorrules) — reglas para el agente en Cursor
