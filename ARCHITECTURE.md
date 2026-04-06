# Análisis de Arquitectura — agoispro-backend

> Documento de referencia para integrar procesos del proyecto `idmhperu` hacia `agoispro-backend`.

---

## 1. Stack Tecnológico

| Componente       | Tecnología                                          |
|-----------------|-----------------------------------------------------|
| Framework       | Spring Boot 3.5.4 + **Spring WebFlux** (reactivo)   |
| Seguridad       | Spring Security 6.5.2 + JWT (jjwt)                 |
| Base de datos   | MySQL 8.0 (JdbcTemplate — **NO usa JPA/Hibernate**) |
| Acceso a datos  | `JdbcTemplate` con SQL raw y stored procedures      |
| Lombok          | Sí (en todos los modelos)                           |
| Documentación   | SpringDoc OpenAPI (Swagger UI)                      |
| Reactividad     | Project Reactor (`Mono` / `Flux`)                   |

> **Importante:** aunque `spring-boot-starter-data-jpa` está en el classpath, el proyecto
> **NO usa entidades JPA ni Spring Data Repositories**. Toda la persistencia se hace con
> `JdbcTemplate` directamente.

---

## 2. Estructura de Paquetes

```
org.autoservicio.backendcontratoservicio
├── BackendAutoservicioApplication.java
├── config/
│   ├── genericModel.java          ← Wrapper genérico de respuesta
│   ├── responseModel.java         ← Respuesta simple (solo mensaje string)
│   ├── response_generic_read.java ← Respuesta con paginación
│   ├── IConfigGeneric.java        ← Provee JdbcTemplate vía DataBaseConfig
│   ├── SecurityConfig.java        ← WebFlux security + CORS
│   └── WebConfig.java             ← (CORS comentado, ya está en Security)
├── database/
│   └── DataBaseConfig.java        ← DataSource manual (hardcoded)
├── jwt/
│   └── JwtAuthenticationFilter.java ← WebFilter que valida JWT
├── util/
│   ├── JwtUtil.java               ← Genera y valida tokens JWT
│   ├── GoogleDriveOAuthUtil.java
│   ├── ConfiguracionUtil.java
│   ├── NumeroALetrasUtil.java
│   └── base64Util.java
├── excepciones/
│   ├── GenericoException.java     ← Convierte resultado/error → ResponseEntity
│   └── RepositorioException.java  ← Excepción de capa de datos
├── interfaces/                    ← Contratos (interfaces) de acceso a datos
│   ├── mantenimiento/ICalles.java
│   ├── gestionclientes/IClientes.java
│   ├── user/IUserRepo.java
│   └── ...
├── model/                         ← POJOs de entrada/request interno
│   ├── mantenimientos/CallesModel.java
│   ├── gestionclientes/ClientesModel.java
│   ├── user/UserModel.java
│   └── ...
├── response/                      ← POJOs de salida (consultas con más campos)
│   ├── ClientesRequest.java
│   ├── ContratoResponse.java
│   └── ...
├── request/                       ← POJOs para requests de operaciones especiales
│   ├── RegisterRequestModel.java
│   └── ...
├── repository/                    ← Implementaciones de interfaces (JdbcTemplate)
│   ├── mantenimientos/CallesRepository.java
│   ├── gestionclientes/ClientesRepository.java
│   ├── user/UserRepository.java
│   └── ...
├── service/                       ← Servicios que envuelven repo en Mono
│   ├── mantenimientos/CallesService.java
│   ├── gestionclientes/ClientesService.java
│   ├── auth/AuthService.java
│   └── ...
└── controller/                    ← Controladores REST WebFlux
    ├── mantenimientos/cCalles.java
    ├── gestionclientes/cClientes.java
    ├── auth/AuthController.java
    └── ...
```

---

## 3. Flujo Completo (de la DB al Controller)

### Capa 1 — Interface (contrato de datos)

```java
// interfaces/mantenimiento/ICalles.java
public interface ICalles {
    List<CallesModel> listadocalles();
    responseModel registrarcalles(Integer op, CallesModel obj);
}
```
- Es una **interface Java pura**, sin Spring.
- Define los métodos de acceso a datos **sincrónicos** (devuelven tipos normales).
- Los métodos de IUserRepo son la excepción: devuelven `Mono<>` directamente.

