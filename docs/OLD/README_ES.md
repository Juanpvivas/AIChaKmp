# App de Chat con IA (Android)

Este es un cliente móvil nativo para Android que permite interactuar con modelos de lenguaje a través de **Groq**, ofreciendo una experiencia de chat fluida con persistencia de historial local. La integración se realiza mediante el SDK `openai-kotlin`, ya que Groq expone un endpoint compatible con el formato de peticiones de OpenAI (no se usa la API de OpenAI en sí).

---

## 🚀 Requisitos Previos

Antes de comenzar, asegúrate de cumplir con los siguientes requisitos en tu entorno de desarrollo:

*   **Android Studio** Ladybug (o superior).
*   **JDK 17** o superior.
*   Una **API Key de Groq** válida (gratuita, ver sección de configuración).

---

## 🛠️ Tecnologías Clave

*   **Jetpack Compose** & **Material 3** (UI)
*   **Kotlin** con Corrutinas y Flow.
*   **MVVM** (Model-View-ViewModel).
*   **Hilt** (Inyección de dependencias).
*   **Room (con KSP)** para base de datos local.
*   **openai-kotlin (Aallam)** para la integración con IA.

*Para la arquitectura general del proyecto (capas, convenciones, DI, testing) consulta [ARCHITECTURE.md](./ARCHITECTURE.md). Para el detalle funcional de la feature de Chat, consulta [../SPEC.md](../SPEC.md).*

---

## ⚙️ Configuración del Proyecto

### 1. Clonar el repositorio
```bash
git clone https://github.com/Juanpvivas/AICha.git
cd AICha
```

### 2. Obtener y configurar la API Key de Groq

Para correr el proyecto, es necesario contar con una API Key gratuita de Groq (no de OpenAI).

1.  Regístrate de forma gratuita en la consola oficial de desarrollo de Groq en [Groq Cloud](https://console.groq.com/) (se recomienda usar registro directo por Google/Gmail).
2.  Genera una nueva clave en la sección **API Keys**.
3.  **En tu máquina local**, crea un archivo llamado `local.properties` en la **raíz del proyecto** (al mismo nivel que `settings.gradle.kts` y `build.gradle.kts`), con el siguiente contenido:

```properties
GROQ_API_KEY=TU_API_KEY_DE_GROQ_AQUI
```

> **Importante:** El archivo `local.properties` está en `.gitignore`, nunca subirá al repositorio — es tu configuración local y segura. Nunca hardcodees la API Key en el código fuente ni en archivos que se versionan.

4.  Android Studio leerá automáticamente esta variable y la inyectará en `BuildConfig` (si está configurada en `build.gradle.kts`). Si tienes dudas sobre cómo acceder a ella desde el código, consulta la configuración de Gradle del proyecto.

### 3. Compilar y Ejecutar
1. Abre el proyecto en Android Studio.
2. Deja que Gradle sincronice las dependencias (`Sync Project with Gradle Files`).
3. Conecta un dispositivo físico o inicia un emulador Android.
4. Presiona el botón **Run (Shift + F10)**.

---

## 📂 Estructura del Código

```text
app/
├── src/main/java/com/juanpvivas/aichatjp/
│   ├── data/                 # Repositorios, Base de datos (Room) y cliente de Groq (openai-kotlin)
│   ├── di/                   # Módulos de inyección de dependencias con Hilt
│   ├── ui/                   # Capa de Presentación (Compose)
│   │   ├── chat/             # Funcionalidad del Chat (Package-by-Feature)
│   │   │   ├── ChatRoute.kt  # Conector con estado (ViewModel, Navegación)
│   │   │   ├── ChatScreen.kt # Interfaz Stateless (Scaffold principal)
│   │   │   ├── ChatViewModel.kt
│   │   │   ├── ChatUiState.kt# Estado de la pantalla (Loading, Success, Error)
│   │   │   └── components/   # Composables específicos extraídos
│   │   │       ├── ChatHeader.kt # Barra superior del chat
│   │   │       ├── ChatContent.kt# Lista de mensajes (MessageBubble, etc.)
│   │   │       └── ChatFooter.kt # Barra de entrada de texto (ChatInputBar)
│   │   └── theme/            # Colores, tipografías y tema de Material 3
│   └── MainActivity.kt
└── build.gradle.kts
```

---

## 🤝 Contribuciones

¿Quieres contribuir? Consulta [../CONTRIBUTING.md](../CONTRIBUTING.md) (en inglés) para el flujo completo: nomenclatura de branches, convenciones de commits, requisitos de testing y guía de PRs.

---

## 🧪 Testing

Además de los tests unitarios estándar (`./gradlew test`), el proyecto valida flujos completos de UI con **Journeys** (Android CLI + Gemini): descripciones en lenguaje natural que un agente de IA ejecuta directamente sobre la app, tomando screenshots y verificando el resultado en pantalla.

- Los archivos de journey (`.xml`) viven en `app/src/androidTest/jurney/`, dentro del `androidTest` source set.
- Se ejecutan pidiéndole al agente de IA (Android CLI) que localice la carpeta y corra el journey correspondiente sobre un emulador/dispositivo. Al vivir dentro de `androidTest`, también se pueden correr vía Gradle si se integran a CI más adelante.

Más detalle de la herramienta en [ARCHITECTURE.md](./ARCHITECTURE.md) §10.2.

---

## 📄 Licencia

Este proyecto se distribuye bajo la licencia MIT.