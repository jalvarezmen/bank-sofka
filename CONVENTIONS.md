# CONVENTIONS.md
# Reglas de Código — Sistema Bancario
# Este archivo define cómo el agente DEBE escribir código en este proyecto.
# Estas reglas son OBLIGATORIAS. No hay excepciones.

---

## 1. LENGUAJE Y VERSIONES

- Java 17 (usa records, var, switch expressions si aplica)
- Spring Boot 3.x
- JUnit 5 (no JUnit 4)
- Mockito 5.x

---

## 2. NOMBRADO DE CLASES

| Tipo | Convención | Ejemplo |
|------|-----------|---------|
| Entidad JPA | PascalCase, sin sufijo | `Cliente`, `Cuenta` |
| Repositorio | NombreEntidad + Repository | `ClienteRepository` |
| Interfaz Service | NombreEntidad + Service | `ClienteService` |
| Implementación | NombreEntidad + ServiceImpl | `ClienteServiceImpl` |
| Controller | NombreEntidad + Controller | `ClienteController` |
| DTO | NombreEntidad + DTO | `ClienteDTO` |
| Excepción | Descripción + Exception | `SaldoInsuficienteException` |
| Test | ClaseTesteada + Test | `MovimientoServiceTest` |

---

## 3. NOMBRADO DE VARIABLES Y MÉTODOS

- **camelCase** para variables y métodos: `saldoDisponible`, `listarTodos()`
- **UPPER_SNAKE_CASE** para constantes: `MAX_INTENTOS`
- **snake_case** para columnas de BD (en @Column): `saldo_disponible`
- Nombres descriptivos — prohibido: `x`, `temp`, `data`, `obj`
- Métodos de servicio siguen el patrón:
  - `listarTodos()` → GET all
  - `obtenerPorId(Long id)` → GET one
  - `crear(DTO dto)` → POST
  - `actualizar(Long id, DTO dto)` → PUT
  - `eliminar(Long id)` → DELETE

---

## 4. ESTRUCTURA DE CADA CLASE

### Controller
```java
@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor          // Lombok: inyección por constructor
public class ClienteController {

    private final ClienteService clienteService;  // siempre final

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<ClienteDTO> crear(@Valid @RequestBody ClienteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(clienteService.crear(dto));
    }
}
```

### Service Implementation
```java
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    public ClienteDTO obtenerPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "Cliente no encontrado con id: " + id));
        return toDto(cliente);
    }

    private ClienteDTO toDto(Cliente cliente) { /* mapeo manual */ }
    private Cliente toEntity(ClienteDTO dto) { /* mapeo manual */ }
}
```

### Repository
```java
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByIdentificacion(String identificacion);
}
```

---

## 5. REGLAS DE INYECCIÓN DE DEPENDENCIAS

- **SIEMPRE** usar inyección por constructor (nunca @Autowired en campos)
- Usar `@RequiredArgsConstructor` de Lombok + campos `private final`
- Los controllers dependen de interfaces, NUNCA de implementaciones directas

```java
// ✅ CORRECTO
private final ClienteService clienteService;  // interfaz

// ❌ INCORRECTO
@Autowired
private ClienteServiceImpl clienteService;    // implementación directa
```

---

## 6. MANEJO DE EXCEPCIONES

- **NUNCA** usar try-catch en controllers o services para errores de negocio
- **SIEMPRE** lanzar excepciones personalizadas desde el service
- El `GlobalExceptionHandler` es el único lugar que convierte excepciones a HTTP responses

```java
// ✅ CORRECTO — en el service
.orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada: " + id));

// ❌ INCORRECTO — en el controller
try {
    return service.obtener(id);
} catch (Exception e) {
    return ResponseEntity.badRequest().build();
}
```

---

## 7. POLÍTICA DTO (OBLIGATORIA)

- **Controllers:** solo reciben y retornan DTOs (`ResponseEntity<ClienteDTO>`, nunca `Cliente`).
- **Services (interfaces e implementaciones):** métodos públicos usan DTOs en entrada y salida.
- **Repositories:** trabajan solo con entidades JPA.
- **Mapeo:** manual en cada `*ServiceImpl` (métodos privados `toDto` / `toEntity`). Sin MapStruct en este proyecto.
- **Movimientos:** el request solo lleva `cuentaId` y `valor`; `tipoMovimiento` se calcula en el service según el signo del valor.

---

## 8. ENTIDADES JPA

