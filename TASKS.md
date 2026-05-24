# TASKS.md
# Lista de Tareas para el Agente — Sistema Bancario Junior
# Orden de ejecución: de arriba hacia abajo. No saltar tareas.

---

## INSTRUCCIONES PARA EL AGENTE

- Ejecuta las tareas en el orden exacto indicado.
- Cada tarea tiene un CRITERIO DE ÉXITO. No avances hasta cumplirlo.
- Si una tarea depende de otra, espera a que la anterior esté completa.
- Usa siempre los nombres de clases, paquetes y variables definidos en ARCHITECTURE.md.
- Sigue las convenciones definidas en CONVENTIONS.md sin excepción.
- Ante cualquier duda de diseño, consulta ARCHITECTURE.md primero.

---

## FASE 0 — SETUP DEL PROYECTO

### TAREA 0.1 — Crear proyecto Spring Boot
**Prompt sugerido para el agente:**
> "Crea un proyecto Spring Boot 3.x con Java 17 usando Maven. El groupId es `com.bank`, el artifactId es `banking-api`. Agrega las dependencias: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, mssql-jdbc (com.microsoft.sqlserver:mssql-jdbc:12.4.2.jre11), spring-boot-starter-test, lombok."

**Criterio de éxito:** El proyecto compila con `mvn clean install` sin errores.

---

### TAREA 0.2 — Configurar application.yml
**Prompt sugerido:**
> "Configura el archivo `src/main/resources/application.yml` con la conexión a SQL Server usando variables de entorno. El servidor es `sqlserver`, base de datos `banking_db`, usuario `sa`, contraseña `Banking@2024`. Activa `spring.jpa.hibernate.ddl-auto: update` y `spring.jpa.show-sql: true`. El puerto del servidor es 8080. Agrega el prefijo de contexto `/api`."

**Criterio de éxito:** El archivo `application.yml` contiene datasource, jpa y server configurados.

---

## FASE 1 — MODELO (ENTIDADES JPA)

### TAREA 1.1 — Crear clase Persona
**Prompt sugerido:**
> "Crea la clase `Persona.java` en el paquete `com.bank.api.model`. Esta clase es un `@MappedSuperclass` (no tiene tabla propia). Campos: nombre (String), genero (String), edad (int), identificacion (String), direccion (String), telefono (String). Usa Lombok `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`. Todos los campos deben tener `@Column` con el nombre en snake_case."

**Criterio de éxito:** La clase compila. Tiene @MappedSuperclass. NO tiene @Entity ni @Table.

---

### TAREA 1.2 — Crear entidad Cliente
**Prompt sugerido:**
> "Crea la entidad `Cliente.java` en `com.bank.api.model`. Hereda de `Persona`. Anótala con `@Entity` y `@Table(name = 'clientes')`. Campos propios: clienteId (Long, @Id, @GeneratedValue IDENTITY), contrasena (String, @Column not null), estado (boolean, @Column not null, default true). Relación: un Cliente tiene muchas Cuentas (`@OneToMany(mappedBy='cliente', cascade=CascadeType.PERSIST, fetch=FetchType.LAZY)`). Usa Lombok."

**Criterio de éxito:** La entidad compila. Usa `CascadeType.PERSIST` (no ALL). clienteId es la PK.

---

### TAREA 1.3 — Crear entidad Cuenta
**Prompt sugerido:**
> "Crea la entidad `Cuenta.java` en `com.bank.api.model`. Anótala con `@Entity` y `@Table(name = 'cuentas')`. Campos: id (Long PK), numeroCuenta (String, unique, not null), tipoCuenta (String, not null), saldoInicial (BigDecimal, not null), saldoDisponible (BigDecimal, not null), estado (boolean). Relación ManyToOne con Cliente: `@ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name='cliente_id')`. Relación OneToMany con Movimiento. Usa Lombok."

**Criterio de éxito:** La entidad compila. Tiene relación bidireccional con Cliente y Movimiento.

---

### TAREA 1.4 — Crear entidad Movimiento
**Prompt sugerido:**
> "Crea la entidad `Movimiento.java` en `com.bank.api.model`. Anótala con `@Entity` y `@Table(name = 'movimientos')`. Campos: id (Long PK), fecha (LocalDateTime, @Column not null, default = now), tipoMovimiento (String, not null: 'Deposito' o 'Retiro'), valor (BigDecimal, not null — puede ser negativo), saldo (BigDecimal, not null — saldo resultante). Relación ManyToOne con Cuenta. Usa Lombok."

**Criterio de éxito:** La entidad compila. La relación con Cuenta está correctamente configurada.

---

## FASE 2 — REPOSITORIOS

