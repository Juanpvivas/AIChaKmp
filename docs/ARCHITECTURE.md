# ARCHITECTURE.md — Arquitectura Kotlin Multiplatform (Clean Architecture)

## 0. Estado y alcance de este documento

Este documento define la arquitectura transversal del proyecto **AICha** (app de chat con IA para **Android e iOS**). El proyecto está migrando de un módulo Android único a **Kotlin Multiplatform (KMP)**, con **Compose Multiplatform** como capa de presentación compartida entre ambas plataformas.

La arquitectura interna adopta **Clean Architecture** con tres capas explícitas (`domain`, `data`, `presentation`), reemplazando el enfoque previo de "package-by-feature plano". El objetivo es que la lógica de negocio (`domain`) y la mayor parte de `data`/`presentation` vivan en código común (`commonMain`), y que el código específico de plataforma se limite a lo estrictamente necesario (drivers de base de datos, engine HTTP, entry points nativos), aislado mediante `expect`/`actual`.

Este documento cubre la arquitectura transversal (capas, módulos, DI, concurrencia, convención de UI, testing, CI/CD). El detalle de comportamiento y requerimientos de una feature concreta (por ejemplo, la feature de Chat) vive en su propio documento de spec (ver [`SPEC.md`](./SPEC.md)), no aquí.

> **Nota de migración:** este documento describe el estado **objetivo** de la arquitectura tras la migración a KMP, no necesariamente el estado actual del código en un momento dado. Durante la migración conviven código legado (Android-only, Hilt) y código nuevo (`commonMain`, Koin); toda incorporación nueva debe seguir las reglas de este documento, y el código legado se migra de forma incremental feature por feature.

---

## 1. Principios de arquitectura

- **Clean Architecture, regla de dependencia:** las dependencias de código siempre apuntan hacia adentro — `presentation` → `domain` ← `data`. `domain` no depende de `data` ni de `presentation`, ni de ningún framework (Android SDK, iOS/Foundation, Room, Ktor, Koin). Es Kotlin puro y 100% multiplatform.
- **Separación por capas:** `domain` (reglas de negocio y contratos), `data` (implementación de esos contratos: red y persistencia), `presentation` (UI y estado de pantalla). Ver §3.
- **Unidireccionalidad de datos (UDF):** la UI emite eventos, el ViewModel procesa (vía use cases) y expone estado inmutable; la UI solo renderiza estado, nunca lo muta directamente.
- **Single source of truth:** cada dato tiene un único origen autoritativo (el repositorio correspondiente, en `data/repository/`); la UI nunca guarda copias divergentes del dato.
- **Testabilidad por diseño:** `domain` y la mayor parte de `data`/`presentation` son Kotlin puro sin dependencias de plataforma, por lo que corren en `commonTest` con JUnit/kotlin.test sin necesidad de emulador ni simulador.
- **Máximo código común, mínimo código de plataforma:** todo lo que no dependa genuinamente de una API nativa (Android SDK / iOS Foundation) vive en `commonMain`. El código específico de plataforma se aísla vía `expect`/`actual` con el menor scope posible (ver §4).
- **Consistencia de paradigma:** coroutines + Flow en toda la app (`commonMain` incluido); no mezclar con RxJava, LiveData ni Combine/RxSwift del lado iOS.

---

## 2. Stack tecnológico

