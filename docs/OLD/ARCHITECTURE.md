# ARCHITECTURE.md — Arquitectura Android (módulo único)

## 0. Estado y alcance de este documento

Este documento define la arquitectura transversal del proyecto **AICha** (app de chat con IA para Android). A diferencia de una propuesta genérica multi-módulo, esta versión está ajustada a la realidad del proyecto: **un único módulo de Gradle (`app`)**, organizado por paquetes siguiendo el patrón **package-by-feature**.

Este documento cubre la arquitectura transversal (capas, paquetes, DI, concurrencia, convención de UI, testing, CI/CD). El detalle de comportamiento y requerimientos de una feature concreta (por ejemplo, la feature de Chat) vive en su propio documento de spec (ver `SPEC.md`), no aquí.

---

## 1. Principios de arquitectura

- **Separación por capas**: presentación, datos (y dominio cuando la lógica lo justifique), con dependencias apuntando siempre hacia adentro.
- **Unidireccionalidad de datos (UDF)**: la UI emite eventos, el ViewModel procesa y expone estado inmutable; la UI solo renderiza estado, nunca lo muta directamente.
- **Single source of truth**: cada dato tiene un único origen autoritativo (el repositorio correspondiente); la UI nunca guarda copias divergentes del dato.
- **Testabilidad por diseño**: la lógica de negocio vive en clases sin dependencias de Android (`ViewModel`, y `UseCase` cuando se justifique) para poder testearse con JUnit puro.
- **Disciplina de paquetes en vez de imposición de Gradle**: al ser un módulo único, los límites entre capas no se imponen por el compilador (como sí ocurre en multi-módulo), sino por convención de equipo. Esto exige más disciplina en code review para no romper la separación.
- **Consistencia de paradigma**: coroutines + Flow en toda la app; no mezclar con RxJava ni LiveData.

---

## 2. Stack tecnológico

| Área | Elección | Nota |
|---|---|---|
| Lenguaje | Kotlin | — |
| UI | Jetpack Compose + Material 3 | — |
| Arquitectura de presentación | MVVM (`ViewModel` + `StateFlow`) | — |
| Inyección de dependencias | Hilt | — |
| Concurrencia | Coroutines + Flow | — |
| Navegación | Compose Navigation | — |
| Proveedor de IA | **Groq** (motor de inferencia) | El cliente HTTP es el propio SDK `openai-kotlin` (Aallam), que trae su transporte (Ktor) — no se usa Retrofit para esta integración. |
| Persistencia local | Room (historial de conversaciones) + KSP (no KAPT) | — |
| Testing unitario | JUnit + MockK + Turbine (para Flow) | — |
| Logging | Timber (opcional) | A confirmar si se adopta |

> Si en el futuro se agregan integraciones REST propias (backend propio, analytics, etc.) que no sean el cliente de Groq, ahí sí aplica evaluar Retrofit + OkHttp como cliente HTTP genérico, ya que el SDK de `openai-kotlin` es específico para la integración con Groq/OpenAI.

---

## 3. Organización de paquetes (módulo único, package-by-feature)

```text
com.juanpvivas.aichatjp/
├── data/
│   ├── local/               # Room: entities, DAOs, instancia de base de datos, migraciones
│   ├── remote/               # Cliente de Groq (openai-kotlin), request/response mapeados si aplica
│   ├── repository/           # Implementaciones de repositorio (única capa que conoce local/ y remote/)
│   └── model/                # Modelos de dominio compartidos entre repository y ui
├── di/                        # Módulos Hilt: NetworkModule, DatabaseModule, RepositoryModule
├── ui/
│   ├── <feature>/             # Un paquete por feature (ej. chat/, history/)
│   │   ├── <Feature>Route.kt
│   │   ├── <Feature>Screen.kt
│   │   ├── <Feature>ViewModel.kt
│   │   ├── <Feature>UiState.kt
│   │   └── components/
│   └── theme/                 # Colores, tipografía y tema de Material 3
└── MainActivity.kt
```

**Reglas de dependencia entre paquetes (aplicadas por convención/code review, no por el compilador):**