### TAREA 2.1 — Crear repositorios JPA
**Prompt sugerido:**
> "Crea las 3 interfaces de repositorio en el paquete `com.bank.api.repository`, extendiendo `JpaRepository`:
> 1. `ClienteRepository` — agrega método: `Optional<Cliente> findByIdentificacion(String identificacion)`
> 2. `CuentaRepository` — agrega método: `Optional<Cuenta> findByNumeroCuenta(String numeroCuenta)`
> 3. `MovimientoRepository` — agrega método: `List<Movimiento> findByCuenta_ClienteClienteIdAndFechaBetween(Long clienteId, LocalDateTime fechaInicio, LocalDateTime fechaFin)` (para el reporte F4)"

**Criterio de éxito:** Las 3 interfaces extienden JpaRepository y compilan sin errores.

---

## FASE 3 — DTOs

### TAREA 3.1 — Crear DTOs de request y response
**Prompt sugerido:**
> "Crea los siguientes DTOs en `com.bank.api.dto` usando Lombok `@Data`:
> 1. `ClienteDTO` — campos de Cliente incluyendo `clienteId` (Long, solo en respuestas; ignorado en POST). Validaciones en POST/PUT: `@NotBlank` en nombre e identificacion, `@Size(min=4)` en contrasena.
> 2. `CuentaDTO` — campos de Cuenta. Incluye `clienteId` (Long) para asociar al crear. `@NotBlank` en numeroCuenta y tipoCuenta.
> 3. `MovimientoDTO` — request/response: cuentaId (Long), valor (BigDecimal); en response incluir también id, fecha, tipoMovimiento, saldo (asignados por el service). Solo `@NotNull` en cuentaId y valor para POST. **No** incluir `tipoMovimiento` en el body del POST (se calcula en el service).
> 4. `ReporteDTO` — campos: fecha, cliente (nombre), numeroCuenta, tipoCuenta, saldoInicial, estado, movimiento (BigDecimal valor), saldoDisponible."

**Criterio de éxito:** Los 4 DTOs compilan. Tienen anotaciones de validación.

---

## FASE 4 — EXCEPCIONES

### TAREA 4.1 — Crear excepciones personalizadas
**Prompt sugerido:**
> "Crea en el paquete `com.bank.api.exception`:
> 1. `SaldoInsuficienteException.java` — extiende `RuntimeException`. Constructor que llama `super('Saldo no disponible')`.
> 2. `RecursoNoEncontradoException.java` — extiende `RuntimeException`. Constructor recibe String mensaje.
> 3. `ErrorResponse.java` — POJO con campos: timestamp (LocalDateTime), status (int), error (String), mensaje (String), path (String). Usa Lombok `@Data` `@Builder`."

**Criterio de éxito:** Las 3 clases compilan. SaldoInsuficienteException usa el mensaje exacto "Saldo no disponible".

---

### TAREA 4.2 — Crear GlobalExceptionHandler
**Prompt sugerido:**
> "Crea `GlobalExceptionHandler.java` en `com.bank.api.config` con `@RestControllerAdvice`. Maneja:
> 1. `SaldoInsuficienteException` → HTTP 400, cuerpo `ErrorResponse` con el mensaje de la excepción.
> 2. `RecursoNoEncontradoException` → HTTP 404, cuerpo `ErrorResponse`.
> 3. `MethodArgumentNotValidException` → HTTP 400, con detalle de qué campos fallaron.
> 4. `Exception` genérica → HTTP 500, mensaje 'Error interno del servidor'.
> Cada handler retorna `ResponseEntity<ErrorResponse>`. El campo `timestamp` usa `LocalDateTime.now()`."

**Criterio de éxito:** El handler compila. Tiene 4 métodos @ExceptionHandler. Retorna ResponseEntity.

---

## FASE 5 — SERVICIOS

### TAREA 5.1 — Crear ClienteService e implementación
**Prompt sugerido:**
> "Crea la interfaz `ClienteService` en `com.bank.api.service` con métodos que usan **solo DTOs**:
> `List<ClienteDTO> listarTodos()`, `ClienteDTO obtenerPorId(Long id)`, `ClienteDTO crear(ClienteDTO dto)`, `ClienteDTO actualizar(Long id, ClienteDTO dto)`, `void eliminar(Long id)`.
> Luego crea `ClienteServiceImpl` con `@Service`. Inyecta `ClienteRepository` por constructor. Mapeo manual entidad↔DTO con métodos privados `toDto`/`toEntity`. `listarTodos` solo clientes con `estado=true`. `eliminar` hace **borrado lógico** (`estado=false`), no `delete()`. Lanza `RecursoNoEncontradoException` si el id no existe o ya está inactivo."

**Criterio de éxito:** Firma pública del service solo usa DTOs. DELETE lógico. Mapeo en el service.

---

