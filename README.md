# AI Chat App (Kotlin Multiplatform — Android & iOS)

> 📖 [Lee esto en Español](./docs/README_ES.md) | English

This is a **Kotlin Multiplatform (KMP)** mobile client, with UI built in **Compose Multiplatform**, that allows interaction with language models through **Groq**, offering a smooth chat experience with local history persistence on both **Android and iOS**. The integration is done using the `openai-kotlin` SDK, as Groq exposes an endpoint compatible with OpenAI's request format (the OpenAI API itself is not used).

> **Migration note:** this project is being migrated from an Android-only app to Kotlin Multiplatform following Clean Architecture. See [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) for the full target architecture and its current migration status.

---

## 🚀 Prerequisites

Before you begin, make sure you meet the following requirements in your development environment:

*   **Android Studio** Ladybug (or higher) — for Android development and for editing the shared (`composeApp`) Kotlin code.
*   **Xcode** (latest stable) and **macOS** — required to build and run the iOS app (`iosApp`). iOS builds cannot be produced on Linux/Windows.
*   **JDK 17** or higher.
*   A valid **Groq API Key** (free, see configuration section).

---

## 🛠️ Key Technologies

*   **Kotlin Multiplatform** (Android + iOS targets)
*   **Compose Multiplatform** & **Material 3** (shared UI across both platforms)
*   **Kotlin** with Coroutines and Flow.
*   **MVVM** with a multiplatform `ViewModel` (`org.jetbrains.androidx.lifecycle`).
*   **Koin** (Dependency Injection, multiplatform).
*   **Room Multiplatform (with KSP)** for local database, shared between platforms.
*   **openai-kotlin (Aallam)** for AI integration (built on Ktor, multiplatform out of the box).
*   **Ktor Client** with the `OkHttp` engine on Android and `Darwin` engine on iOS.

*For the general project architecture (Clean Architecture layers, module structure, DI, testing) see [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md). For the Chat feature functional details, see [docs/SPEC.md](./docs/SPEC.md).*

---

## ⚙️ Project Setup

### 1. Clone the repository
```bash
git clone https://github.com/Juanpvivas/AICha.git
cd AICha
```

### 2. Get a Groq API Key

To run the project on either platform, you need a free Groq API Key (not OpenAI).

1.  Sign up for free at the official Groq development console at [Groq Cloud](https://console.groq.com/) (recommended to use Google/Gmail direct sign-up).
2.  Generate a new key in the **API Keys** section.

You'll configure this key differently depending on the platform you want to run (steps below); you only need to do the one(s) relevant to you.

### 3. Configure the API Key — Android

**On your local machine**, create a file named `local.properties` in the **project root** (at the same level as `settings.gradle.kts` and `build.gradle.kts`), with the following content:

```properties
GROQ_API_KEY=YOUR_GROQ_API_KEY_HERE
```

> **Important:** `local.properties` is in `.gitignore`, it will never be uploaded to the repository. Android Studio/Gradle reads this variable and injects it into `BuildConfig`. Never hardcode the API Key in source code or versioned files.

### 4. Configure the API Key — iOS

Create a file named `Config.xcconfig` inside `iosApp/` (ignored by git — see `docs/ARCHITECTURE.md` §15 if it isn't yet listed in `.gitignore`), with the following content:

```
GROQ_API_KEY = YOUR_GROQ_API_KEY_HERE
```

Xcode injects this into `Info.plist`, and the shared Kotlin code reads it at runtime through the platform-specific `groqApiKey()` implementation (see `docs/ARCHITECTURE.md` §4). Never hardcode the API Key in Swift/Kotlin source files.

### 5. Build and Run — Android

1. Open the project in Android Studio.
2. Let Gradle sync the dependencies (`Sync Project with Gradle Files`).
3. Select the `composeApp` run configuration (Android app).
4. Connect a physical device or start an Android emulator.
5. Press the **Run (Shift + F10)** button.

### 6. Build and Run — iOS

1. Open `iosApp/iosApp.xcodeproj` in Xcode (or open the whole repo in Android Studio with the Kotlin Multiplatform plugin, which can also trigger iOS runs).
2. Let Xcode/Gradle resolve the shared Kotlin framework (`composeApp`) — this happens automatically as part of the Xcode build phase.
3. Select an iOS Simulator or a connected physical device as the run destination.
4. Press **Run** (⌘R).

---

## 📂 Code Structure

```text
composeApp/
├── src/
│   ├── commonMain/kotlin/com/juanpvivas/aichatjp/
│   │   ├── domain/                 # Entities, UseCases, Repository interfaces (pure Kotlin)
│   │   ├── data/                   # local/ (Room), remote/ (Groq client), repository/ (impl)
│   │   ├── di/                     # Koin modules (common)
│   │   ├── ui/                     # Compose Multiplatform screens (Route/Screen/ViewModel/UiState)
│   │   │   ├── chat/               # Chat feature
│   │   │   ├── history/            # Conversation history feature
│   │   │   └── theme/              # Colors, typography, Material 3 theme
│   │   └── core/                   # Multiplatform utilities (logger, dispatcher provider)
│   ├── androidMain/kotlin/.../      # Android actuals (Room driver, Ktor OkHttp engine, MainActivity)
│   ├── iosMain/kotlin/.../          # iOS actuals (Room driver, Ktor Darwin engine)
│   ├── commonTest/                  # Multiplatform tests (domain, data with fakes, ViewModels)
│   └── androidUnitTest/             # Android-only tests (MockK, when common fakes aren't enough)
└── build.gradle.kts

iosApp/
├── iosApp.xcodeproj
└── iosApp/App.swift                 # Native entry point: starts Koin, mounts the shared Compose UI
```

See [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) for the full Clean Architecture layering and dependency rules between packages.

---

## 🤝 Contributing

Want to contribute? Check out [CONTRIBUTING.md](./CONTRIBUTING.md) for the full workflow: branch naming, commit conventions, testing requirements, and PR guidelines (including the Android/iOS parity checklist).

---

## 🧪 Testing

Most of the test suite lives in `commonTest` and runs on both platforms without an emulator/simulator: domain logic, repositories/mappers (with fakes), and ViewModels.

```bash
./gradlew :composeApp:allTests   # runs commonTest + androidUnitTest
```

In addition, critical Android UI flows are validated with **Journeys** (Android CLI + Gemini): natural language descriptions that an AI agent executes directly on the app, taking screenshots and verifying results on screen. This tool is currently **Android-only**; there is no equivalent automated E2E coverage for iOS yet (see `docs/ARCHITECTURE.md` §14 open points), so critical iOS flows are validated manually before each release.

- Journey files (`.xml`) live in `composeApp/src/androidInstrumentedTest/journey/`.
- They are executed by asking the AI agent (Android CLI) to locate the folder and run the corresponding journey on an emulator/device.

More details about the tool in [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) §12.2.

---

## 📄 License

This project is distributed under the MIT license.
