# AI Chat App (Android)

> 📖 [Lee esto en Español](./docs/README_ES.md) | English

This is a native Android mobile client that allows interaction with language models through **Groq**, offering a smooth chat experience with local history persistence. The integration is done using the `openai-kotlin` SDK, as Groq exposes an endpoint compatible with OpenAI's request format (the OpenAI API itself is not used).

---

## 🚀 Prerequisites

Before you begin, make sure you meet the following requirements in your development environment:

*   **Android Studio** Ladybug (or higher).
*   **JDK 17** or higher.
*   A valid **Groq API Key** (free, see configuration section).

---

## 🛠️ Key Technologies

*   **Jetpack Compose** & **Material 3** (UI)
*   **Kotlin** with Coroutines and Flow.
*   **MVVM** (Model-View-ViewModel).
*   **Hilt** (Dependency Injection).
*   **Room (with KSP)** for local database.
*   **openai-kotlin (Aallam)** for AI integration.

*For the general project architecture (layers, conventions, DI, testing) see [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md). For the Chat feature functional details, see [SPEC.md](./SPEC.md).*

---

## ⚙️ Project Setup

### 1. Clone the repository
```bash
git clone https://github.com/Juanpvivas/AICha.git
cd AICha
```

### 2. Get and configure the Groq API Key

To run the project, you need a free Groq API Key (not OpenAI).

1.  Sign up for free at the official Groq development console at [Groq Cloud](https://console.groq.com/) (recommended to use Google/Gmail direct sign-up).
2.  Generate a new key in the **API Keys** section.
3.  **On your local machine**, create a file named `local.properties` in the **project root** (at the same level as `settings.gradle.kts` and `build.gradle.kts`), with the following content:

```properties
GROQ_API_KEY=YOUR_GROQ_API_KEY_HERE
```

> **Important:** The `local.properties` file is in `.gitignore`, it will never be uploaded to the repository — it's your local and secure configuration. Never hardcode the API Key in source code or versioned files.

4.  Android Studio will automatically read this variable and inject it into `BuildConfig` (if configured in `build.gradle.kts`). If you have questions about how to access it from code, check the project's Gradle configuration.

### 3. Build and Run
1. Open the project in Android Studio.
2. Let Gradle sync the dependencies (`Sync Project with Gradle Files`).
3. Connect a physical device or start an Android emulator.
4. Press the **Run (Shift + F10)** button.

---

## 📂 Code Structure

```text
app/
├── src/main/java/com/juanpvivas/aichatjp/
│   ├── data/                 # Repositories, Database (Room) and Groq client (openai-kotlin)
│   ├── di/                   # Dependency injection modules with Hilt
│   ├── ui/                   # Presentation Layer (Compose)
│   │   ├── chat/             # Chat Functionality (Package-by-Feature)
│   │   │   ├── ChatRoute.kt  # State connector (ViewModel, Navigation)
│   │   │   ├── ChatScreen.kt # Stateless Interface (main Scaffold)
│   │   │   ├── ChatViewModel.kt
│   │   │   ├── ChatUiState.kt# Screen state (Loading, Success, Error)
│   │   │   └── components/   # Extracted specific Composables
│   │   │       ├── ChatHeader.kt # Chat header bar
│   │   │       ├── ChatContent.kt# Message list (MessageBubble, etc.)
│   │   │       └── ChatFooter.kt # Text input bar (ChatInputBar)
│   │   └── theme/            # Colors, typography, and Material 3 theme
│   └── MainActivity.kt
└── build.gradle.kts
```

---

## 🤝 Contributing

Want to contribute? Check out [CONTRIBUTING.md](./CONTRIBUTING.md) for the full workflow: branch naming, commit conventions, testing requirements, and PR guidelines.

---

## 🧪 Testing

In addition to standard unit tests (`./gradlew test`), the project validates complete UI flows with **Journeys** (Android CLI + Gemini): natural language descriptions that an AI agent executes directly on the app, taking screenshots and verifying results on screen.

- Journey files (`.xml`) live in `app/src/androidTest/jurney/`, within the `androidTest` source set.
- They are executed by asking the AI agent (Android CLI) to locate the folder and run the corresponding journey on an emulator/device. By living within `androidTest`, they can also be run via Gradle if integrated into CI later.

More details about the tool in [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) §10.2.

---

## 📄 License

This project is distributed under the MIT license.