| Área | Elección | Nota |
|---|---|---|
| Lenguaje | Kotlin Multiplatform (Android + iOS) | Target `androidTarget()` + `iosArm64()`/`iosSimulatorArm64()`/`iosX64()` |
| UI | **Compose Multiplatform** + Material 3 | UI compartida entre Android e iOS; un único árbol de Composables en `commonMain` |
| Arquitectura de presentación | MVVM (`ViewModel` multiplatform + `StateFlow`) | `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose` (artefacto KMP oficial de JetBrains, API equivalente a `androidx.lifecycle.ViewModel`) |
| Navegación | Compose Multiplatform Navigation | `org.jetbrains.androidx.navigation:navigation-compose` |
| Inyección de dependencias | **Koin** | Reemplaza a Hilt (Android-only); módulos comunes + módulos por plataforma (ver §7) |
| Concurrencia | Coroutines + Flow | `Dispatchers.IO`/`Default`/`Main` disponibles en `commonMain` desde kotlinx.coroutines 1.6+; siempre inyectados, nunca hardcodeados (ver §6) |
| Fechas/tiempo | `kotlinx-datetime` | Reemplaza `java.time`/`Date` (no multiplatform) para timestamps de mensajes/conversaciones |
| Proveedor de IA | **Groq** (motor de inferencia) | Cliente `openai-kotlin` (Aallam), ya multiplatform porque su transporte es Ktor |
| Cliente HTTP | Ktor Client | Engine `OkHttp` en Android, engine `Darwin` en iOS (vía `expect`/`actual`, ver §4) |
| Persistencia local | **Room Multiplatform** (2.7+) + KSP | Entities/DAOs en `commonMain`; `RoomDatabase.Builder` con actual por plataforma (Context en Android, ruta de documentos en iOS) |
| Testing común | kotlin.test + kotlinx-coroutines-test + Turbine (multiplatform) | Domain, data (con fakes) y ViewModels, en `commonTest` |
| Testing específico Android | JUnit + MockK | MockK es JVM-only: solo en `androidUnitTest`, para lo que no se pueda testear con fakes en común |
| Logging | Kermit (Touchlab) o logger propio `expect`/`actual` | A confirmar; reemplaza soluciones Android-only tipo Timber |

> Si en el futuro se agregan integraciones REST propias (backend propio, analytics, etc.) que no sean el cliente de Groq, reutilizar el mismo `HttpClient` de Ktor ya configurado en `data/remote/`, no introducir un segundo cliente HTTP.

---

## 3. Estructura de módulos y capas (Clean Architecture)

### 3.1 Estructura de módulos (Gradle)

```text
AICha-KMP/
├── composeApp/                         # Módulo KMP único: domain + data + presentation compartidos
│   ├── src/
│   │   ├── commonMain/kotlin/com/juanpvivas/aichatjp/
│   │   │   ├── domain/                 # Entities, UseCases, contratos de Repository — Kotlin puro
│   │   │   ├── data/                   # Implementación de los contratos de domain
│   │   │   │   ├── local/              # Room Multiplatform: entities, DAOs, database
│   │   │   │   ├── remote/             # Cliente Groq (openai-kotlin/Ktor), DTOs, mappers
│   │   │   │   └── repository/         # Impl. de las interfaces de domain/repository
│   │   │   ├── di/                     # Módulos Koin comunes
│   │   │   ├── ui/                     # Presentation: Compose Multiplatform (Route/Screen/ViewModel/UiState)
│   │   │   └── core/                   # Utilidades multiplatform (logger, dispatchers provider, etc.)
│   │   ├── androidMain/kotlin/.../      # actual: driver de Room, engine Ktor (OkHttp), módulo Koin Android, MainActivity
│   │   ├── iosMain/kotlin/.../          # actual: driver de Room, engine Ktor (Darwin), módulo Koin iOS
│   │   ├── commonTest/                  # Tests multiplatform: domain, data (fakes), ViewModels
│   │   ├── androidUnitTest/             # Tests específicos Android (MockK, si aplica)
│   │   └── androidInstrumentedTest/     # Instrumentados Android + Journeys (ver §10.2)
│   └── build.gradle.kts
├── iosApp/                              # Proyecto Xcode: únicamente entry point nativo
│   ├── iosApp.xcodeproj
│   └── iosApp/App.swift                 # Monta el framework de composeApp (ComposeUIViewController)
├── docs/
├── build.gradle.kts
└── settings.gradle.kts
```

**Regla clave:** `iosApp/` (Swift) no contiene lógica de negocio ni de presentación — es un wrapper mínimo que arranca Koin (`initKoin()`) y monta la UI de Compose Multiplatform expuesta por `composeApp`. Toda pantalla, estado y lógica vive en Kotlin dentro de `composeApp`.

### 3.2 Capas de Clean Architecture

```text
com.juanpvivas.aichatjp/
├── domain/
│   ├── model/            # Entidades de dominio puras (ChatMessage, Conversation) — sin sufijo
│   ├── repository/        # Interfaces de repositorio (contratos que implementa data/)
│   └── usecase/           # Casos de uso: una acción de negocio por clase
├── data/
│   ├── local/              # Room Multiplatform: entities, DAOs, instancia de base de datos
│   ├── remote/              # Cliente de Groq (openai-kotlin), DTOs, mappers DTO→Dominio
│   └── repository/          # Implementaciones de domain/repository (única capa que conoce local/ y remote/)
├── di/                        # Módulos Koin: NetworkModule, DatabaseModule, RepositoryModule, UseCaseModule
├── core/                      # Dispatchers provider, logger, utilidades sin lógica de negocio
└── ui/
    ├── <feature>/             # Un paquete por feature (ej. chat/, history/)
    │   ├── <Feature>Route.kt
    │   ├── <Feature>Screen.kt
    │   ├── <Feature>ViewModel.kt
    │   ├── <Feature>UiState.kt
    │   └── components/
    └── theme/                 # Colores, tipografía y tema de Material 3 (Compose Multiplatform)
```

