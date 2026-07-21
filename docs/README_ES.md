# App de Chat con IA (Kotlin Multiplatform — Android & iOS)

Este es un cliente móvil **Kotlin Multiplatform (KMP)**, con la UI construida en **Compose Multiplatform**, que permite interactuar con modelos de lenguaje a través de **Groq**, ofreciendo una experiencia de chat fluida con persistencia de historial local en **Android e iOS**. La integración se realiza mediante el SDK `openai-kotlin`, ya que Groq expone un endpoint compatible con el formato de peticiones de OpenAI (no se usa la API de OpenAI en sí).

> **Nota de migración:** el proyecto está migrando de una app Android nativa a Kotlin Multiplatform siguiendo Clean Architecture. Ver [ARCHITECTURE.md](./ARCHITECTURE.md) para la arquitectura objetivo completa y su estado actual de migración.

---

## 🚀 Requisitos Previos

Antes de comenzar, asegúrate de cumplir con los siguientes requisitos en tu entorno de desarrollo:

*   **Android Studio** Ladybug (o superior) — para el desarrollo Android y para editar el código Kotlin compartido (`composeApp`).
*   **Xcode** (última versión estable) y **macOS** — necesario para compilar y correr la app de iOS (`iosApp`). Los builds de iOS no pueden generarse en Linux/Windows.
*   **JDK 17** o superior.
*   Una **API Key de Groq** válida (gratuita, ver sección de configuración).

---

## 🛠️ Tecnologías Clave

*   **Kotlin Multiplatform** (targets Android + iOS)
*   **Compose Multiplatform** & **Material 3** (UI compartida entre ambas plataformas)
*   **Kotlin** con Corrutinas y Flow.
*   **MVVM** con `ViewModel` multiplatform (`org.jetbrains.androidx.lifecycle`).
*   **Koin** (Inyección de dependencias, multiplatform).
*   **Room Multiplatform (con KSP)** para base de datos local, compartida entre plataformas.
*   **openai-kotlin (Aallam)** para la integración con IA (construido sobre Ktor, multiplatform de fábrica).
*   **Ktor Client** con engine `OkHttp` en Android y engine `Darwin` en iOS.

*Para la arquitectura general del proyecto (capas de Clean Architecture, estructura de módulos, DI, testing) consulta [ARCHITECTURE.md](./ARCHITECTURE.md). Para el detalle funcional de la feature de Chat, consulta [SPEC.md](./SPEC.md).*

---

## ⚙️ Configuración del Proyecto

### 1. Clonar el repositorio
```bash
git clone https://github.com/Juanpvivas/AICha.git
cd AICha
```

### 2. Obtener una API Key de Groq

Para correr el proyecto en cualquiera de las dos plataformas, es necesario contar con una API Key gratuita de Groq (no de OpenAI).