---

### Capa 2 — Repository (implementación con JdbcTemplate)

```java
// repository/mantenimientos/CallesRepository.java
@Repository
public class CallesRepository extends IConfigGeneric implements ICalles {

    @Override
    public List<CallesModel> listadocalles() {
        try {
            String query = "SELECT c.codcalle, ... FROM calles AS c JOIN tipocalle AS tc ...";
            return this.jTemplate().query(query,
                    new BeanPropertyRowMapper<>(CallesModel.class));
        } catch (Exception ex) {
            throw new RepositorioException("error en listado: " + ex.getMessage());
        }
    }

    @Override
    public responseModel registrarcalles(Integer op, CallesModel obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(obj);          // objeto → JSON string
            String sql = "CALL usp_registrar_calles(?, ?)";        // stored procedure
            String mensaje = this.jTemplate().queryForObject(sql, String.class, op, json);
            return responseModel.builder().response(mensaje).build();
        } catch (Exception ex) {
            throw new RepositorioException("Error al registrar: " + ex.getMessage());
        }
    }
}
```

**Patrones clave del Repository:**
- Extiende `IConfigGeneric` → hereda `jTemplate()` que devuelve `JdbcTemplate`.
- Implementa la interface correspondiente.
- **Consultas SELECT:** `jTemplate().query(sql, new BeanPropertyRowMapper<>(Clase.class))`.
- **Consultas con parámetros:** `jTemplate().query(sql, new Object[]{param}, new BeanPropertyRowMapper<>(...))`.
- **Stored procedures de escritura:** serializa el objeto a JSON con `ObjectMapper`, llama `CALL usp_nombre(op, json)`, obtiene un `String` de respuesta.
- **Errores:** siempre captura `Exception` y lanza `RepositorioException`.

---

### Capa 3 — Service (envoltura reactiva)

```java
// service/mantenimientos/CallesService.java
@Service
public class CallesService {
    @Autowired
    private CallesRepository repo;

    public Mono<List<CallesModel>> listadocalles() {
        return Mono.fromCallable(() -> this.repo.listadocalles());
    }

    public Mono<responseModel> registrarcalles(Integer op, CallesModel obj) {
        return Mono.fromCallable(() -> this.repo.registrarcalles(op, obj));
    }
}
```

**Patrones clave del Service:**
- `@Autowired` sobre el repository concreto (no la interface).
- **Única responsabilidad:** envolver la llamada síncrona del repo en `Mono.fromCallable()`.
- Cuando se necesita threading explícito: `.subscribeOn(Schedulers.boundedElastic())`.
- Sin interface de servicio (no hay `IClientesService`).

---

### Capa 4 — Controller (endpoint REST WebFlux)

```java
// controller/mantenimientos/cCalles.java
@Slf4j
@RestController
@RequestMapping("/api/calles")
@RequiredArgsConstructor
public class cCalles {
    private final CallesService service;

    // GET — listado
    @GetMapping("/listar")
    public Mono<ResponseEntity<genericModel<List<CallesModel>>>> obtener_listadocalles() {
        return this.service.listadocalles()
                .flatMap(GenericoException::success)
                .doOnSuccess(r -> log.info("Operación exitosa"))
                .doOnError(e -> log.error("Error: {}", e.getMessage()))
                .onErrorResume(GenericoException::error);
    }

    // POST — registrar con operación
    @PostMapping("/registrar/{op}")
    public @ResponseBody Mono<ResponseEntity<genericModel<responseModel>>> registrarcalles(
            @PathVariable Integer op,
            @RequestBody CallesModel form
    ) {
        return this.service.registrarcalles(op, form)
                .flatMap(GenericoException::success)
                .doOnSuccess(r -> log.info("Operación exitosa"))
                .doOnError(e -> log.error("Error: {}", e.getMessage()))
                .onErrorResume(GenericoException::error);
    }
}
```