- `ui/<feature>/` depende de `data/repository/` (vía interfaz) y de `data/model/`. Nunca importa clases de `data/local/` (entidades de Room) ni de `data/remote/` (DTOs) directamente.
- `data/repository/` es la única capa que conoce Room y el cliente de Groq; traduce entidades/DTOs a `data/model/` antes de exponerlos.
- Si la lógica de negocio de una feature empieza a repetirse entre ViewModels o a crecer en complejidad, extraerla a `domain/usecase/` (paquete a introducir solo cuando se justifique, no por defecto desde el día uno).
- `di/` es el único paquete que puede referenciar simultáneamente `data/local`, `data/remote` y `data/repository` para armar el grafo de dependencias.

---

## 4. Estructura y convenciones de la capa de datos (`data/`)

La capa de datos está dividida en tres sub-paquetes principales más un paquete de modelos compartidos:

```text
data/
├── local/               # Room: entidades, DAOs, instancia de base de datos, migraciones
├── remote/              # Cliente de Groq (openai-kotlin), DTOs, mappers DTO→Dominio
├── repository/          # Implementaciones de repositorio (única capa que conoce local/ y remote/)
└── model/               # Modelos de dominio compartidos entre repository y ui
```

**Reglas fundamentales:**

- `remote/` es la única capa que conoce el SDK externo (en este caso `openai-kotlin` para Groq); nunca expone sus tipos (DTOs, clases del SDK) fuera de este paquete.
- `repository/` consume tanto `local/` como `remote/`, y es la única que decide cuándo leer de cada una; expone interfaces y tipos del paquete `model/`, nunca DTOs ni entidades de Room.
- `model/` contiene los modelos de dominio puros (sin dependencias de Room, SDK externo ni Android); son el "lenguaje" que usa toda la app.

### 4.1 Convenciones de nomenclatura en `remote/`

Para mantener consistencia y claridad, sigue estas convenciones al agregar nuevas integraciones remotas:

| Tipo de archivo | Patrón de nombre | Ejemplo | Responsabilidad |
|---|---|---|---|
| **DataSource (interfaz)** | `<Entidad>RemoteDataSource` | `ChatRemoteDataSource` | Define qué operaciones remotas expone (métodos públicos sin lógica de SDK) |
| **DataSource (implementación)** | `<Entidad>RemoteDataSourceImpl` | `ChatRemoteDataSourceImpl` | Implementa la interfaz usando el SDK concreto (openai-kotlin para Groq) |
| **DTOs (request/response)** | `<Entidad>Dto`, `<Entidad>Request`, `<Entidad>Response` | `ChatMessageDto`, `SendMessageRequest`, `ChatResponse` | Clases que mapean directamente con el contrato de la API/SDK |
| **Mapper DTO→Dominio** | `<Entidad>Mapper` o `<Entidad>MapperImpl` | `ChatMessageMapper` | Traduce DTOs a modelos de dominio; testeable y sin dependencias de Android |
| **Cliente API/SDK wrapper** | `<Entidad>Service`, `<Entidad>Api`, `<Entidad>Client` | `GroqClient` (si es un wrapper) | Si encapsulas el SDK en un cliente propio, úsalo para abstraer cambios futuros de proveedor |

**Ejemplo de estructura para la feature de Chat:**

```text
data/remote/
├── ChatRemoteDataSource.kt              # Interfaz pública
├── ChatRemoteDataSourceImpl.kt           # Implementación (usa openai-kotlin)
├── dto/
│   ├── ChatMessageDto.kt
│   ├── ChatResponse.kt
│   └── SendMessageRequest.kt
└── mapper/
    └── ChatMessageMapper.kt             # DTO → Dominio
```

**Regla de oro:** los DTOs y el SDK nunca cruzan hacia `model/`, `repository/` o `ui/`. Todo mapeo sucede dentro de `remote/`, y la salida es siempre un tipo del paquete `model/`.

### 4.2 Convenciones de nomenclatura en `local/`

| Tipo de archivo | Patrón de nombre | Ejemplo | Responsabilidad |
|---|---|---|---|
| **Entidad Room** | `<Entidad>Entity` | `ChatMessageEntity`, `ConversationEntity` | Representa la tabla tal cual vive en SQLite; solo la conoce `local/` |
| **DAO** | `<Entidad>Dao` | `ChatMessageDao`, `ConversationDao` | Define las queries de Room (`@Query`, `@Insert`, etc.) |
| **Instancia de base de datos** | `<App>Database` | `AiChaDatabase` | `RoomDatabase` que agrupa todos los DAOs |
| **Migraciones** | `MIGRATION_<from>_<to>` (constante) | `MIGRATION_1_2` | Una constante por salto de versión de esquema |
| **LocalDataSource** (si se envuelve el DAO en vez de inyectarlo directo) | `<Entidad>LocalDataSource` / `<Entidad>LocalDataSourceImpl` | `ChatMessageLocalDataSource` | Opcional: útil si hay lógica extra sobre el DAO (combinar queries, cachear, etc.) |