**Reglas de dependencia entre capas (aplicadas por convención/code review; el compilador solo impone el límite de `expect`/`actual` entre source sets, no entre capas):**

- `domain/` no importa nada de `data/`, `ui/`, `di/`, ni de ningún SDK de plataforma. Solo Kotlin estándar + `kotlinx.coroutines` + `kotlinx-datetime`.
- `ui/<feature>/` depende de `domain/model/` y `domain/usecase/` (nunca de `domain/repository/` directamente, y nunca de `data/local/` ni `data/remote/`). El ViewModel invoca use cases, no repositorios.
- `data/repository/` implementa las interfaces definidas en `domain/repository/`; es la única capa que conoce `local/` y `remote/` a la vez, y traduce entidades/DTOs a `domain/model/` antes de exponerlos.
- `di/` es el único paquete que puede referenciar simultáneamente `data/local`, `data/remote`, `data/repository` y `domain/usecase` para armar el grafo de dependencias.
- Un **UseCase** (`domain/usecase/`) representa una única acción de negocio (ej. `SendMessageUseCase`, `ObserveConversationHistoryUseCase`, `CreateConversationUseCase`). Es la puerta de entrada obligatoria desde `ui/` hacia `domain`/`data`: los ViewModels **no** invocan repositorios directamente, siempre a través de un use case, aunque el use case sea un simple passthrough. Esto mantiene el límite de capas explícito y testeable incluso cuando la lógica es trivial hoy y puede crecer mañana.

---

## 4. Código específico de plataforma (`expect`/`actual`)

Con Compose Multiplatform y Room Multiplatform, la superficie de código específico de plataforma se reduce a los puntos donde Kotlin no puede ser 100% común: acceso a Context/almacenamiento del SO y engine de red.

| Necesidad | `expect` (en `commonMain`) | `actual` Android | `actual` iOS |
|---|---|---|---|
| Instancia de base de datos | `expect fun createRoomDatabase(): AiChaDatabase` (o `DatabaseBuilderFactory`) | Usa `Context` (inyectado por Koin) + `Room.databaseBuilder` | Usa el directorio de documentos de la app (`NSDocumentDirectory`) |
| Engine HTTP de Ktor | `expect fun httpClientEngine(): HttpClientEngine` | `OkHttp` | `Darwin` |
| Dispatcher de I/O | No requiere `expect` (disponible en `commonMain` desde coroutines 1.6+) | — | — |
| Logger | `expect class Logger` o librería multiplatform (Kermit) | Delegado a `Log`/Logcat | Delegado a `NSLog`/os_log |
| API Key de Groq | `expect fun groqApiKey(): String` | Lee de `BuildConfig` (inyectado desde `local.properties`, ver §13) | Lee de `Info.plist`/`Config.xcconfig` (ver §13) |

**Reglas:**

- El bloque `expect`/`actual` debe ser lo más pequeño posible (una función o clase puntual), nunca una capa entera duplicada por plataforma.
- Ningún archivo de `androidMain`/`iosMain` contiene lógica de negocio: solo el "pegamento" hacia la API nativa correspondiente.
- Nomenclatura: mismo nombre de archivo/clase en los tres source sets (`commonMain`, `androidMain`, `iosMain`), sufijo `.<platform>.kt` quede implícito por el source set, no por el nombre del archivo.

---

## 5. Estructura y convenciones de la capa de datos (`data/`)

La capa de datos está dividida en tres sub-paquetes, e implementa los contratos definidos en `domain/repository/`:

```text
data/
├── local/               # Room Multiplatform: entidades, DAOs, instancia de base de datos, migraciones
├── remote/              # Cliente de Groq (openai-kotlin), DTOs, mappers DTO→Dominio
└── repository/          # Implementaciones de domain/repository/ (única capa que conoce local/ y remote/)
```

**Reglas fundamentales:**