### TAREA 5.2 — Crear CuentaService e implementación
**Prompt sugerido:**
> "Crea `CuentaService` y `CuentaServiceImpl` con el mismo patrón DTO que ClienteService (`List<CuentaDTO>`, `CuentaDTO`, borrado lógico con `estado`). En `crear`, busca el cliente activo por `clienteId` del DTO. `saldoDisponible` = `saldoInicial` al crear. Mapeo manual en el service."

**Criterio de éxito:** Solo DTOs en la API del service. Borrado lógico. Cuenta asociada al cliente activo.

---

### TAREA 5.3 — Crear MovimientoService con lógica de saldo (F2 y F3)
**Prompt sugerido:**
> "Crea `MovimientoService` y `MovimientoServiceImpl` con métodos públicos en DTO: `List<MovimientoDTO> listarTodos()`, `MovimientoDTO obtenerPorId(Long id)`, `MovimientoDTO registrar(MovimientoDTO dto)`. En `registrar`:
> 1. Buscar cuenta activa por `cuentaId` — `RecursoNoEncontradoException` si no existe o `estado=false`.
> 2. `nuevoSaldo = saldoDisponible.add(valor)` con `BigDecimal`.
> 3. Si `nuevoSaldo.compareTo(BigDecimal.ZERO) < 0`, lanzar `SaldoInsuficienteException`.
> 4. Actualizar y guardar cuenta; crear movimiento con fecha=now, tipo según signo del valor, saldo=nuevoSaldo.
> 5. Retornar `MovimientoDTO` mapeado. `@Transactional` en `registrar`. Listados con `@Transactional(readOnly = true)`."

**Criterio de éxito:** @Transactional en registrar. compareTo para saldo. Retorna DTO. Sin PUT/DELETE.

---

### TAREA 5.4 — Crear ReporteService (F4)
**Prompt sugerido:**
> "Crea `ReporteService` y su implementación. El método `generarReporte(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin)` retorna `List<ReporteDTO>`. Usa `MovimientoRepository.findByCuenta_ClienteClienteIdAndFechaBetween(...)` para obtener los movimientos. Mapea cada movimiento a `ReporteDTO` con: fecha, cliente (nombre del cliente), numeroCuenta, tipoCuenta, saldoInicial, estado, movimiento (valor del movimiento), saldoDisponible (saldo resultante)."

**Criterio de éxito:** El método retorna una lista de ReporteDTO correctamente mapeados.

---

## FASE 6 — CONTROLADORES

### TAREA 6.1 — Crear ClienteController
**Prompt sugerido:**
> "Crea `ClienteController.java` con `@RestController` y `@RequestMapping('/clientes')`. Inyecta `ClienteService`. Implementa GET /, GET /{id}, POST /, PUT /{id}, DELETE /{id} (borrado lógico). Todos los métodos usan `ClienteDTO` en request/response (`ResponseEntity<ClienteDTO>` o `List<ClienteDTO>`). @Valid en POST/PUT."

**Criterio de éxito:** Solo DTOs en el controller. Códigos HTTP correctos. DELETE → 204.

---

### TAREA 6.2 — Crear CuentaController
**Prompt sugerido:**
> "Crea `CuentaController.java` siguiendo exactamente el mismo patrón que `ClienteController` pero para `/cuentas` y usando `CuentaService` y `CuentaDTO`."

**Criterio de éxito:** Mismo criterio que 6.1.

---

### TAREA 6.3 — Crear MovimientoController
**Prompt sugerido:**
> "Crea `MovimientoController.java` para `/movimientos`. Solo: GET / (listar), GET /{id}, POST / (registrar → HTTP 201). **No** implementar PUT ni DELETE (movimientos inmutables). Todos los métodos retornan `MovimientoDTO`."

**Criterio de éxito:** Solo GET y POST. POST ejecuta lógica de saldo. Respuestas en DTO.

---

### TAREA 6.4 — Crear ReporteController
**Prompt sugerido:**
> "Crea `ReporteController.java` con `@GetMapping('/reportes')`. Recibe parámetros: `@RequestParam String fechaInicio`, `@RequestParam String fechaFin`, `@RequestParam Long clienteId`. Parsea las fechas a `LocalDate` con formato `yyyy-MM-dd`. Llama a `reporteService.generarReporte(...)` y retorna HTTP 200 con `List<ReporteDTO>`."

**Criterio de éxito:** El endpoint `/api/reportes?fechaInicio=2024-01-01&fechaFin=2024-12-31&clienteId=1` retorna JSON.

---

## FASE 7 — PRUEBAS UNITARIAS