**Patrones clave del Controller:**
- `@RequiredArgsConstructor` + `private final Service` (inyección por constructor via Lombok).
- Retorno siempre: `Mono<ResponseEntity<genericModel<T>>>`.
- Pipeline reactivo: `.flatMap(GenericoException::success)` → `.onErrorResume(GenericoException::error)`.
- Operaciones CRUD usan `@PathVariable Integer op` (1=insert, 2=update, 3=delete — definido en el SP).
- Parámetros de búsqueda con `@RequestParam(required = false)`.

---

## 4. Wrappers de Respuesta

### `genericModel<T>` — Respuesta estándar

```java
// Todos los endpoints retornan esto:
{
  "success": true,
  "mensaje": "EXITO",
  "data": { ... }  // cualquier tipo T
}
```

### `responseModel` — Para escrituras (insert/update/delete)

```java
{
  "response": "Registro guardado correctamente"  // mensaje del SP
}
```

### `response_generic_read<T>` — Para listados con conteo

```java
{
  "totalrecords": 100,
  "listar": [ ... ]
}
```

### `GenericoException` — Factory de respuestas

```java
// Éxito → HTTP 200 + genericModel(success=true)
GenericoException.success(data)

// Error → HTTP 200 + genericModel(success=false, mensaje=error)
GenericoException.error(throwable)
```

---

## 5. Modelos vs Responses

El proyecto diferencia dos tipos de POJOs:

| Paquete      | Propósito                                                         |
|-------------|-------------------------------------------------------------------|
| `model/`    | Datos de **entrada** (request body) — campos del formulario       |
| `response/` | Datos de **salida** (resultado de SP/query) — puede tener joins   |

Ejemplo `ClientesModel` (entrada) vs `ClientesRequest` (salida con campos extra de JOINs como `destipodocident`, `descripcioncalle`, etc.).

> **No usan MapStruct** — los datos van directamente del ResultSet al POJO vía `BeanPropertyRowMapper`.

---

## 6. Seguridad

### JWT Filter (WebFlux WebFilter)

```
Request → JwtAuthenticationFilter → extrae Bearer token → JwtUtil.validateToken()
       → claims (username, rol) → UsernamePasswordAuthenticationToken
       → ReactiveSecurityContextHolder → continúa cadena
```

### SecurityConfig

- `@EnableWebFluxSecurity` (NO `@EnableWebSecurity`).
- `SecurityWebFilterChain` (NO `HttpSecurity`).
- Rutas públicas: `/api/auth/**`, Swagger, Izipay, Paramae, Pagos.
- Rutas protegidas: `/api/**`.

---

## 7. Convenciones de Nomenclatura

| Capa        | Convención                  | Ejemplo                    |
|------------|-----------------------------|----------------------------|
| Interface   | `I` + Nombre                | `ICalles`, `IClientes`     |
| Repository  | Nombre + `Repository`       | `CallesRepository`         |
| Service     | Nombre + `Service`          | `CallesService`            |
| Controller  | `c` + Nombre (minúscula c)  | `cCalles`, `cClientes`     |
| Model (IN)  | Nombre + `Model`            | `CallesModel`              |
| Model (OUT) | Nombre + `Request`/`Response` | `ClientesRequest`        |

---

## 8. Cómo Adaptar los Patrones de `idmhperu`

El proyecto `idmhperu` usa: `Entity (JPA)` → `Repository (Spring Data)` → `Specification` → `Service (interface + impl)` → `Request/Response (MapStruct)` → `Controller`.

Este proyecto (`agoispro`) usa: `POJO Model` → `Interface` → `Repository (JdbcTemplate)` → `Service` → `Controller`.

### Tabla de equivalencias

| `idmhperu`                   | `agoispro` (cómo implementar)                                   |
|-----------------------------|------------------------------------------------------------------|
| `@Entity` + `@Table`        | POJO plano con Lombok en `model/` (sin anotaciones JPA)         |
| Spring Data `JpaRepository` | Interface en `interfaces/` + impl en `repository/` con JdbcTemplate |
| `Specification<T>`          | Query SQL raw en el repository con parámetros dinámicos         |
| `ServiceInterface`          | No necesaria — service concreto directamente                     |
| `ServiceImpl`               | Clase `@Service` simple que llama al repo con `Mono.fromCallable`|
| `RequestDTO` (MapStruct)    | POJO en `model/` (sin mapper — datos van directo al SP en JSON)  |
| `ResponseDTO` (MapStruct)   | POJO en `response/` mapeado por `BeanPropertyRowMapper`          |
| `@RestController`           | Igual, pero retorna `Mono<ResponseEntity<genericModel<T>>>`      |