- `remote/` es la única capa que conoce el SDK externo (`openai-kotlin` para Groq); nunca expone sus tipos (DTOs, clases del SDK) fuera de este paquete.
- `repository/` consume tanto `local/` como `remote/`, decide cuándo leer de cada una, e implementa las interfaces de `domain/repository/`; expone siempre tipos de `domain/model/`, nunca DTOs ni entidades de Room.
- `domain/model/` (no `data/model/`) contiene los modelos de dominio puros, sin dependencias de Room, del SDK externo ni de Android/iOS. Son el "lenguaje" que usa toda la app, incluida `ui/`.

### 5.1 Convenciones de nomenclatura en `remote/`

| Tipo de archivo | Patrón de nombre | Ejemplo | Responsabilidad |
|---|---|---|---|
| **DataSource (interfaz)** | `<Entidad>RemoteDataSource` | `ChatRemoteDataSource` | Define qué operaciones remotas expone (métodos públicos sin lógica de SDK) |
| **DataSource (implementación)** | `<Entidad>RemoteDataSourceImpl` | `ChatRemoteDataSourceImpl` | Implementa la interfaz usando el SDK concreto (openai-kotlin para Groq) — vive en `commonMain`, ya que el SDK es multiplatform |
| **DTOs (request/response)** | `<Entidad>Dto`, `<Entidad>Request`, `<Entidad>Response` | `ChatMessageDto`, `SendMessageRequest`, `ChatResponse` | Clases que mapean directamente con el contrato de la API/SDK |
| **Mapper DTO→Dominio** | Extension functions `to<Destino>()` (ver §5.4) | `ChatMessageDto.toDomain()` | Traduce DTOs a modelos de dominio; testeable en `commonTest` sin dependencias de plataforma |

**Ejemplo de estructura para la feature de Chat:**

```text
data/remote/
├── ChatRemoteDataSource.kt              # Interfaz pública
├── ChatRemoteDataSourceImpl.kt           # Implementación (usa openai-kotlin, commonMain)
├── dto/
│   ├── ChatMessageDto.kt
│   ├── ChatResponse.kt
│   └── SendMessageRequest.kt
└── mapper/
    └── ChatMessageMappers.kt             # DTO → Dominio
```

**Regla de oro:** los DTOs y el SDK nunca cruzan hacia `domain/`, `data/repository/` (como tipo de retorno) o `ui/`. Todo mapeo sucede dentro de `remote/`, y la salida es siempre un tipo de `domain/model/`.

### 5.2 Convenciones de nomenclatura en `local/` (Room Multiplatform)

| Tipo de archivo | Patrón de nombre | Ejemplo | Responsabilidad |
|---|---|---|---|
| **Entidad Room** | `<Entidad>Entity` | `ChatMessageEntity`, `ConversationEntity` | Representa la tabla tal cual vive en SQLite; solo la conoce `local/`. Vive en `commonMain` (Room Multiplatform soporta entities/DAOs comunes) |
| **DAO** | `<Entidad>Dao` | `ChatMessageDao`, `ConversationDao` | Define las queries de Room (`@Query`, `@Insert`, etc.), en `commonMain` |
| **Instancia de base de datos** | `<App>Database` | `AiChaDatabase` | `RoomDatabase` que agrupa todos los DAOs; declarada en `commonMain`, construida vía `expect`/`actual` (ver §4) |
| **Migraciones** | `MIGRATION_<from>_<to>` (constante) | `MIGRATION_1_2` | Una constante por salto de versión de esquema, en `commonMain` |
| **LocalDataSource** (si se envuelve el DAO en vez de inyectarlo directo) | `<Entidad>LocalDataSource` / `<Entidad>LocalDataSourceImpl` | `ChatMessageLocalDataSource` | Opcional: útil si hay lógica extra sobre el DAO (combinar queries, cachear, etc.) |

### 5.3 Convenciones de nomenclatura en `repository/`

| Tipo de archivo | Patrón de nombre | Ejemplo |
|---|---|---|
| **Interfaz de repositorio** | `<Entidad>Repository` | Definida en `domain/repository/ChatRepository.kt` |
| **Implementación** | `<Entidad>RepositoryImpl` | `data/repository/ChatRepositoryImpl.kt` |

La interfaz vive en `domain/repository/`; la implementación vive en `data/repository/`. `ui/` y `di/` referencian siempre la interfaz (nunca la clase `Impl`, salvo en el binding de Koin).

### 5.4 Convención de mapeo: extension functions