1.  Regístrate de forma gratuita en la consola oficial de desarrollo de Groq en [Groq Cloud](https://console.groq.com/) (se recomienda usar registro directo por Google/Gmail).
2.  Genera una nueva clave en la sección **API Keys**.

La forma de configurar esta clave difiere según la plataforma que quieras correr (pasos abajo); solo necesitas hacer la que te aplique.

### 3. Configurar la API Key — Android

**En tu máquina local**, crea un archivo llamado `local.properties` en la **raíz del proyecto** (al mismo nivel que `settings.gradle.kts` y `build.gradle.kts`), con el siguiente contenido:

```properties
GROQ_API_KEY=TU_API_KEY_DE_GROQ_AQUI
```

> **Importante:** `local.properties` está en `.gitignore`, nunca subirá al repositorio. Android Studio/Gradle lee esta variable y la inyecta en `BuildConfig`. Nunca hardcodees la API Key en el código fuente ni en archivos que se versionan.

### 4. Configurar la API Key — iOS

Crea un archivo llamado `Config.xcconfig` dentro de `iosApp/` (ignorado por git — ver `ARCHITECTURE.md` §15 si aún no figura en `.gitignore`), con el siguiente contenido:

```
GROQ_API_KEY = TU_API_KEY_DE_GROQ_AQUI
```

Xcode inyecta esto en `Info.plist`, y el código Kotlin compartido lo lee en tiempo de ejecución a través de la implementación específica de plataforma de `groqApiKey()` (ver `ARCHITECTURE.md` §4). Nunca hardcodees la API Key en archivos fuente de Swift/Kotlin.

### 5. Compilar y Ejecutar — Android

1. Abre el proyecto en Android Studio.
2. Deja que Gradle sincronice las dependencias (`Sync Project with Gradle Files`).
3. Selecciona la configuración de ejecución `composeApp` (app Android).
4. Conecta un dispositivo físico o inicia un emulador Android.
5. Presiona el botón **Run (Shift + F10)**.

### 6. Compilar y Ejecutar — iOS

1. Abre `iosApp/iosApp.xcodeproj` en Xcode (o abre todo el repo en Android Studio con el plugin de Kotlin Multiplatform, que también puede disparar ejecuciones en iOS).
2. Deja que Xcode/Gradle resuelva el framework Kotlin compartido (`composeApp`) — esto ocurre automáticamente como parte de la fase de build de Xcode.
3. Selecciona un simulador de iOS o un dispositivo físico conectado como destino de ejecución.
4. Presiona **Run** (⌘R).

---

## 📂 Estructura del Código

```text
composeApp/
├── src/
│   ├── commonMain/kotlin/com/juanpvivas/aichatjp/
│   │   ├── domain/                 # Entidades, UseCases, interfaces de Repository (Kotlin puro)
│   │   ├── data/                   # local/ (Room), remote/ (cliente Groq), repository/ (impl)
│   │   ├── di/                     # Módulos Koin (comunes)
│   │   ├── ui/                     # Pantallas Compose Multiplatform (Route/Screen/ViewModel/UiState)
│   │   │   ├── chat/               # Feature de chat
│   │   │   ├── history/            # Feature de historial de conversaciones
│   │   │   └── theme/              # Colores, tipografías, tema Material 3
│   │   └── core/                   # Utilidades multiplatform (logger, dispatcher provider)
│   ├── androidMain/kotlin/.../      # Actuals de Android (driver Room, engine Ktor OkHttp, MainActivity)
│   ├── iosMain/kotlin/.../          # Actuals de iOS (driver Room, engine Ktor Darwin)
│   ├── commonTest/                  # Tests multiplatform (domain, data con fakes, ViewModels)
│   └── androidUnitTest/             # Tests específicos de Android (MockK, cuando los fakes comunes no alcanzan)
└── build.gradle.kts

iosApp/
├── iosApp.xcodeproj
└── iosApp/App.swift                 # Entry point nativo: arranca Koin, monta la UI compartida de Compose
```

Ver [ARCHITECTURE.md](./ARCHITECTURE.md) para el detalle completo de las capas de Clean Architecture y las reglas de dependencia entre paquetes.

---

## 🤝 Contribuciones

¿Quieres contribuir? Consulta [../CONTRIBUTING.md](../CONTRIBUTING.md) (en inglés) para el flujo completo: nomenclatura de branches, convenciones de commits, requisitos de testing y guía de PRs (incluyendo el checklist de paridad Android/iOS).

---

## 🧪 Testing

La mayor parte de la suite de tests vive en `commonTest` y corre en ambas plataformas sin necesidad de emulador/simulador: lógica de dominio, repositorios/mappers (con fakes) y ViewModels.

```bash
./gradlew :composeApp:allTests   # corre commonTest + androidUnitTest
```

Además, los flujos críticos de UI en Android se validan con **Journeys** (Android CLI + Gemini): descripciones en lenguaje natural que un agente de IA ejecuta directamente sobre la app, tomando screenshots y verificando el resultado en pantalla. Esta herramienta es hoy **exclusiva de Android**; no existe aún una cobertura E2E automatizada equivalente para iOS (ver el punto abierto en `ARCHITECTURE.md` §16), por lo que los flujos críticos en iOS se validan manualmente antes de cada release.

- Los archivos de journey (`.xml`) viven en `composeApp/src/androidInstrumentedTest/journey/`.
- Se ejecutan pidiéndole al agente de IA (Android CLI) que localice la carpeta y corra el journey correspondiente sobre un emulador/dispositivo.

Más detalle de la herramienta en [ARCHITECTURE.md](./ARCHITECTURE.md) §12.2.

---

## 📄 Licencia

Este proyecto se distribuye bajo la licencia MIT.
