# Contributing to AICha

Thank you for your interest in contributing to **AICha**! This document outlines the guidelines and workflow for contributing to this project.

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

Before contributing, make sure your local environment is set up and the project runs correctly. Follow the **full setup guide in [README.md](./README.md)** — prerequisites, Groq API Key configuration, and how to build/run the app.

Once you've completed that setup, verify everything compiles:
```bash
./gradlew compileDebugKotlin
```

If this fails, revisit the [README.md setup section](./README.md#️-project-setup) before continuing — most issues at this stage are a missing/misconfigured `local.properties`.

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

- Follow the architecture and conventions defined in [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md)
- Write clean, testable code (see [Code Standards](#code-standards))
- Compile frequently to catch errors early:
```bash
./gradlew compileDebugKotlin
```

### 3. Test Your Changes

- Write unit tests for new logic (Repository, ViewModel, Mappers)
- Run the test suite before pushing:
```bash
./gradlew test
```
- If your change affects UI behavior, consider adding a Journey test (see [Testing Requirements](#testing-requirements))

### 4. Update Documentation

**This is mandatory.** If your change affects architecture, introduces new conventions, or changes existing behavior, you **must** update the relevant documentation.

See [Documentation Standards](#documentation-standards) for details.

### 5. Push and Create a Pull Request

```bash
git push origin feature/your-feature-name
```

Then open a Pull Request (PR) on GitHub toward the `main` branch. Use the PR template (if available) and provide:
- Clear description of what changed and why
- Reference to any related issues
- Links to updated documentation (if applicable)

---

## Code Standards

### Kotlin Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use **ktlint** to format code (runs in CI; fix locally with `./gradlew ktlintFormat`)
- Use **detekt** for linting (runs in CI; check locally with `./gradlew detekt`)

### Naming Conventions

Follow the conventions defined in [docs/ARCHITECTURE.md §4](./docs/ARCHITECTURE.md):

- **Data Layer (remote):** `ChatRemoteDataSource`, `ChatMessageDto`, `ChatMessageMapper`
- **Data Layer (local):** `ChatMessageEntity`, `ChatMessageDao`, `AiChaDatabase`
- **Repository:** `ChatRepository` (interface), `ChatRepositoryImpl` (implementation)
- **Domain Models:** `ChatMessage` (no suffix — clean name = domain model)
- **ViewModels:** `ChatViewModel`
- **UI State:** `ChatUiState` (sealed interface with states: `Loading`, `Success`, `Error`, `Empty`)
- **Composables:** `ChatScreen` (full screen), `ChatContent` (section), `ChatHeader` (component)

### No Hardcoded Strings

All user-facing text must come from `strings.xml`:
```kotlin
// ❌ Don't do this:
Text("Send Message")

// ✅ Do this:
Text(stringResource(R.string.send_message))
```

### Architecture Compliance

- **Unidirectional Data Flow (UDF):** UI → ViewModel → Repository → Data Layer (never backwards)
- **Dependency Injection:** Use Hilt; avoid hardcoding dependencies
- **Immutable State:** Use `StateFlow<UiState>` in ViewModels, never mutable state in UI
- **No SDK Leaks:** DTOs and Groq SDK types never cross outside `data/remote/`

For more details, see [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md).

---

## Documentation Standards

### When to Update Documentation

You **must** update documentation if your change:

- ✅ Adds or modifies a layer, component, or module structure
- ✅ Introduces new naming conventions or patterns
- ✅ Changes how a feature works (behavior contract)
- ✅ Updates API keys configuration, build setup, or environment variables
- ✅ Modifies testing strategy or test locations
- ✅ Adds new technologies, libraries, or integrations

### What to Update

| Change Type | Document(s) to Update |
|---|---|
| New feature (chat, history, etc.) | Create `SPEC.md` entry for the feature, update README if user-facing |
| Architecture change (layers, patterns) | Update `docs/ARCHITECTURE.md` with new section/subsection |
| Naming conventions, folder structure | Update relevant section in `docs/ARCHITECTURE.md` §4 |
| New dependency (Hilt modules, etc.) | Update `docs/ARCHITECTURE.md` §2 (Stack) if major; §6-9 (relevant section) for details |
| Testing approach | Update `docs/ARCHITECTURE.md` §10 (Testing) |
| Build setup, CI/CD | Update `docs/ARCHITECTURE.md` §11-12 and `README.md` |
| Security concerns (secrets, keys) | Update `docs/ARCHITECTURE.md` §13 (Security) |

### How to Update Documentation

1. **Be precise:** document the "why" and "how", not just the "what"
2. **Use examples:** code snippets, directory trees, and tables make documentation clearer
3. **Link related sections:** use `§N` notation to reference other sections
4. **Keep it DRY:** avoid repeating the same rule in multiple places — link to the canonical source instead
5. **Update translations:** if you edit English docs, translate the same section to `docs/README_ES.md` if it affects user-facing content

### Documentation Review

Your PR **cannot be merged** if:
- You changed architecture/behavior but didn't update docs
- Your docs are vague or incomplete
- Your docs contradict the actual code

Reviewers will request updates before approval.

---

## Testing Requirements

### Unit Tests (Mandatory for `data/` and `ui/`)

**Location:** `app/src/test/java/` (mirroring the package structure of the class being tested)

**Naming:** `<ClassName>Test.kt` (e.g., `ChatViewModelTest.kt`, `ChatRepositoryImplTest.kt`)

**Coverage:**
- **Repository:** test mappers, error handling, and data source interaction
- **ViewModel:** test state transitions (loading → success → error for each scenario)
- **Mappers:** test DTO/Entity → Domain conversion edge cases

**Run locally before pushing:**
```bash
./gradlew test
```

### Compose UI Tests (Recommended for complex screens)

**Location:** `app/src/androidTest/java/` (for Compose testing, not Journeys)

**Coverage:** test that states render correctly (success state shows content, error state shows error message, etc.)

### Journeys (For critical end-to-end flows)

**Location:** `app/src/androidTest/jurney/` (following Android CLI convention)

**When to add:** for critical user flows that must pass end-to-end (e.g., "send a message and receive a response")

**Naming:** `<flow-name>.xml` (e.g., `send_message.xml`, `start_conversation.xml`)

See [docs/ARCHITECTURE.md §10.2](./docs/ARCHITECTURE.md) for more details.

### Running Tests

```bash
# Unit tests
./gradlew test

# Run all tests
./gradlew testDebug
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

## Documentation Updated
- [ ] Yes (link: ...)
- [ ] No (explain why: ...)

## Testing
- [ ] Unit tests added/updated
- [ ] Manual testing performed
- [ ] Journey tests added (if end-to-end flow changed)

## Checklist
- [ ] Code compiles without errors (`./gradlew compileDebugKotlin`)
- [ ] All tests pass (`./gradlew test`)
- [ ] Code follows style guide (ktlint + detekt)
- [ ] No hardcoded strings (all text in `strings.xml`)
- [ ] Architecture rules followed (DI, layers, naming)
- [ ] Documentation updated (if applicable)
```

---

## Code Review Process

### Before Submitting Your PR

1. **Compile:** `./gradlew compileDebugKotlin` ✅ (no errors)
2. **Lint & Format:** ktlint and detekt pass (CI will check)
3. **Tests:** `./gradlew test` ✅ (all pass)
4. **Documentation:** Updated (if applicable)
5. **Architecture:** Follows conventions from `docs/ARCHITECTURE.md`

### During Review

**Reviewers will check:**
- Code quality and architecture compliance
- Test coverage and correctness
- Documentation accuracy and completeness
- No security/privacy issues
- Performance implications

**Common reasons for rejection:**
- ❌ Architecture violated (e.g., DTOs leaking to UI)
- ❌ No tests or tests don't pass
- ❌ Documentation not updated when it should be
- ❌ Hardcoded strings in code
- ❌ Dependency injection not used
- ❌ Compilation errors

**Approval & Merge:**
- Requires **at least 1 approval** from a maintainer
- All CI checks must pass (lint, tests, build)
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