Se usan **extension functions** con nombre estandarizado para mapear entre capas, por ser más idiomático en Kotlin y multiplatform-friendly (sin necesidad de instanciar una clase `Mapper` con DI):

```kotlin
// remote/mapper/ChatMessageMappers.kt
fun ChatMessageDto.toDomain(): ChatMessage

// local/mapper/ChatMessageMappers.kt
fun ChatMessageEntity.toDomain(): ChatMessage
fun ChatMessage.toEntity(): ChatMessageEntity
```

- Nombre de archivo: `<Entidad>Mappers.kt` (plural, agrupa ida y vuelta de un mismo tipo).
- Función siempre `to<Destino>()`: `toDomain()`, `toEntity()`, `toDto()`.
- Todas las funciones de mapeo son Kotlin puro y viven en `commonMain`; se testean en `commonTest` sin mocks.

---

## 6. Capa de dominio (`domain/`)

```text
domain/
├── model/          # ChatMessage, Conversation, AppError — sin sufijo, sin dependencias externas
├── repository/     # ChatRepository, ConversationRepository (interfaces)
└── usecase/        # SendMessageUseCase, ObserveConversationHistoryUseCase, CreateConversationUseCase, ...
```

- **Modelos de dominio:** sin sufijo (`ChatMessage`, no `ChatMessageModel`/`ChatMessageDomain`). Usan `kotlinx-datetime` para timestamps, nunca `java.time`/`java.util.Date` (no son multiplatform).
- **Errores de dominio:** tipo sellado `AppError` con subtipos por categoría (`AppError.Network`, `AppError.Http`, `AppError.NoConnectivity`, `AppError.Unknown`). Se evita `Response<T>` genérico (choca semánticamente con clases HTTP); se prefiere `Result<T>` (stdlib de Kotlin, multiplatform) o un `NetworkResult<T>` propio si se necesita más granularidad.
- **Use cases:** una clase por acción de negocio, con `operator fun invoke(...)` como convención de invocación (`SendMessageUseCase(conversationId, text)`). Reciben las interfaces de `domain/repository/` por constructor (inyectadas por Koin), nunca implementaciones concretas. Son la única puerta de entrada desde `ui/`.

---

## 7. Capa de presentación: Compose Multiplatform + convención Route/Screen

Cada pantalla de la aplicación sigue esta estructura, común a Android e iOS (un único árbol de Composables):

```text
ui/
└── <nombre_pantalla>/
    ├── <NombrePantalla>Route.kt         # Conector con estado (ViewModel, navegación)
    ├── <NombrePantalla>Screen.kt        # Pantalla "Stateless" (estructura visual)
    ├── <NombrePantalla>ViewModel.kt     # ViewModel multiplatform (invoca use cases)
    ├── <NombrePantalla>UiState.kt       # Estado de la UI (Loading, Success, Error, Empty)
    └── components/                      # Composables específicos de esta pantalla
        ├── <NombrePantalla>Header.kt
        ├── <NombrePantalla>Content.kt
        ├── <NombrePantalla>Item.kt
        ├── <NombrePantalla>Empty.kt
        └── <NombrePantalla>Footer.kt
```

**Reglas:**

- **Patrón Route/Screen:** `Route.kt` es el único archivo con acceso al `ViewModel`; recolecta el estado y maneja eventos de navegación hacia otras pantallas. `Screen.kt` es **completamente stateless**: solo recibe parámetros primitivos, el `UiState` y lambdas (`() -> Unit`) para eventos, de forma que sea 100% compatible con `@Preview` en ambas plataformas.
- **`ViewModel`:** se usa `org.jetbrains.androidx.lifecycle.ViewModel` (artefacto KMP), no `androidx.lifecycle.ViewModel` (Android-only). La API es equivalente; el `viewModelScope` sigue disponible. El ViewModel **invoca use cases de `domain/usecase/`**, nunca repositorios directamente.
- **`UiState`:** toda pantalla con datos asíncronos modela su estado como `sealed interface` (`Loading`, `Success`, `Error`, y `Empty` cuando aplique).
- **Componentes en `components/`:** independientes y reutilizables dentro de la pantalla; no acceden al ViewModel ni a variables globales. Componentes muy pequeños (<20 líneas) pueden agruparse en un único archivo.
- **Pragmatismo:** en pantallas estáticas o muy simples se permite omitir `components/` y definir sub-composables privados dentro de `Screen.kt`.
- **Localización:** prohibido hardcodear textos en los Composables. Compose Multiplatform usa recursos multiplatform (`compose.resources` / `stringResource(Res.string.identificador)`), generados a partir de archivos en `composeApp/src/commonMain/composeResources/`, reemplazando el `strings.xml` Android-only.
- **Diferencias visuales por plataforma (cuando sean necesarias):** se resuelven con `expect`/`actual` a nivel de Composable puntual (ej. un `expect @Composable fun PlatformBackHandler()`), nunca duplicando la pantalla completa.

