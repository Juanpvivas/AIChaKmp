# AGENT.md

Guía de referencia rápida para agentes de IA (Claude Code, Cursor, etc.) que trabajen en este repositorio. Para el detalle completo, ver los documentos enlazados: no dupliques aquí lo que ya está documentado en otro lugar.

## Qué es este proyecto

**AICha** es un cliente de chat con modelos de lenguaje vía **Groq** (endpoint compatible con OpenAI, integrado con el SDK `openai-kotlin`), para **Android e iOS**. El proyecto está migrando de una app Android nativa de módulo único a **Kotlin Multiplatform (KMP)** con **Clean Architecture** (capas `domain`/`data`/`presentation`) y **Compose Multiplatform** como UI compartida.

- README funcional: [README.md](./README.md) (`docs/README_ES.md` para la versión en español)
- Arquitectura completa (capas, módulos, DI, testing, convenciones): [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md)
- Spec funcional de la feature de Chat: [docs/SPEC.md](./docs/SPEC.md)
- Workflow de contribución (branches, commits, PRs, paridad Android/iOS): [CONTRIBUTING.md](./CONTRIBUTING.md)

Lee `docs/ARCHITECTURE.md` antes de tocar código de datos/DI/presentación; es la fuente de verdad para convenciones de nombres, capas y reglas de dependencia entre paquetes.

> **Nota de migración:** este documento (y `docs/ARCHITECTURE.md`) describen el estado **objetivo** tras la migración a KMP. Puede convivir código legado (Android-only, Hilt) con código nuevo (`commonMain`, Koin) mientras la migración avanza feature por feature; todo código nuevo debe seguir las reglas de aquí, no las del código legado.

## Stack

Kotlin Multiplatform (Android + iOS) · Compose Multiplatform + Material 3 (UI compartida) · MVVM con `ViewModel` multiplatform (`org.jetbrains.androidx.lifecycle`) + `StateFlow` · **Koin** (DI, reemplaza a Hilt) · Coroutines + Flow · `kotlinx-datetime` · Room Multiplatform (con KSP, no KAPT) · `openai-kotlin` (Aallam) sobre Ktor (engine `OkHttp` en Android, `Darwin` en iOS) · kotlin.test + kotlinx-coroutines-test + Turbine (multiplatform, en `commonTest`) · JUnit + MockK (solo `androidUnitTest`, MockK es JVM-only).

## Estructura del código

```
composeApp/
├── src/
│   ├── commonMain/kotlin/com/juanpvivas/aichatjp/
│   │   ├── domain/
│   │   │   ├── model/        # Entidades de dominio puras (sin sufijo): ChatMessage, Conversation
│   │   │   ├── repository/   # Interfaces de repositorio (contratos)
│   │   │   └── usecase/      # Un caso de uso por acción de negocio (operator fun invoke)
│   │   ├── data/
│   │   │   ├── local/        # Room Multiplatform: entities, DAOs, database
│   │   │   ├── remote/        # Cliente Groq (openai-kotlin/Ktor), DTOs, mappers
│   │   │   └── repository/    # Impl. de domain/repository/ (única capa que conoce local/ y remote/)
│   │   ├── di/                 # Módulos Koin comunes
│   │   ├── ui/
│   │   │   ├── chat/           # Feature de chat (Route/Screen/ViewModel/UiState/components)
│   │   │   ├── history/        # Feature de historial de conversaciones
│   │   │   └── theme/
│   │   └── core/               # DispatcherProvider, logger multiplatform
│   ├── androidMain/kotlin/.../   # actual: driver Room, engine Ktor OkHttp, módulo Koin Android, MainActivity
│   ├── iosMain/kotlin/.../       # actual: driver Room, engine Ktor Darwin, módulo Koin iOS
│   ├── commonTest/               # Tests multiplatform: domain, data (fakes), ViewModels
│   └── androidUnitTest/          # Tests específicos Android (MockK, cuando un fake común no alcanza)
└── build.gradle.kts

iosApp/                # Proyecto Xcode: solo entry point (App.swift arranca Koin y monta la UI compartida)
```

Tests unitarios en `composeApp/src/commonTest/kotlin/...` (espejo exacto del paquete de `commonMain`). Journeys de UI en lenguaje natural (Android-only) en `composeApp/src/androidInstrumentedTest/journey/*.xml`.

## Reglas de dependencia entre capas (Clean Architecture)