### 4.3 Convenciones de nomenclatura en `repository/`

| Tipo de archivo | Patrón de nombre | Ejemplo |
|---|---|---|
| **Interfaz de repositorio** | `<Entidad>Repository` | `ChatRepository` |
| **Implementación** | `<Entidad>RepositoryImpl` | `ChatRepositoryImpl` |

La interfaz vive junto a (o cerca de) su implementación dentro de `repository/`, y es lo único que `ui/` y `di/` deberían referenciar directamente (nunca la clase `Impl`, salvo en el binding de Hilt).

### 4.4 Convenciones en `model/`

- Los modelos de dominio **no llevan sufijo** (a diferencia de `Entity`/`Dto`): `ChatMessage`, `Conversation`, no `ChatMessageModel` ni `ChatMessageDomain`. El nombre "limpio" es la señal de que es el modelo de dominio, el que se usa en `ui/` y `repository/`.
- Errores de dominio: tipo sellado `AppError` con subtipos por categoría (`AppError.Network`, `AppError.Http`, `AppError.NoConnectivity`, `AppError.Unknown`). Evitar nombres genéricos como `Response<T>` para wrappers de resultado, ya que choca semánticamente con clases de librerías HTTP; preferir `Result<T>` (stdlib de Kotlin) o un `NetworkResult<T>` propio si se necesita más granularidad que `Result`.

### 4.5 Convención de mapeo: extension functions en vez de clases `Mapper`

Como alternativa (y en general preferida en proyectos Kotlin) a una clase `Mapper` dedicada, se recomienda usar **extension functions** con nombre estandarizado, ya que es más idiomático en Kotlin y reduce boilerplate:

```kotlin
// remote/mapper/ChatMessageMappers.kt
fun ChatMessageDto.toDomain(): ChatMessage

// local/mapper/ChatMessageMappers.kt
fun ChatMessageEntity.toDomain(): ChatMessage
fun ChatMessage.toEntity(): ChatMessageEntity
```

- Nombre de archivo: `<Entidad>Mappers.kt` (plural, ya que suele agrupar ida y vuelta de un mismo tipo).
- Función siempre `to<Destino>()`: `toDomain()`, `toEntity()`, `toDto()`.
- Se usa esta convención **o** una clase `<Entidad>Mapper` (§4.1) de forma consistente en todo el proyecto — mezclar ambos estilos para el mismo tipo de mapeo genera inconsistencia. Elegir una y documentarla aquí una vez decidida (por ahora, `Mapper` está definido en §4.1 como el default de `remote/`; si se adopta extension functions, actualizar esta sección para que ambas coincidan).

---

## 5. Capa de presentación: convención Route/Screen

Cada pantalla de la aplicación sigue esta estructura y respeta el patrón de desacoplamiento de Compose:

```text
ui/
└── <nombre_pantalla>/
    ├── <NombrePantalla>Route.kt         # Conector con estado (ViewModel, Navegación)
    ├── <NombrePantalla>Screen.kt        # Pantalla "Stateless" (Scaffold y estructura visual)
    ├── <NombrePantalla>ViewModel.kt     # ViewModel de la pantalla
    ├── <NombrePantalla>UiState.kt       # Estado de la UI (Loading, Success, Error, Empty)
    └── components/                      # Composables específicos de esta pantalla
        ├── <NombrePantalla>Header.kt
        ├── <NombrePantalla>Content.kt
        ├── <NombrePantalla>Item.kt
        ├── <NombrePantalla>Empty.kt
        └── <NombrePantalla>Footer.kt
```

**Reglas:**