---

## 8. Manejo de estado y concurrencia

- Todo `Flow` expuesto desde un ViewModel usa `stateIn(scope, SharingStarted.WhileSubscribed(5_000), initialValue)` para sobrevivir cambios de configuración/recomposición sin fugas, en ambas plataformas.
- Los `Dispatchers` se inyectan vía un `DispatcherProvider` (interfaz en `core/`, implementación única en `commonMain` ya que `Dispatchers.IO`/`Default`/`Main` son multiplatform desde coroutines 1.6+); nunca se referencia `Dispatchers.IO` hardcodeado dentro de una clase, para poder sustituirlo por `TestDispatcher` en tests.
- Efectos secundarios de un solo disparo (navegación, snackbars) se exponen como `Channel`/`SharedFlow`, nunca como parte del `UiState` persistente, para que no se repitan en recomposición o rotación/re-render.

---

## 9. Inyección de dependencias (Koin)

- Reemplaza a Hilt (Android-only). Módulos organizados por capa, igual que antes con Hilt:
  - `networkModule` (cliente Ktor/openai-kotlin) — común, con el engine resuelto vía `expect`/`actual`.
  - `databaseModule` (Room) — común, con el builder resuelto vía `expect`/`actual`.
  - `repositoryModule` (bindings de `domain/repository/` → `data/repository/*Impl`).
  - `useCaseModule` (factories de `domain/usecase/`).
  - `viewModelModule` (ViewModels de `ui/`).
- **Un módulo común (`commonModule`)** agrupa todos los bindings anteriores que no requieren tipos de plataforma. **Un módulo por plataforma** (`androidPlatformModule`, `iosPlatformModule`) resuelve únicamente los `expect`/`actual` (Context de Android, paths de iOS).
- **Arranque de Koin:**
  - Android: `startKoin { androidContext(this@AIChaApplication); modules(commonModule, androidPlatformModule) }` en la `Application`.
  - iOS: función `fun initKoin()` expuesta desde `commonMain`/`iosMain`, invocada una vez desde `App.swift` al arrancar.
- Repositorios y use cases expuestos vía interfaz/factory, nunca instanciados a mano en `ui/`, para permitir fakes en tests.
- Scoping: singletons para el cliente HTTP y la instancia de Room; factory (nueva instancia) para ViewModels y use cases.

---

## 10. Persistencia local (Room Multiplatform)

- **Room Multiplatform** (2.7+) para el historial de conversaciones: entidades y DAOs viven en `commonMain`, con **KSP** (no KAPT, que no es multiplatform) para el procesador de anotaciones.
- La instancia de `RoomDatabase` se construye vía `expect`/`actual` (ver §4): en Android recibe un `Context`, en iOS resuelve el path al directorio de documentos de la sandbox de la app.
- Migraciones obligatorias y testeadas ante cualquier cambio de esquema (test en `commonTest`); no usar `fallbackToDestructiveMigration()` en producción.
- El repositorio correspondiente (`data/repository/`) decide cuándo leer de Room vs. cuándo pedir una nueva respuesta a Groq; `ui/` nunca accede a los DAOs directamente, ni siquiera a través del repositorio sin pasar por un use case.

---

## 11. Integración con Groq

- El cliente de IA vive en `data/remote/` (`commonMain`), usando el SDK `openai-kotlin` apuntando al endpoint compatible de Groq. Al estar construido sobre Ktor, el SDK es multiplatform sin cambios de código entre Android e iOS; solo cambia el `HttpClientEngine` inyectado (`OkHttp` vs `Darwin`, ver §4).
- El repositorio de chat es responsable de construir el contexto histórico completo de la conversación antes de cada llamada (ver `SPEC.md` para el detalle de comportamiento de esta feature).
- Errores de red/API (timeout, rate limit, error de modelo) se normalizan en `AppError` antes de llegar al ViewModel; el ViewModel decide cómo se traduce a `UiState.Error`.
- La API Key de Groq nunca se hardcodea: se resuelve vía `expect fun groqApiKey(): String` (ver §4), leída desde `local.properties`/`BuildConfig` en Android y desde `Info.plist`/`Config.xcconfig` en iOS. Nunca se sube al repositorio en ninguna de las dos plataformas (ver §13).

