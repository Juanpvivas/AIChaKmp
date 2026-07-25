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
- Sin literales sueltos (URLs, IDs de modelo, timeouts): `const val` en `private companion object` del mismo archivo (ver `docs/ARCHITECTURE.md` §13).
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

## Cómo validar que la app corre en iOS

El build de Android compilando no garantiza nada sobre iOS: son toolchains separadas (Gradle vs Xcode) y varios problemas solo aparecen en runtime, no en compilación. Antes de dar por cerrada una feature que tocó `commonMain`, `iosMain` o `iosApp/`:

1. **Cerrar Xcode** si hay ediciones pendientes en `iosApp/iosApp.xcodeproj/project.pbxproj` hechas a mano (por un agente, por ejemplo). Xcode reescribe el `.pbxproj` completo al abrir/guardar el proyecto; si queda abierto mientras alguien más lo edita como texto, las ediciones se pisan entre sí y el archivo puede terminar con referencias duplicadas o colgantes (ver Troubleshooting).
2. **Build & Run** desde Xcode (`Cmd+R`) contra un simulador, o headless:
   ```bash
   xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
     -destination 'platform=iOS Simulator,name=iPhone 17 Pro' build
   ```
   (La fase de Run Script del target invoca `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`, así que esto también valida que `commonMain`/`iosMain` compilan.)
3. **Confirmar que la app realmente carga** (no solo que compila): abrir el chat, ver que no haya pantalla en blanco ni crash inmediato al arrancar. Un build exitoso no significa que Koin/Room/Compose se inicialicen bien — varios de los bugs de esta sección solo truenan en el primer frame de composición.
4. **Si crashea y no hay Xcode a mano (o el proceso quedó pausado en el debugger)**, se puede diagnosticar desde terminal:
   ```bash
   xcrun simctl list devices booted                      # obtener el UDID del simulador
   ls ~/Library/Logs/DiagnosticReports/ | grep iosApp     # crash reports (.ips) del SO
   xcrun simctl launch --console-pty <UDID> com.juanpvivas.aichatjp.iosApp
   ```
   El `.ips` del sistema solo confirma el tipo de señal (normalmente `SIGABRT`/`EXC_CRASH`), pero **no** trae el mensaje real: Kotlin/Native imprime `Uncaught Kotlin exception: ...` por stderr, no queda registrado en el `.ips`. `simctl launch --console-pty` relanza la app mostrando ese stderr en vivo, con el stack Kotlin completo y las cadenas `Caused by:` anidadas.
5. **Si el error es un `org.koin.core.error.InstanceCreationException`**, no te quedes con el primer mensaje (suele ser genérico, ej. "no se pudo crear el ViewModel") — bajá hasta el **último** `Caused by:` de la cadena; ahí está la causa real.

## Troubleshooting: problemas conocidos de build/runtime en iOS

Bugs reales encontrados poniendo a andar la app en iOS por primera vez (ninguno tenía que ver con lógica de negocio — todo era wiring de la toolchain Gradle↔Xcode↔Koin↔Room). Antes de investigar desde cero un problema nuevo, chequear si encaja en esta lista:

| Síntoma | Causa | Fix |
|---|---|---|
| `No such module 'composeApp'` al compilar Swift | Gradle no genera el framework, o Xcode no lo compila antes de las fuentes Swift | En `composeApp/build.gradle.kts`, cada target iOS necesita `it.binaries.framework { baseName = "composeApp"; isStatic = true }`. En el target de Xcode, una fase **Run Script** (antes de "Compile Sources") con `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`, y `ENABLE_USER_SCRIPT_SANDBOXING = NO` |
| `Unable to open base configuration reference file .../Config.xcconfig` (o `Info.plist`) | El `path` de un `PBXGroup` se acumula con el de sus grupos padre al resolver la ubicación en disco; si el archivo referenciado no vive en esa ruta acumulada, Xcode no lo encuentra | Verificar que el grupo que contiene `Config.xcconfig`/`Info.plist` resuelva al mismo directorio físico (normalmente junto al `.xcodeproj`, **no** dentro de `iosApp/iosApp/` donde vive `App.swift`) |
| Linker: `Undefined symbol: _main` | Al `AppDelegate` en `App.swift` le falta el atributo de entry point | Agregar `@UIApplicationMain` (o `@main`) a la clase `AppDelegate` |
| Crash inmediato al arrancar, stack menciona `PlistSanityCheck.uikit.kt` | Compose Multiplatform exige `CADisableMinimumFrameDurationOnPhone` en `Info.plist` (soporte ProMotion) y aborta a propósito si falta | Agregar `<key>CADisableMinimumFrameDurationOnPhone</key><true/>` a `iosApp/Info.plist` |
| Pantalla en blanco / `InstanceCreationException` resolviendo cualquier ViewModel | Koin nunca se inicializó en iOS | Confirmar que `InitKoinKt.doInitKoin(config: nil)` se llama en `AppDelegate.application(_:didFinishLaunchingWithOptions:)` **antes** de crear el `MainViewController`. (`doInitKoin`, no `initKoin`: Kotlin/Native antepone `do` a funciones exportadas que empiezan con `init`, porque Objective-C/ARC trata los métodos `init*` como inicializadores especiales) |
| `InstanceCreationException` en cadena → `Cannot create a RoomDatabase without providing a SQLiteDriver via setDriver()` | Room Multiplatform no tiene driver por defecto en iOS (a diferencia de Android) | En el builder de Room de `iosMain/.../di/PlatformModule.kt`, agregar `.setDriver(BundledSQLiteDriver())`. Requiere la dependencia `androidx.sqlite:sqlite-bundled` en `iosMain.dependencies` — **ojo, versiona por separado de Room** (ej. Room 2.7.1 usa `androidx.sqlite` 2.5.0, no 2.7.1; confirmar la versión correcta en el árbol de dependencias transitivo si Gradle no la resuelve) |
| `InstanceCreationException` en cadena → `Cannot find the associated androidx.room.RoomDatabaseConstructor for X. Is Room annotation processor correctly configured?` | Falta el mecanismo `RoomDatabaseConstructor` que Room KMP necesita para targets no-JVM, y/o KSP no corre para los targets iOS | En `commonMain`: `@ConstructedBy(XConstructor::class)` sobre la clase `@Database`, más `expect object XConstructor : RoomDatabaseConstructor<X> { override fun initialize(): X }`. En `build.gradle.kts`: `add("kspIosArm64", libs.room.compiler)`, `add("kspIosSimulatorArm64", ...)`, `add("kspIosX64", ...)` (no alcanza con `kspAndroid`) |
| El `.pbxproj` queda con referencias duplicadas o colgantes después de abrir/editar el proyecto en Xcode | Xcode reescribe el `.pbxproj` completo al abrir/guardar; ediciones manuales concurrentes (agente + Xcode abierto a la vez) se pisan entre sí | Cerrar Xcode antes de que un agente edite el `.pbxproj` a mano; validar con `plutil -lint iosApp/iosApp.xcodeproj/project.pbxproj` después de cada edición; commitear en cuanto se confirme que compila y corre |

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