### TAREA 7.1 — Prueba unitaria: MovimientoService (lógica de saldo)
**Prompt sugerido:**
> "Crea `MovimientoServiceTest.java` en `src/test/.../service/` con JUnit 5 y Mockito. Escribe 2 tests:
> 1. `registrarMovimiento_debeActualizarSaldo_cuandoHaySaldo`: Simula una cuenta con saldo 2000, registra retiro de 575 (valor=-575), verifica que el saldo resultante sea 1425 y que se guardó el movimiento.
> 2. `registrarMovimiento_debeLanzarExcepcion_cuandoSaldoInsuficiente`: Simula cuenta con saldo 100, intenta retiro de 200 (valor=-200), verifica que se lanza `SaldoInsuficienteException`.
> Usa `@ExtendWith(MockitoExtension.class)`, `@Mock` para los repositorios, `@InjectMocks` para el servicio."

**Criterio de éxito:** Los 2 tests pasan con `mvn test`. Usan Mockito correctamente.

---

### TAREA 7.2 — Prueba unitaria: ClienteController
**Prompt sugerido:**
> "Crea `ClienteControllerTest.java` en `src/test/.../controller/` usando `@WebMvcTest(ClienteController.class)` y `MockMvc`. Escribe 2 tests:
> 1. `listarClientes_debeRetornar200_conListaVacia`: GET /api/clientes → verifica HTTP 200 y respuesta JSON array.
> 2. `crearCliente_debeRetornar201_cuandoDatosValidos`: POST /api/clientes con body JSON válido → verifica HTTP 201.
> Usa `@MockBean` para ClienteService."

**Criterio de éxito:** Los 2 tests pasan. Se usa MockMvc correctamente.

---

## FASE 8 — DOCKER

### TAREA 8.1 — Crear Dockerfile
**Prompt sugerido:**
> "Crea un `Dockerfile` multi-stage para la aplicación Spring Boot. Stage 1 (build): usa `maven:3.9.4-eclipse-temurin-17` para compilar el jar con `mvn clean package -DskipTests`. Stage 2 (runtime): usa `eclipse-temurin:17-jre-alpine`, copia el jar del stage anterior, expone puerto 8080, CMD para ejecutar el jar."

**Criterio de éxito:** El Dockerfile tiene 2 stages. La imagen final es ligera (alpine).

---

### TAREA 8.2 — Crear docker-compose.yml
**Prompt sugerido:**
> "Crea `docker-compose.yml` con 2 servicios:
> 1. `sqlserver`: imagen `mcr.microsoft.com/mssql/server:2022-latest`, variables SA_PASSWORD=Banking@2024, ACCEPT_EULA=Y, puerto 1433:1433, volumen para persistencia, healthcheck con sqlcmd.
> 2. `banking-api`: build desde el Dockerfile local, puerto 8080:8080, depende de sqlserver (condition: service_healthy), variables de entorno para la conexión JDBC."

**Criterio de éxito:** `docker-compose up` levanta ambos servicios. La app conecta a la BD.

---

## FASE 9 — ENTREGABLES FINALES

### TAREA 9.1 — Generar BaseDatos.sql
**Prompt sugerido:**
> "Genera el archivo `src/main/resources/BaseDatos.sql` con:
> 1. Creación de la base de datos `banking_db`
> 2. CREATE TABLE para `clientes`, `cuentas`, `movimientos` con todas las columnas, PKs, FKs y constraints.
> 3. INSERT de datos de ejemplo del ejercicio: Jose Lema, Marianela Montalvo, Juan Osorio con sus cuentas y movimientos."

**Criterio de éxito:** El script ejecuta sin errores en SQL Server. Incluye todos los datos de ejemplo.

---

### TAREA 9.2 — Generar colección Postman
**Prompt sugerido:**
> "Genera un archivo JSON de colección Postman (v2.1) llamado `banking-api.postman_collection.json` con requests para todos los endpoints: GET/POST/PUT/DELETE /clientes, /cuentas, /movimientos, y GET /reportes. Incluye ejemplos de body JSON para los POST y PUT basados en los datos de prueba del ejercicio."

**Criterio de éxito:** El archivo JSON importa correctamente en Postman. Todos los endpoints tienen ejemplos.

---

## CHECKLIST FINAL

Antes de subir a GitHub verifica:

- [ ] `mvn clean install` pasa sin errores
- [ ] `mvn test` — todos los tests en verde
- [ ] `docker-compose up` — ambos contenedores levantan
- [ ] Postman: POST /clientes crea un cliente → HTTP 201
- [ ] Postman: POST /movimientos con saldo insuficiente → HTTP 400 + "Saldo no disponible"
- [ ] Postman: GET /reportes retorna JSON con estructura correcta
- [ ] Repositorio GitHub público con README
- [ ] BaseDatos.sql en el repositorio
- [ ] Colección Postman exportada en el repositorio