---

## 12. Testing

| Capa | Source set | Herramientas | Qué se cubre |
|---|---|---|---|
| Domain (use cases, modelos) | `commonTest` | kotlin.test | Lógica de negocio pura, sin mocks (o con fakes simples) |
| Repositorio y mappers | `commonTest` | kotlin.test + fakes de `RemoteDataSource`/DAO | Mapeo de datos, manejo de errores de red, lectura/escritura en Room (in-memory) |
| ViewModel | `commonTest` | kotlin.test + kotlinx-coroutines-test + Turbine (multiplatform) | Transiciones de `UiState`: un test por estado (loading/success/error/empty) |
| Específico Android (si no se puede fakear en común) | `androidUnitTest` | JUnit + MockK | Casos que dependan de detalles JVM/Android puntuales |
| UI Compose (Android) | `androidInstrumentedTest` | Compose Testing | Estados principales renderizados correctamente, interacciones clave |
| Flujos end-to-end (Android) | `androidInstrumentedTest` | Android CLI + Journeys (Gemini) | Journeys en lenguaje natural que navegan la app como lo haría un usuario real (ver §12.2) — herramienta Android-only |
| UI/E2E iOS | Pendiente (ver §14) | XCUITest (a evaluar) | Sin cobertura automatizada equivalente a Journeys todavía |

- **Preferir fakes sobre mocks** en `commonTest`, ya que MockK es JVM-only y no está disponible en `iosTest`. Un mock real multiplatform (si se necesita) puede evaluarse con librerías como `mokkery`, pero el default del proyecto es usar fakes escritos a mano.
- Los fakes/mocks compartidos entre features se agrupan en un paquete de testing común (ej. `commonTest/.../testutil/` o `data/repository/fake/`) para no duplicarlos.

### 12.1. Ubicación y nomenclatura de tests unitarios

Los tests unitarios (`ViewModel`, `Repository`, `UseCase`, mappers) viven en **`commonTest`** siempre que no dependan de una API de plataforma, no en `androidUnitTest` ni `iosTest`. Regla: **espejo exacto del paquete de la clase bajo test**, cambiando `commonMain` por `commonTest`:

```text
composeApp/src/commonTest/kotlin/com/juanpvivas/aichatjp/
├── ui/chat/
│   └── ChatViewModelTest.kt
├── domain/usecase/
│   └── SendMessageUseCaseTest.kt
└── data/repository/
    └── ChatRepositoryImplTest.kt
```

**Reglas:**

- **Nombre de archivo:** `<ClassName>Test.kt`. Se testea la implementación concreta, no la interfaz: si la clase es `ChatRepositoryImpl`, el test es `ChatRepositoryImplTest`.
- **Ruta:** idéntica a la de `commonMain/`, solo cambia el source set.
- **Un test por estado/caso**, no un único test gigante por clase.
- Solo baja a `androidUnitTest`/`iosTest` lo que sea imposible de testear en común (por ejemplo, una interacción muy puntual con una API de plataforma detrás de un `actual`).

### 12.2. Testing con Android CLI (Journeys)

Además de los tests unitarios/instrumentados, el proyecto usa **Journeys** (funcionalidad de Android Studio/Android CLI basada en Gemini) para validar flujos completos de UI en lenguaje natural, **solo en Android** (la herramienta no tiene equivalente para iOS a la fecha de este documento).

- Los archivos `.xml` de journeys viven en `composeApp/src/androidInstrumentedTest/journey/`.
- Se ejecutan pidiéndole al agente de IA (Android CLI) que localice la carpeta y corra el journey correspondiente sobre un emulador/dispositivo. Al estar dentro de `androidInstrumentedTest`, quedan disponibles para correrse vía Gradle si se decide integrarlos a CI más adelante.
- **Nomenclatura por journey:** un archivo por flujo crítico de usuario (ej. `send_message.xml`, `select_previous_conversation.xml`, `start_new_conversation.xml`).
- **iOS:** hasta que se defina una herramienta equivalente (ver §14), la validación end-to-end de flujos críticos en iOS es manual antes de cada release.

---