- **Patrón Route/Screen**: `Route.kt` es el único archivo con acceso al `ViewModel`; recolecta el estado y maneja eventos de navegación hacia otras pantallas. `Screen.kt` es **completamente stateless**: solo recibe parámetros primitivos, el `UiState` y lambdas (`() -> Unit`) para eventos, de forma que sea 100% compatible con `@Preview`.
- **`UiState`**: toda pantalla con datos asíncronos modela su estado como `sealed interface` (`Loading`, `Success`, `Error`, y `Empty` cuando aplique).
- **Componentes en `components/`**: independientes y reutilizables dentro de la pantalla; no acceden al ViewModel ni a variables globales. Componentes muy pequeños (<20 líneas) pueden agruparse en un único archivo (ej. `ChatList.kt` puede contener lista e ítem si no son complejos).
- **Pragmatismo**: en pantallas estáticas o muy simples se permite omitir `components/` y definir sub-composables privados dentro de `Screen.kt`.
- **Localización**: prohibido hardcodear textos en los Composables. Todo texto visible va en `strings.xml` y se llama con `stringResource(R.string.identificador)`.

---

## 6. Manejo de estado y concurrencia

- Todo `Flow` expuesto desde un ViewModel usa `stateIn(scope, SharingStarted.WhileSubscribed(5_000), initialValue)` para sobrevivir cambios de configuración sin fugas.
- Los `Dispatchers` se inyectan (nunca `Dispatchers.IO` hardcodeado dentro de una clase) para poder sustituirlos en tests.
- Efectos secundarios de un solo disparo (navegación, snackbars) se exponen como `Channel`/`SharedFlow`, nunca como parte del `UiState` persistente, para que no se repitan en recomposición o rotación.

---

## 7. Inyección de dependencias (Hilt)

- Un `@Module` por tipo de binding dentro de `di/` (`NetworkModule` para el cliente de Groq, `DatabaseModule` para Room, `RepositoryModule` para los bindings de repositorio).
- Repositorios expuestos vía interfaz (`@Binds`), nunca la implementación concreta, para permitir fakes en tests.
- `@Singleton` para el cliente de Groq y la instancia de Room; `@ViewModelScoped` para dependencias atadas al ciclo de vida de una pantalla.
- `hiltViewModel()` en Compose para inyectar ViewModels en el árbol de navegación.

---

## 8. Persistencia local

- **Room** para el historial de conversaciones, con **KSP** (no KAPT) para el procesador de anotaciones.
- Migraciones obligatorias y testeadas ante cualquier cambio de esquema; no usar `fallbackToDestructiveMigration()` en producción.
- El repositorio correspondiente decide cuándo leer de Room vs. cuándo pedir una nueva respuesta a Groq; la UI nunca accede a los DAOs directamente.

---

## 9. Integración con Groq

- El cliente de IA vive en `data/remote/`, usando el SDK `openai-kotlin` apuntando al endpoint compatible de Groq.
- El repositorio de chat es responsable de construir el contexto histórico completo de la conversación antes de cada llamada (ver `SPEC.md` para el detalle de comportamiento de esta feature).
- Errores de red/API (timeout, rate limit, error de modelo) se normalizan en un tipo de error propio antes de llegar al ViewModel; el ViewModel decide cómo se traduce a `UiState.Error`.
- La API Key de Groq nunca se hardcodea: se lee desde `local.properties`/variables de entorno y se inyecta vía `BuildConfig` o Hilt, nunca se sube al repositorio.

---

## 10. Testing

| Capa | Herramientas | Qué se cubre |
|---|---|---|
| Repositorio | JUnit + MockK + fixtures | Mapeo de datos, manejo de errores de red, lectura/escritura en Room (in-memory) |
| ViewModel | JUnit + MockK + Turbine | Transiciones de `UiState`: un test por estado (loading/success/error/empty) |
| UI | Compose Testing | Estados principales renderizados correctamente, interacciones clave |
| Flujos end-to-end (agente IA) | Android CLI + Journeys (Gemini) | Journeys en lenguaje natural que navegan la app como lo haría un usuario real (ver §10.2) |

- Los fakes/mocks compartidos entre features se agrupan en un paquete de testing común (ej. `testutil/` o `data/repository/fake/`) para no duplicarlos.

### 10.1. Ubicación y nomenclatura de tests unitarios

Los tests unitarios (`ViewModel`, `Repository`, mappers, use cases) viven en el source set **`test`**, no en `androidTest` (ese queda reservado para instrumentados y Journeys, ver §10.2). La regla es **espejo exacto del paquete de la clase bajo test**, cambiando `main` por `test`:

```text
app/src/test/java/com/juanpvivas/aichatjp/
├── ui/chat/
│   └── ChatViewModelTest.kt
└── data/repository/
    └── ChatRepositoryImplTest.kt
```

