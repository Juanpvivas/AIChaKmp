# Contributing to AICha

Thank you for your interest in contributing to **AICha**! This document outlines the guidelines and workflow for contributing to this project.

> **KMP migration:** the project is migrating from Android-only to Kotlin Multiplatform (Android + iOS) with Clean Architecture. If you're new here, read [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) first — it defines the target module structure, layers, and conventions referenced throughout this document.

---

## 📋 Table of Contents

1. [Getting Started](#getting-started)
2. [Development Workflow](#development-workflow)
3. [Code Standards](#code-standards)
4. [Documentation Standards](#documentation-standards)
5. [Testing Requirements](#testing-requirements)
6. [Commit and PR Guidelines](#commit-and-pr-guidelines)
7. [Code Review Process](#code-review-process)

---

## Getting Started

Before contributing, make sure your local environment is set up and the project runs correctly. Follow the **full setup guide in [README.md](./README.md)** — prerequisites, Groq API Key configuration (Android and iOS), and how to build/run the app on each platform.

Once you've completed that setup, verify everything compiles on both targets:
```bash
./gradlew :composeApp:compileDebugKotlinAndroid
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

If either fails, revisit the [README.md setup section](./README.md#️-project-setup) before continuing — most issues at this stage are a missing/misconfigured `local.properties` (Android) or `Config.xcconfig` (iOS). If you don't have access to a macOS machine, you can still contribute to `commonMain`/`androidMain` code, but flag in your PR description that the iOS build wasn't verified locally so a reviewer with macOS access can check it.

---

## Development Workflow

### 1. Create a Feature Branch

**Never work directly on `main`.** All changes, no matter how small, must go through a feature branch.

```bash
git checkout main
git pull origin main
git checkout -b feature/descriptive-name
```

**Branch naming convention:**
- Use lowercase and hyphens: `feature/add-history-search`, `fix/chat-message-overflow`, `refactor/viewmodel-state`
- Prefix with type: `feature/`, `fix/`, `refactor/`, `chore/`, `test/`, `docs/`
- Keep it short and descriptive (aim for <50 characters)

### 2. Develop Your Changes

- Follow the architecture and conventions defined in [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md), in particular:
  - **Respect the Clean Architecture dependency rule:** `domain` never depends on `data`/`ui`/platform SDKs; `ui` talks to `domain` through use cases, never directly to repositories or DAOs.
  - **Default to `commonMain`.** Only write code in `androidMain`/`iosMain` when it genuinely needs a platform API (Room database builder, Ktor engine, native entry point). If you're not sure whether something needs to be platform-specific, it probably doesn't.
  - Keep `expect`/`actual` blocks as small as possible — one function/class per platform concern, never a duplicated layer.
- Write clean, testable code (see [Code Standards](#code-standards)).
- Compile frequently to catch errors early on **both** platforms:
```bash
./gradlew :composeApp:compileDebugKotlinAndroid
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

### 3. Test Your Changes

- Write unit tests for new logic (UseCase, Repository, ViewModel, Mappers) in `commonTest` whenever the code lives in `commonMain` — see [Testing Requirements](#testing-requirements).
- Run the test suite before pushing:
```bash
./gradlew :composeApp:allTests
```
- If your change affects Android UI behavior, consider adding a Journey test (Android-only, see [Testing Requirements](#testing-requirements)). If it affects iOS-only behavior, describe the manual verification you performed in the PR.

### 4. Update Documentation

**This is mandatory.** If your change affects architecture, introduces new conventions, or changes existing behavior, you **must** update the relevant documentation.

See [Documentation Standards](#documentation-standards) for details.

### 5. Push and Create a Pull Request

```bash
git push origin feature/your-feature-name
```

Then open a Pull Request (PR) on GitHub toward the `main` branch. Use the PR template (below) and provide:
- Clear description of what changed and why
- Reference to any related issues
- Which platforms you built/tested locally (Android, iOS, or both)
- Links to updated documentation (if applicable)

---

## Code Standards

### Kotlin Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use **ktlint** to format code (runs in CI; fix locally with `./gradlew ktlintFormat`)
- Use **detekt** for linting (runs in CI; check locally with `./gradlew detekt`)

### Naming Conventions

Follow the conventions defined in [docs/ARCHITECTURE.md §5-§7](./docs/ARCHITECTURE.md):

- **Domain Layer:** `ChatMessage`, `Conversation` (no suffix — clean name = domain model); `ChatRepository`/`ConversationRepository` (interfaces); `SendMessageUseCase`, `ObserveConversationHistoryUseCase` (use cases, one business action per class)
- **Data Layer (remote):** `ChatRemoteDataSource`, `ChatMessageDto`, `ChatMessageMapper`/`ChatMessageMappers`
- **Data Layer (local):** `ChatMessageEntity`, `ChatMessageDao`, `AiChaDatabase`
- **Repository implementation:** `ChatRepositoryImpl` (implements the `domain/repository/` interface)
- **ViewModels:** `ChatViewModel` (extends the multiplatform `ViewModel`, never `androidx.lifecycle.ViewModel` directly)
- **UI State:** `ChatUiState` (sealed interface with states: `Loading`, `Success`, `Error`, `Empty`)
- **Composables:** `ChatScreen` (full screen), `ChatContent` (section), `ChatHeader` (component)

### No Hardcoded Strings

All user-facing text must come from Compose Multiplatform resources (`composeResources`), not `androidx` string resources:
```kotlin
// ❌ Don't do this:
Text("Send Message")

// ✅ Do this:
Text(stringResource(Res.string.send_message))
```

### Architecture Compliance

- **Unidirectional Data Flow (UDF):** UI → ViewModel → UseCase → Repository → Data Layer (never backwards)
- **Dependency Injection:** Use Koin; avoid hardcoding dependencies or instantiating repositories/data sources by hand in `ui/`
- **Immutable State:** Use `StateFlow<UiState>` in ViewModels, never mutable state in UI
- **No SDK Leaks:** DTOs, Room entities, and the Groq SDK's own types never cross outside `data/remote/`/`data/local/`
- **Platform code stays platform code:** nothing in `androidMain`/`iosMain` contains business logic; it's only the glue to the native API behind an `expect` declaration

For more details, see [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md).

---

## Documentation Standards

### When to Update Documentation

You **must** update documentation if your change:

- ✅ Adds or modifies a layer, component, or module structure
- ✅ Introduces new naming conventions or patterns
- ✅ Changes how a feature works (behavior contract)
- ✅ Updates API keys configuration, build setup, or environment variables (Android and/or iOS)
- ✅ Modifies testing strategy or test locations
- ✅ Adds new technologies, libraries, or integrations
- ✅ Adds/changes an `expect`/`actual` declaration or platform-specific module

### What to Update

| Change Type | Document(s) to Update |
|---|---|
| New feature (chat, history, etc.) | Create/update a `docs/SPEC.md` entry for the feature, update README if user-facing |
| Architecture change (layers, patterns, modules) | Update `docs/ARCHITECTURE.md` with new section/subsection |
| Naming conventions, folder structure | Update relevant section in `docs/ARCHITECTURE.md` §5-§7 |
| New dependency (Koin modules, etc.) | Update `docs/ARCHITECTURE.md` §2 (Stack) if major; §9 for DI details |
| New/changed `expect`/`actual` | Update `docs/ARCHITECTURE.md` §4 |
| Testing approach | Update `docs/ARCHITECTURE.md` §12 (Testing) |
| Build setup, CI/CD | Update `docs/ARCHITECTURE.md` §14 and `README.md` |
| Security concerns (secrets, keys, either platform) | Update `docs/ARCHITECTURE.md` §15 (Security) |

### How to Update Documentation

1. **Be precise:** document the "why" and "how", not just the "what"
2. **Use examples:** code snippets, directory trees, and tables make documentation clearer
3. **Link related sections:** use `§N` notation to reference other sections
4. **Keep it DRY:** avoid repeating the same rule in multiple places — link to the canonical source instead
5. **Update translations:** if you edit English docs, translate the same section to `docs/README_ES.md` if it affects user-facing content
6. **Mind both platforms:** when describing setup, build commands, or behavior, be explicit about whether something applies to Android, iOS, or both

### Documentation Review

Your PR **cannot be merged** if:
- You changed architecture/behavior but didn't update docs
- Your docs are vague or incomplete
- Your docs contradict the actual code
- Your docs describe Android-only behavior as if it applied to iOS too (or vice versa) without saying so

Reviewers will request updates before approval.

---

## Testing Requirements

### Unit Tests (Mandatory for `domain/` and `data/`, strongly recommended for `ui/`)

**Location:** `composeApp/src/commonTest/kotlin/` (mirroring the package structure of the class being tested), **not** `androidUnitTest`/`iosTest`, unless the code under test is genuinely platform-specific.

**Naming:** `<ClassName>Test.kt` (e.g., `ChatViewModelTest.kt`, `ChatRepositoryImplTest.kt`, `SendMessageUseCaseTest.kt`)

**Coverage:**
- **UseCase:** test the business rule in isolation, with fakes for its repository dependency
- **Repository:** test mappers, error handling, and data source interaction (with fakes for `RemoteDataSource`/DAO)
- **ViewModel:** test state transitions (loading → success → error for each scenario)
- **Mappers:** test DTO/Entity → Domain conversion edge cases

**Prefer fakes over mocks.** MockK is JVM-only and unavailable in `iosTest`; the project default for `commonTest` is hand-written fakes. Only fall back to `androidUnitTest` + MockK for the rare case that can't be reasonably faked and is genuinely Android-specific.

**Run locally before pushing:**
```bash
./gradlew :composeApp:allTests
```

### Compose UI Tests (Recommended for complex screens, Android)

**Location:** `composeApp/src/androidInstrumentedTest/kotlin/` (for Compose testing, not Journeys)

**Coverage:** test that states render correctly (success state shows content, error state shows error message, etc.)

### Journeys (For critical end-to-end flows, Android-only)

**Location:** `composeApp/src/androidInstrumentedTest/journey/`

**When to add:** for critical user flows that must pass end-to-end (e.g., "send a message and receive a response")

**Naming:** `<flow-name>.xml` (e.g., `send_message.xml`, `start_conversation.xml`)

**iOS:** there is no equivalent automated tool today (tracked as an open point in `docs/ARCHITECTURE.md` §16). If your change affects a critical iOS flow, describe your manual verification steps in the PR.

See [docs/ARCHITECTURE.md §12.2](./docs/ARCHITECTURE.md) for more details.

### Running Tests

```bash
# Common + Android unit tests
./gradlew :composeApp:allTests

# Android instrumented tests / Journeys
./gradlew :composeApp:connectedAndroidTest
```

---

## Commit and PR Guidelines

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
type(scope): short description

Optional longer description explaining the change.

Fixes #123
```

**Types:** `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `perf`

**Examples:**
```
feat(chat): add message retry on network error
fix(ui): prevent chat input overlap on landscape mode
docs(architecture): add naming conventions for data layer
test(repository): add mapper error handling tests
```

### Pull Request Template

When opening a PR, include:

```markdown
## Description
Brief summary of the change.

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Refactoring
- [ ] Documentation update
- [ ] Architecture change

## Related Issue
Fixes #(issue number)

## Platforms Verified
- [ ] Android (built/ran locally)
- [ ] iOS (built/ran locally)
- [ ] Only one platform verified (explain why: ...)

## Documentation Updated
- [ ] Yes (link: ...)
- [ ] No (explain why: ...)

## Testing
- [ ] Unit tests added/updated (commonTest)
- [ ] Manual testing performed (state which platform(s))
- [ ] Journey tests added (Android, if end-to-end flow changed)

## Checklist
- [ ] Code compiles on Android (`./gradlew :composeApp:compileDebugKotlinAndroid`)
- [ ] Code compiles on iOS (`./gradlew :composeApp:compileKotlinIosSimulatorArm64`)
- [ ] All tests pass (`./gradlew :composeApp:allTests`)
- [ ] Code follows style guide (ktlint + detekt)
- [ ] No hardcoded strings (all text via Compose Multiplatform resources)
- [ ] Architecture rules followed (Clean Architecture layers, DI, `expect`/`actual` scope, naming)
- [ ] Documentation updated (if applicable)
```

---

## Code Review Process

### Before Submitting Your PR

1. **Compile:** both `./gradlew :composeApp:compileDebugKotlinAndroid` and `./gradlew :composeApp:compileKotlinIosSimulatorArm64` ✅ (no errors)
2. **Lint & Format:** ktlint and detekt pass (CI will check)
3. **Tests:** `./gradlew :composeApp:allTests` ✅ (all pass)
4. **Documentation:** Updated (if applicable)
5. **Architecture:** Follows conventions from `docs/ARCHITECTURE.md`, including the Clean Architecture dependency rule and `expect`/`actual` scoping

### During Review

**Reviewers will check:**
- Code quality and architecture compliance (domain/data/presentation boundaries respected)
- Whether code that could live in `commonMain` was unnecessarily duplicated into `androidMain`/`iosMain`
- Test coverage and correctness
- Documentation accuracy and completeness
- No security/privacy issues (secrets never hardcoded, on either platform)
- Performance implications

**Common reasons for rejection:**
- ❌ Architecture violated (e.g., DTOs/entities leaking to `ui/`, ViewModel calling a repository directly instead of a use case)
- ❌ Platform-specific code that didn't need to be (should have been `commonMain`)
- ❌ No tests or tests don't pass
- ❌ Documentation not updated when it should be
- ❌ Hardcoded strings in code
- ❌ Dependency injection not used (manual instantiation instead of Koin)
- ❌ Compilation errors on either platform

**Approval & Merge:**
- Requires **at least 1 approval** from a maintainer
- All CI checks must pass (lint, tests, Android build, iOS build)
- Branch must be up-to-date with `main` (no merge conflicts)
- Squash commits if requested by reviewers

---

## Questions or Need Help?

- Check [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) first — most answers are there
- Review existing PRs and issues for similar questions
- Ask in a GitHub Issue or Discussion before starting large changes

---

## License

By contributing to AICha, you agree that your contributions will be licensed under the [MIT License](./LICENSE).

Thank you for contributing! 🚀