- Toda entidad tiene `@Entity` y `@Table(name = "nombre_en_snake_case")`
- La PK siempre es `Long` con `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- No exponer entidades JPA en controllers ni en firmas públicas de servicios
- Las relaciones lazy se anotan explícitamente: `fetch = FetchType.LAZY`
- **Cliente → Cuenta:** usar `cascade = CascadeType.PERSIST` (no `ALL`) para no borrar cuentas/movimientos en cascada accidentalmente
- **Borrado:** `DELETE` en clientes y cuentas es **borrado lógico** (`estado = false`), no `repository.delete()`

```java
@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente extends Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clienteId;

    @Column(nullable = false)
    private String contrasena;

    @Column(nullable = false)
    private boolean estado;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<Cuenta> cuentas = new ArrayList<>();
}
```

- **Contraseña:** se persiste en texto plano (fuera de alcance: BCrypt o Spring Security).
- **Montos:** usar `BigDecimal` y comparar con `compareTo`, nunca `==` ni `double`.

---

## 9. RESPUESTAS HTTP — CÓDIGOS OBLIGATORIOS

| Operación | Código HTTP |
|-----------|-------------|
| GET (exitoso) | 200 OK |
| POST (creación exitosa) | 201 CREATED |
| PUT (actualización exitosa) | 200 OK |
| DELETE (eliminación exitosa) | 204 NO CONTENT |
| Recurso no encontrado | 404 NOT FOUND |
| Validación fallida / saldo insuficiente | 400 BAD REQUEST |
| Error interno | 500 INTERNAL SERVER ERROR |

---

## 10. VALIDACIONES EN DTOs

- Usar anotaciones de `jakarta.validation.constraints`
- Mensajes de error en español
- Campos obligatorios de texto: `@NotBlank(message = "El nombre es obligatorio")`
- Campos obligatorios de objeto: `@NotNull(message = "El valor es obligatorio")`
- Tamaño mínimo: `@Size(min = 4, message = "Mínimo 4 caracteres")`

```java
public class ClienteDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La identificacion es obligatoria")
    private String identificacion;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres")
    private String contrasena;
}
```

---

## 11. REGLAS PARA PRUEBAS UNITARIAS

- Clase de test: `@ExtendWith(MockitoExtension.class)`
- Nombre de test: `metodoProbado_resultadoEsperado_cuandoCondicion`
- Un test verifica UNA sola cosa
- No conectarse a BD real en tests unitarios (usar @Mock)
- Para tests de controller: `@WebMvcTest` + `@MockBean`

```java
@Test
void registrarMovimiento_debeLanzarExcepcion_cuandoSaldoInsuficiente() {
    // Arrange
    Cuenta cuenta = new Cuenta();
    cuenta.setSaldoDisponible(new BigDecimal("100"));
    when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));

    MovimientoDTO dto = new MovimientoDTO();
    dto.setCuentaId(1L);
    dto.setValor(new BigDecimal("-200"));  // retiro mayor al saldo

    // Act & Assert
    assertThrows(SaldoInsuficienteException.class,
        () -> movimientoService.registrar(dto));
}
```

---

## 12. REGLAS DE FORMATO

- Indentación: 4 espacios (no tabs)
- Máximo 1 línea en blanco entre métodos
- Máximo 100 caracteres por línea
- Llaves de apertura `{` en la misma línea
- Imports organizados: java.*, javax.*, org.*, com.*
- Sin imports con wildcard (`import java.util.*` ❌)

---

## 13. REGLAS PARA docker-compose.yml

- Siempre incluir `healthcheck` en el servicio de base de datos
- La app debe tener `depends_on` con `condition: service_healthy`
- Variables sensibles van en variables de entorno, nunca hardcodeadas en el código Java
- Los puertos expuestos: app → 8080:8080, bd → 1433:1433

---

## 14. LO QUE EL AGENTE NUNCA DEBE HACER

- ❌ Poner lógica de negocio en un Controller
- ❌ Llamar directamente a un Repository desde un Controller
- ❌ Retornar entidades JPA desde controllers o métodos públicos de servicios
- ❌ Usar `repository.delete()` para clientes/cuentas (usar borrado lógico con `estado`)
- ❌ Implementar PUT/DELETE en movimientos (son inmutables; solo POST y GET)
- ❌ Hacer commit de contraseñas o secrets en el código
- ❌ Usar `System.out.println` (usar `@Slf4j` de Lombok con `log.info/error`)
- ❌ Ignorar excepciones con catch vacíos
- ❌ Crear métodos con más de 30 líneas sin refactorizar