**Reglas:**

- **Nombre de archivo:** `<ClassName>Test.kt`. Se testea la implementación concreta, no la interfaz: si la clase es `ChatRepositoryImpl`, el test es `ChatRepositoryImplTest`, no `ChatRepositoryTest` (una interfaz no tiene lógica propia que testear).
- **Ruta:** idéntica a la de `src/main/`, solo cambia el source set (`test` en vez de `main`).
- **Un test por estado/caso**, no un único test gigante por clase — por ejemplo, `ChatViewModelTest` debería tener métodos como `emits loading then success when message sent`, `emits error when repository throws`, etc.

### 10.2. Testing con Android CLI (Journeys)

Además de los tests unitarios/instrumentados tradicionales, el proyecto usa **Journeys** (la funcionalidad de Android Studio/Android CLI basada en Gemini) para validar flujos completos de UI en lenguaje natural: se describe una secuencia de pasos (`<action>`) y el agente de IA los ejecuta sobre la app, tomando screenshots y razonando sobre lo que ve en pantalla, en vez de depender de selectores como Espresso/UI Automator.

**Estado actual:**

- Los archivos `.xml` de journeys viven en `app/src/androidTest/jurney/`, siguiendo la convención estándar de la herramienta (dentro del `androidTest` source set).
- Se ejecutan pidiéndole al agente de IA (Android CLI) que localice la carpeta y corra el journey correspondiente sobre un emulador/dispositivo. Al estar ya dentro de `androidTest`, también quedan disponibles para correrse vía Gradle (ej. `./gradlew testJourneysTestDefaultDebugTestSuite`) si se decide integrarlos a CI más adelante.

**Nomenclatura sugerida por journey:** un archivo por flujo crítico de usuario (ej. `send_message.xml`, `select_previous_conversation.xml`, `start_new_conversation.xml`). Cada journey debe describir un flujo de principio a fin (incluyendo verificaciones, no solo acciones), tal como el ejemplo actual de "Send Message Journey".

---

## 11. Convenciones de código y calidad

- **Linting**: ktlint + detekt en pre-commit/CI, bloqueando merge si fallan.
- **Naming**: `PascalCase` para clases/composables, `camelCase` para funciones/variables, sufijos consistentes (`Repository`, `ViewModel`, `UiState`, `Screen`, `Route`).
- **Verificación de compilación**: antes de dar por cerrado cualquier cambio de código, compilar con `./gradlew compileDebugKotlin` para detectar errores de sintaxis o dependencias rotas.
- **Gradle**: no modificar configuración existente en `build.gradle.kts`/`libs.versions.toml` salvo que sea estrictamente necesario; agregar lo nuevo sin tocar lo ya definido salvo justificación explícita.
- **Commits**: Conventional Commits (`feat:`, `fix:`, `refactor:`, `test:`, `chore:`).
- **Branching**: `feature/<nombre-descriptivo>` desde `main`; prohibido commitear directo a `main`. Todo cambio pasa por Pull Request.

---

## 12. CI/CD

Pipeline mínimo sugerido (a adaptar al proveedor real):

1. **Lint**: ktlint + detekt.
2. **Build**: `./gradlew assembleDebug`.
3. **Test**: `./gradlew test` (suite unitaria completa en cada PR).
4. **Artefactos**: generación de APK en merges a `main` (opcional, según necesidad del proyecto).

---

## 13. Seguridad

- **API Key de Groq:** nunca se sube al repositorio. Vive en `local.properties` en la raíz del proyecto (ignorado por git) o en variables de entorno de CI, según el entorno:
    - **Local:** archivo `local.properties` (ver README.md §2 para instrucciones de creación).
    - **CI/CD:** variables de entorno del proveedor (GitHub Secrets, GitLab CI variables, etc.), inyectadas al build sin pasar por archivos versionados.
- R8/minificación habilitado en builds de release.

---

## 14. Puntos abiertos

- [ ] Estrategia de expiración/límite del historial de conversaciones (¿se guarda indefinidamente? ¿hay límite de mensajes o de conversaciones?).
- [ ] Comportamiento ante error de la API de Groq a mitad de una respuesta en streaming (si se implementa streaming a futuro).
- [ ] Si el proyecto crece y se justifica, evaluar extracción a multi-módulo Gradle (`core`, `feature-chat`, etc.) — no aplica por ahora dado el tamaño del proyecto.