- `domain/` no depende de nada: ni de `data/`, ni de `ui/`, ni de ningún SDK de plataforma (Android/iOS). Solo Kotlin estándar + coroutines + `kotlinx-datetime`.
- `ui/<feature>/` depende de `domain/model/` y `domain/usecase/`. El ViewModel invoca **use cases**, nunca repositorios ni DAOs directamente.
- `data/repository/` implementa las interfaces de `domain/repository/`; es la única capa que conoce `local/` y `remote/` a la vez, y traduce entidades/DTOs a `domain/model/` antes de exponerlos.
- `data/remote/` es la única capa que conoce el SDK de Groq; nunca expone sus tipos fuera de sí misma. `data/local/` es la única que conoce Room.
- `di/` es el único paquete que puede referenciar `local`, `remote`, `repository` y `usecase` a la vez para armar el grafo de Koin.
- **Código común por defecto:** todo va en `commonMain` salvo que dependa genuinamente de una API nativa (Room `DatabaseBuilder`, engine de Ktor, entry point). Esos casos se aíslan con `expect`/`actual` lo más acotado posible (una función/clase, nunca una capa entera duplicada).

## Convenciones clave

- Sufijos: `Repository`/`RepositoryImpl`, `UseCase` (con `operator fun invoke`), `RemoteDataSource`/`RemoteDataSourceImpl`, `Entity` (Room), `Dto`/`Request`/`Response` (remote), `ViewModel`, `UiState` (sealed interface: `Loading`/`Success`/`Error`/`Empty`), `Screen`/`Route`. Modelos de dominio sin sufijo (`ChatMessage`, no `ChatMessageModel`).
- Patrón Route/Screen: `Route.kt` es el único archivo con acceso al `ViewModel`; `Screen.kt` es stateless (solo params + `UiState` + lambdas), compatible con `@Preview` en ambas plataformas.
- Cero strings hardcodeados en Composables: todo texto vía recursos multiplatform (`stringResource(Res.string.identificador)`), no `strings.xml` Android-only.
- Dispatchers siempre inyectados vía `DispatcherProvider`, nunca `Dispatchers.IO` hardcodeado.
- `Flow` expuesto desde ViewModel usa `stateIn(scope, SharingStarted.WhileSubscribed(5_000), initial)`.
- Errores de red/API se normalizan a un tipo de dominio (`AppError`) antes de llegar al ViewModel.
- Mapeo vía extension functions `to<Destino>()` (`toDomain()`, `toEntity()`, `toDto()`), no clases `Mapper` con estado.

## Comandos habituales

```bash
./gradlew :composeApp:compileDebugKotlinAndroid       # verificación rápida de compilación Android
./gradlew :composeApp:compileKotlinIosSimulatorArm64  # verificación rápida de compilación iOS
./gradlew :composeApp:allTests                        # commonTest + androidUnitTest
./gradlew ktlintFormat                                # formateo
./gradlew detekt                                      # lint estático
./gradlew :composeApp:assembleDebug                   # build completo Android
```

Para cambios que toquen `commonMain`, compilar **ambos** targets antes de dar el cambio por cerrado; si no hay acceso a macOS, dejarlo explícito para que un reviewer verifique iOS.

## Configuración necesaria antes de compilar

- **Android:** archivo `local.properties` en la raíz (ignorado por git) con `GROQ_API_KEY=...`. Sin esto, `BuildConfig.GROQ_API_KEY` queda vacío pero el proyecto compila igual.
- **iOS:** archivo `Config.xcconfig` dentro de `iosApp/` (ignorado por git) con `GROQ_API_KEY = ...`, inyectado a `Info.plist` y leído vía el `actual` de `groqApiKey()`.

**Nunca** hardcodear la key en código o archivos versionados, en ninguna plataforma.

## Testing

- Preferir **fakes** sobre mocks en `commonTest`: MockK es JVM-only y no corre en `iosTest`. MockK solo en `androidUnitTest`, para lo puntual que no se pueda fakear.
- Un test por transición de estado (loading/success/error/empty), no un test gigante por clase.
- Se testea la implementación concreta (`ChatRepositoryImplTest`), no la interfaz.
- Fakes/mocks compartidos van en un paquete común de `commonTest` (ej. `testutil/` o `data/repository/fake/`).
- Journeys (Android CLI) son la validación E2E de Android; iOS no tiene hoy herramienta equivalente (validación manual).
- Detalle completo en [docs/ARCHITECTURE.md §12](./docs/ARCHITECTURE.md).

## Antes de dar por cerrado un cambio

1. Compila sin errores en Android y, si el cambio toca `commonMain`, también en iOS (ver comandos arriba).
2. `./gradlew :composeApp:allTests` en verde.
3. ktlint/detekt sin issues nuevos.
4. Nada de lógica de negocio quedó en `androidMain`/`iosMain` que pudiera vivir en `commonMain`; ningún `expect`/`actual` quedó más amplio de lo necesario.
5. Si el cambio afecta arquitectura, convenciones o comportamiento de una feature, actualizar `docs/ARCHITECTURE.md` y/o `docs/SPEC.md` (ver tabla en [CONTRIBUTING.md](./CONTRIBUTING.md#documentation-standards) para saber qué documento corresponde). Si afecta contenido de cara al usuario en el README, actualizar también `docs/README_ES.md`.
6. Nunca commitear directo a `main`; seguir el flujo de branches/commits de [CONTRIBUTING.md](./CONTRIBUTING.md).