## 13. Convenciones de código y calidad

- **Linting:** ktlint + detekt en pre-commit/CI, sobre todo el código Kotlin (`commonMain`, `androidMain`, `iosMain`, tests). Bloquean merge si fallan.
- **Naming:** `PascalCase` para clases/composables, `camelCase` para funciones/variables, sufijos consistentes (`Repository`, `UseCase`, `ViewModel`, `UiState`, `Screen`, `Route`, `Entity`, `Dto`).
- **Verificación de compilación:** antes de dar por cerrado cualquier cambio de código, compilar ambos targets:
  - Android: `./gradlew :composeApp:compileDebugKotlinAndroid`
  - iOS: `./gradlew :composeApp:compileKotlinIosSimulatorArm64` (o el framework completo, según el cambio)
- **Gradle:** no modificar configuración existente en `build.gradle.kts`/`libs.versions.toml` salvo que sea estrictamente necesario; agregar lo nuevo sin tocar lo ya definido salvo justificación explícita.
- **Commits:** Conventional Commits (`feat:`, `fix:`, `refactor:`, `test:`, `chore:`).
- **Branching:** `feature/<nombre-descriptivo>` desde `main`; prohibido commitear directo a `main`. Todo cambio pasa por Pull Request.
- **Swift (`iosApp/`):** al ser solo un wrapper de entry point, se mantiene deliberadamente mínimo. Si crece, evaluar SwiftLint (no configurado por ahora, ver §14).

---

## 14. CI/CD

Pipeline mínimo sugerido (a adaptar al proveedor real). A diferencia del pipeline Android-only anterior, ahora requiere un **runner macOS** para los pasos de iOS:

1. **Lint:** ktlint + detekt (cualquier runner).
2. **Build Android:** `./gradlew :composeApp:assembleDebug` (runner Linux/macOS).
3. **Build iOS:** compilar el framework de `composeApp` para simulador (`linkDebugFrameworkIosSimulatorArm64`) y build del proyecto `iosApp` con `xcodebuild` (requiere runner **macOS**).
4. **Test:** `./gradlew :composeApp:allTests` (corre `commonTest` + `androidUnitTest`; los tests de `iosTest` requieren simulador, correr en el runner macOS).
5. **Artefactos:** generación de APK/AAB en merges a `main`; generación de `.ipa`/archivo de iOS opcionalmente, según necesidad de distribución (TestFlight, etc.).

---

## 15. Seguridad

- **API Key de Groq:** nunca se sube al repositorio, en ninguna plataforma.
  - **Android, local:** archivo `local.properties` en la raíz del proyecto (ignorado por git), leído hacia `BuildConfig` (ver README.md).
  - **iOS, local:** archivo `Config.xcconfig` (ignorado por git) referenciado desde `Info.plist`, leído en runtime vía el `actual` de `groqApiKey()` (ver §4).
  - **CI/CD:** variables de entorno/secretos del proveedor (GitHub Secrets, etc.), inyectadas al build sin pasar por archivos versionados, generando los archivos locales (`local.properties`/`Config.xcconfig`) como paso previo al build si es necesario.
- R8/minificación habilitado en builds de release de Android; evaluar equivalente de ofuscación para el framework de iOS si aplica.
- La base de datos local (Room) no está cifrada por defecto en ninguna plataforma; evaluar cifrado (ej. SQLCipher) si el historial de conversaciones se considera dato sensible (ver §16).

---

## 16. Puntos abiertos

- [ ] Estrategia de expiración/límite del historial de conversaciones (¿se guarda indefinidamente? ¿hay límite de mensajes o de conversaciones?).
- [ ] Comportamiento ante error de la API de Groq a mitad de una respuesta en streaming (si se implementa streaming a futuro).
- [ ] Herramienta de validación E2E para iOS equivalente a Journeys (XCUITest u otra) — hoy la validación de flujos críticos en iOS es manual.
- [ ] Confirmar librería de logging multiplatform definitiva (Kermit vs. logger propio `expect`/`actual`).
- [ ] Evaluar cifrado de la base de datos local (Room) si el historial se considera dato sensible.
- [ ] Plan de migración incremental: orden en que se migran las features existentes (Chat, History) de Hilt/Android-only a Koin/`commonMain`, y cómo conviven ambos enfoques durante la transición.
- [ ] Definir estrategia de distribución de builds iOS (TestFlight, Ad Hoc, etc.) fuera del alcance de este documento.