### Pasos para integrar un nuevo proceso

1. **Crear el POJO de entrada** en `model/<subpaquete>/NombreModel.java`
2. **Crear el POJO de salida** en `response/NombreResponse.java` (si tiene más campos que el model)
3. **Crear la interface** en `interfaces/<subpaquete>/INombre.java` con métodos sincrónicos
4. **Crear el Repository** en `repository/<subpaquete>/NombreRepository.java` extendiendo `IConfigGeneric` e implementando la interface
5. **Crear el Service** en `service/<subpaquete>/NombreService.java` envolviendo con `Mono.fromCallable()`
6. **Crear el Controller** en `controller/<subpaquete>/cNombre.java` con el pipeline reactivo estándar

---

## 9. Ejemplo de Código Plantilla

### Interface
```java
public interface INuevoModulo {
    List<NuevoResponse> listar();
    responseModel registrar(Integer op, NuevoModel obj);
}
```

### Repository
```java
@Repository
public class NuevoModuloRepository extends IConfigGeneric implements INuevoModulo {
    @Override
    public List<NuevoResponse> listar() {
        try {
            return this.jTemplate().query(
                "CALL usp_listar_nuevo()",
                new BeanPropertyRowMapper<>(NuevoResponse.class));
        } catch (Exception ex) {
            throw new RepositorioException("error: " + ex.getMessage());
        }
    }

    @Override
    public responseModel registrar(Integer op, NuevoModel obj) {
        try {
            String json = new ObjectMapper().writeValueAsString(obj);
            String msg = this.jTemplate().queryForObject(
                "CALL usp_registrar_nuevo(?, ?)", String.class, op, json);
            return responseModel.builder().response(msg).build();
        } catch (Exception ex) {
            throw new RepositorioException("error: " + ex.getMessage());
        }
    }
}
```

### Service
```java
@Service
public class NuevoModuloService {
    @Autowired
    private NuevoModuloRepository repo;

    public Mono<List<NuevoResponse>> listar() {
        return Mono.fromCallable(() -> this.repo.listar());
    }

    public Mono<responseModel> registrar(Integer op, NuevoModel obj) {
        return Mono.fromCallable(() -> this.repo.registrar(op, obj));
    }
}
```

### Controller
```java
@Slf4j
@RestController
@RequestMapping("/api/nuevo")
@RequiredArgsConstructor
public class cNuevoModulo {
    private final NuevoModuloService service;

    @GetMapping("/listar")
    public Mono<ResponseEntity<genericModel<List<NuevoResponse>>>> listar() {
        return this.service.listar()
                .flatMap(GenericoException::success)
                .doOnSuccess(r -> log.info("Operación exitosa"))
                .doOnError(e -> log.error("Error: {}", e.getMessage()))
                .onErrorResume(GenericoException::error);
    }

    @PostMapping("/registrar/{op}")
    public @ResponseBody Mono<ResponseEntity<genericModel<responseModel>>> registrar(
            @PathVariable Integer op,
            @RequestBody NuevoModel form) {
        return this.service.registrar(op, form)
                .flatMap(GenericoException::success)
                .doOnSuccess(r -> log.info("Operación exitosa"))
                .doOnError(e -> log.error("Error: {}", e.getMessage()))
                .onErrorResume(GenericoException::error);
    }
}
```

---

## 10. Notas Importantes

- **No hay validación de campos** (`@Valid`, `@NotNull`) en los controllers actuales.
- **No hay paginación** implementada (aunque existe `response_generic_read` con `totalrecords`).
- La **lógica de negocio** (validaciones, cálculos, flujo) reside en los **stored procedures** de MySQL, no en el service Java.
- El service Java es prácticamente un delegador reactivo, no contiene lógica.
- Los modelos de entrada y salida a veces son idénticos en campos, diferenciados solo porque el de salida puede tener campos adicionales del JOIN (resultado del SP).
- **DataBaseConfig** tiene las credenciales hardcodeadas (además de estar en `application.properties`).
