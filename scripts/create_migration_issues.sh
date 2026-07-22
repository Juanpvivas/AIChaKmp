#!/usr/bin/env bash
#
# create_migration_issues.sh
#
# Creates the full backlog of GitHub issues for the AICha -> Kotlin Multiplatform
# (Android + iOS, Clean Architecture) migration, in Juanpvivas/AIChaKmp.
#
# Prerequisites:
#   - GitHub CLI (`gh`) installed and authenticated: `gh auth login`
#   - Write access to Juanpvivas/AIChaKmp
#
# Usage:
#   chmod +x create_migration_issues.sh
#   ./create_migration_issues.sh
#
# The script is idempotent-ish for labels (safe to re-run), but re-running it
# will create duplicate issues since `gh issue create` has no built-in
# dedupe. Run it once.

set -euo pipefail

REPO="Juanpvivas/AIChaKmp"

echo "==> Target repo: $REPO"
echo "==> Verifying gh auth..."
gh auth status || { echo "Run 'gh auth login' first."; exit 1; }

# ---------------------------------------------------------------------------
# 1. Labels
# ---------------------------------------------------------------------------
echo "==> Creating labels (safe to ignore 'already exists' errors)..."

create_label () {
  local name="$1" color="$2" desc="$3"
  gh label create "$name" --repo "$REPO" --color "$color" --description "$desc" 2>/dev/null \
    || echo "   (label '$name' already exists, skipping)"
}

create_label "migration-kmp"      "5319E7" "Part of the Android -> Kotlin Multiplatform migration"
create_label "epic"               "8E44AD" "Tracking issue that groups related sub-issues"
create_label "area:scaffolding"   "0E8A16" "Gradle/module/project scaffolding"
create_label "area:domain"        "1D76DB" "domain/ layer (entities, use cases, repository contracts)"
create_label "area:data"          "1D76DB" "data/ layer (local, remote, repository implementations)"
create_label "area:di"            "1D76DB" "Dependency injection (Koin)"
create_label "area:presentation"  "1D76DB" "ui/ layer (Compose Multiplatform)"
create_label "area:testing"       "FBCA04" "Test suite (commonTest, androidUnitTest, iosTest, Journeys)"
create_label "area:ci-cd"         "FBCA04" "CI/CD pipeline"
create_label "area:security"      "D93F0B" "Secrets/API key handling"
create_label "area:qa"            "FBCA04" "Manual/automated QA parity"
create_label "platform:android"   "3DDC84" "Android-specific"
create_label "platform:ios"       "999999" "iOS-specific"
create_label "platform:common"    "C5DEF5" "commonMain, applies to both platforms"

# ---------------------------------------------------------------------------
# Helper: create a sub-issue and echo back its issue number
# ---------------------------------------------------------------------------
create_issue () {
  local title="$1" body="$2" labels="$3"
  local url
  url=$(gh issue create --repo "$REPO" --title "$title" --body "$body" --label "$labels")
  echo "$url" >&2
  basename "$url"
}

# Helper: create an epic issue once we know its sub-issues' numbers
create_epic () {
  local title="$1" intro="$2" labels="$3"
  shift 3
  local checklist=""
  for num in "$@"; do
    checklist="${checklist}- [ ] #${num}
"
  done
  local body
  body="$(cat <<EOF
## Context

$intro

## Sub-issues

$checklist

Closing criteria: this epic is done when every sub-issue above is closed and the relevant section of \`docs/ARCHITECTURE.md\` no longer lists an open point for this phase.
EOF
)"
  gh issue create --repo "$REPO" --title "$title" --body "$body" --label "$labels" >&2
}

echo "==> Creating Phase 0 sub-issues (Scaffolding)..."

P0_1=$(create_issue \
  "Set up composeApp as a Kotlin Multiplatform module" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §3.1.

The project is currently a single Android Gradle module (`app/`). This is the first step of the KMP migration: introduce a multiplatform module (`composeApp`) with the target source sets, alongside the existing `app/` module so the migration can proceed incrementally.

## Work
- [ ] Add the Kotlin Multiplatform Gradle plugin to the root `build.gradle.kts`.
- [ ] Create `composeApp/build.gradle.kts` declaring `androidTarget()` and iOS targets (`iosArm64()`, `iosSimulatorArm64()`, `iosX64()`).
- [ ] Create empty source sets: `commonMain`, `androidMain`, `iosMain`, `commonTest`, `androidUnitTest`.
- [ ] Register `composeApp` in `settings.gradle.kts` (`include(":composeApp")`).
- [ ] Confirm `./gradlew :composeApp:compileDebugKotlinAndroid` and `./gradlew :composeApp:compileKotlinIosSimulatorArm64` both succeed on an empty module.

## Acceptance criteria
- [ ] `composeApp` module builds for both Android and iOS simulator targets with no source code yet.
- [ ] `docs/ARCHITECTURE.md` §3.1 module tree matches the actual folder structure created.
EOF
)" \
  "migration-kmp,area:scaffolding,platform:common")

P0_2=$(create_issue \
  "Create the iosApp Xcode project shell" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §3.1.

Create the native iOS entry point project. Per the architecture, `iosApp/` must stay a thin wrapper with no business logic — it only starts Koin and mounts the Compose Multiplatform UI exposed by `composeApp`.

## Work
- [ ] Create `iosApp/iosApp.xcodeproj` (or `.xcworkspace` if using CocoaPods/SPM integration).
- [ ] Add `iosApp/iosApp/App.swift` as the SwiftUI/UIKit entry point.
- [ ] Link the `composeApp` Kotlin/Native framework to the Xcode project.
- [ ] Verify the app launches on an iOS Simulator showing a placeholder screen (real UI comes in Phase 4).

## Acceptance criteria
- [ ] `iosApp` builds and runs on iOS Simulator from Xcode.
- [ ] No Kotlin business logic duplicated in Swift — `App.swift` only bootstraps.
EOF
)" \
  "migration-kmp,area:scaffolding,platform:ios")

P0_3=$(create_issue \
  "Add Compose Multiplatform + Material 3 to composeApp" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §2, §3.1.

With the module scaffolding in place, wire up the Compose Multiplatform Gradle plugin and Material 3 dependency so `commonMain` can host shared UI code (used starting in Phase 4).

## Work
- [ ] Add the `org.jetbrains.compose` plugin to `composeApp/build.gradle.kts`.
- [ ] Add `compose.runtime`, `compose.foundation`, `compose.material3` dependencies to `commonMain`.
- [ ] Add a trivial `@Composable fun App()` in `commonMain` and render it from both `MainActivity` (Android, temporary) and `App.swift` (iOS, via `ComposeUIViewController`).

## Acceptance criteria
- [ ] The same Composable renders on an Android emulator and an iOS Simulator.
EOF
)" \
  "migration-kmp,area:scaffolding,platform:common")

echo "==> Creating Phase 0 epic..."
create_epic \
  "[Epic] Phase 0 — KMP project scaffolding" \
  "First phase of the migration described in \`docs/ARCHITECTURE.md\`: introduce the \`composeApp\` multiplatform module and the \`iosApp\` native shell, without moving any feature code yet." \
  "migration-kmp,epic,area:scaffolding" \
  "$P0_1" "$P0_2" "$P0_3"

echo "==> Creating Phase 1 sub-issues (Domain layer)..."

P1_1=$(create_issue \
  "Create domain/model with pure Kotlin entities" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §6.

Extract the domain models (`ChatMessage`, `Conversation`) out of the current Android-only `data/model/` into `domain/model/` in `commonMain`, as pure Kotlin with zero platform dependencies. Replace `java.time`/`java.util.Date` usage with `kotlinx-datetime`.

## Work
- [ ] Add the `kotlinx-datetime` dependency to `commonMain`.
- [ ] Move/rewrite `ChatMessage`, `Conversation` into `composeApp/src/commonMain/.../domain/model/`, no suffix naming.
- [ ] Define the sealed `AppError` type (`Network`, `Http`, `NoConnectivity`, `Unknown`) in `domain/model/`.
- [ ] Remove the old `data/model/ChatModels.kt` once nothing references it.

## Acceptance criteria
- [ ] `domain/model/` compiles in `commonMain` with no Android/iOS imports.
- [ ] `commonTest` has basic tests for any model logic (e.g. equality, mapping helpers) if applicable.
EOF
)" \
  "migration-kmp,area:domain,platform:common")

P1_2=$(create_issue \
  "Define domain/repository interfaces" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §3.2, §6.

Define the repository contracts that `data/repository/` will implement and that use cases will depend on. This decouples `ui/`/`domain/usecase/` from any concrete Room/Ktor implementation detail.

## Work
- [ ] Create `domain/repository/ChatRepository.kt` (interface): send message, observe conversation messages, etc.
- [ ] Create `domain/repository/ConversationRepository.kt` (interface): list conversations, create conversation, select conversation.
- [ ] Document each method's contract (suspend vs Flow, error handling via `AppError`/`Result`).

## Acceptance criteria
- [ ] Interfaces compile in `commonMain` with no reference to Room entities, DTOs, or the openai-kotlin SDK.
EOF
)" \
  "migration-kmp,area:domain,platform:common")

P1_3=$(create_issue \
  "Create domain/usecase classes" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §3.2, §6.

Introduce the use case layer, the mandatory entry point from `ui/` into `domain`/`data`. ViewModels must call use cases, never repositories directly.

## Work
- [ ] `SendMessageUseCase` — sends a message and appends the AI response to the active conversation.
- [ ] `ObserveConversationHistoryUseCase` — exposes the message history of a conversation as a `Flow`.
- [ ] `CreateConversationUseCase` — starts a new conversation.
- [ ] `ObserveConversationsUseCase` — exposes the list of past conversations (for the history drawer).
- [ ] Each use case takes its repository dependency via constructor and exposes `operator fun invoke(...)`.

## Acceptance criteria
- [ ] Each use case has a `commonTest` covering its main behavior using a fake repository.
EOF
)" \
  "migration-kmp,area:domain,platform:common")

echo "==> Creating Phase 1 epic..."
create_epic \
  "[Epic] Phase 1 — Domain layer (Clean Architecture)" \
  "Introduce the \`domain/\` layer (models, repository interfaces, use cases) as pure Kotlin in \`commonMain\`, per \`docs/ARCHITECTURE.md\` §6." \
  "migration-kmp,epic,area:domain" \
  "$P1_1" "$P1_2" "$P1_3"

echo "==> Creating Phase 2 sub-issues (Data layer)..."

P2_1=$(create_issue \
  "Migrate data/remote (Groq client) to commonMain" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §4, §5.1, §11; `docs/SPEC.md` §2.

The `openai-kotlin` SDK is built on Ktor and is multiplatform-compatible as-is. Move `ChatRemoteDataSource`/`ChatRemoteDataSourceImpl`, DTOs, and mappers into `commonMain`. Only the `HttpClientEngine` needs to be platform-specific.

## Work
- [ ] Move `data/remote/` (interface, impl, `dto/`, `mapper/`) to `composeApp/src/commonMain/.../data/remote/`.
- [ ] Declare `expect fun httpClientEngine(): HttpClientEngine` in `commonMain`.
- [ ] Implement `actual` with the `OkHttp` engine in `androidMain`.
- [ ] Implement `actual` with the `Darwin` engine in `iosMain`.
- [ ] Mapper functions (`toDomain()`) keep returning `domain/model/` types only.

## Acceptance criteria
- [ ] `ChatRemoteDataSourceImpl` compiles and is testable in `commonTest` (mapper tests, no live network calls).
- [ ] Android and iOS builds both resolve a working `HttpClient`.
EOF
)" \
  "migration-kmp,area:data,platform:common")

P2_2=$(create_issue \
  "Migrate data/local to Room Multiplatform" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §4, §5.2, §10; `docs/SPEC.md` §4.

Move Room entities/DAOs to `commonMain` using Room Multiplatform (2.7+) with KSP. The database instance itself needs a platform-specific builder.

## Work
- [ ] Bump Room to a Multiplatform-compatible version; confirm KSP (not KAPT) is used.
- [ ] Move `ChatMessageEntity`, `ConversationEntity`, `ChatMessageDao`, `ConversationDao`, `AiChaDatabase` to `commonMain`.
- [ ] Declare `expect fun createRoomDatabase(): AiChaDatabase` (or a `DatabaseBuilderFactory`).
- [ ] Implement the Android `actual` using `Context` (injected via Koin, see Phase 3).
- [ ] Implement the iOS `actual` resolving the app's documents directory path.
- [ ] Port existing migrations (`MIGRATION_x_y`) unchanged into `commonMain`.

## Acceptance criteria
- [ ] Database reads/writes work identically on an Android emulator and an iOS Simulator.
- [ ] Migration tests (if any existed) still pass in `commonTest`.
EOF
)" \
  "migration-kmp,area:data,platform:common")

P2_3=$(create_issue \
  "Implement data/repository/*Impl against domain interfaces" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §5.3, §11; `docs/SPEC.md` §1.2.

Rewrite `ChatRepositoryImpl` (and add `ConversationRepositoryImpl`) to implement the `domain/repository/` interfaces defined in Phase 1, using the migrated `local/` and `remote/` data sources from this phase.

## Work
- [ ] `ChatRepositoryImpl`: builds the full conversation history as context before every Groq call (existing behavior, preserved).
- [ ] `ConversationRepositoryImpl`: list/create/select conversations backed by Room.
- [ ] Normalize network/API errors into `AppError` before returning from the repository.
- [ ] Bind interfaces to implementations via Koin (coordinate with Phase 3, #P3).

## Acceptance criteria
- [ ] `commonTest` covers mapping, error handling, and local/remote coordination using fakes for `RemoteDataSource`/DAO.
EOF
)" \
  "migration-kmp,area:data,platform:common")

P2_4=$(create_issue \
  "Implement expect/actual groqApiKey()" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §4, §11, §15.

The Groq API key must never be hardcoded. Resolve it per platform behind a common `expect` declaration.

## Work
- [ ] Declare `expect fun groqApiKey(): String` in `commonMain`.
- [ ] Android `actual`: read from `BuildConfig.GROQ_API_KEY` (sourced from `local.properties`, existing mechanism).
- [ ] iOS `actual`: read from `Info.plist` (sourced from `Config.xcconfig`, ignored by git — coordinate with #P7).
- [ ] Wire the resolved key into the Ktor/openai-kotlin client configuration in `data/remote/`.

## Acceptance criteria
- [ ] Neither platform has the key hardcoded anywhere in versioned files.
- [ ] Missing key on either platform fails clearly (not silently) at startup or first request.
EOF
)" \
  "migration-kmp,area:data,area:security,platform:common")

echo "==> Creating Phase 2 epic..."
create_epic \
  "[Epic] Phase 2 — Data layer migration to KMP" \
  "Move \`data/local/\` and \`data/remote/\` to \`commonMain\` (Room Multiplatform + Ktor), and implement the \`domain/repository/\` contracts. Per \`docs/ARCHITECTURE.md\` §5, §10, §11." \
  "migration-kmp,epic,area:data" \
  "$P2_1" "$P2_2" "$P2_3" "$P2_4"

echo "==> Creating Phase 3 sub-issue (DI)..."

P3_1=$(create_issue \
  "Replace Hilt with Koin" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §9.

Hilt is Android-only and cannot be used in `commonMain`/iOS. Replace it with Koin across the whole dependency graph.

## Work
- [ ] Remove Hilt plugin/annotations/modules (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@Module`, `@Binds`, etc.).
- [ ] Create `commonModule` in `commonMain` (Ktor client, Room database, repository bindings, use case factories).
- [ ] Create `androidPlatformModule` (androidMain) resolving Android-only `expect`/`actual` (e.g. `Context`-dependent Room builder).
- [ ] Create `iosPlatformModule` (iosMain) resolving iOS-only `expect`/`actual`.
- [ ] Start Koin from the Android `Application` class (`androidContext(...)`, `modules(commonModule, androidPlatformModule)`).
- [ ] Expose and call `fun initKoin()` from `iosMain`/`App.swift` on iOS startup.
- [ ] Inject ViewModels via Koin (coordinate with Phase 4).

## Acceptance criteria
- [ ] No Hilt dependency remains in `libs.versions.toml`/`build.gradle.kts`.
- [ ] App boots and resolves all dependencies on both Android and iOS.
EOF
)" \
  "migration-kmp,area:di,platform:common")

echo "==> Creating Phase 3 epic..."
create_epic \
  "[Epic] Phase 3 — Dependency Injection: Hilt -> Koin" \
  "Replace Hilt (Android-only) with Koin (multiplatform), per \`docs/ARCHITECTURE.md\` §9." \
  "migration-kmp,epic,area:di" \
  "$P3_1"

echo "==> Creating Phase 4 sub-issues (Presentation)..."

P4_1=$(create_issue \
  "Migrate the Chat feature UI to Compose Multiplatform" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §7; `docs/SPEC.md` §1.1, §1.2, §3.

Move `ui/chat/` (Route, Screen, ViewModel, UiState, components) into `commonMain`, using the multiplatform `ViewModel` (`org.jetbrains.androidx.lifecycle`) and invoking the use cases from Phase 1 instead of a repository directly.

## Work
- [ ] Move `ChatRoute.kt`, `ChatScreen.kt`, `ChatUiState.kt`, `components/` to `commonMain`.
- [ ] Rewrite `ChatViewModel` to extend the multiplatform `ViewModel` and call `SendMessageUseCase`/`ObserveConversationHistoryUseCase`.
- [ ] Verify `ChatScreen` remains fully stateless and `@Preview`-compatible.

## Acceptance criteria
- [ ] The chat screen renders and functions identically on an Android emulator and an iOS Simulator.
- [ ] `ChatViewModelTest` moved to `commonTest`, passing with fakes.
EOF
)" \
  "migration-kmp,area:presentation,platform:common")

P4_2=$(create_issue \
  "Migrate the History feature UI to Compose Multiplatform" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §7; `docs/SPEC.md` §1.3, §3.

Move `ui/history/` (drawer, Route, Screen, ViewModel, UiState, components) into `commonMain`, following the same pattern as the Chat feature.

## Work
- [ ] Move `HistoryRoute.kt`, `HistoryScreen.kt`, `HistoryUiState.kt`, `components/` to `commonMain`.
- [ ] Rewrite `HistoryViewModel` to call `ObserveConversationsUseCase`/`CreateConversationUseCase`.
- [ ] If the drawer open/close gesture needs a platform nuance, isolate it behind a small `expect @Composable` per `docs/SPEC.md` §1.3, not a full screen duplication.

## Acceptance criteria
- [ ] History drawer lists, selects, and creates conversations identically on both platforms.
EOF
)" \
  "migration-kmp,area:presentation,platform:common")

P4_3=$(create_issue \
  "Migrate string resources to Compose Multiplatform resources" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §7.

Android's `strings.xml` is not usable from `commonMain`. Migrate all user-facing text to Compose Multiplatform's `composeResources`.

## Work
- [ ] Set up `composeApp/src/commonMain/composeResources/values/strings.xml` (or the Compose Multiplatform resource format in use at implementation time).
- [ ] Port every string from the existing Android `strings.xml`/`values-en/strings.xml`.
- [ ] Replace every `stringResource(R.string.x)` call with `stringResource(Res.string.x)`.
- [ ] Grep the codebase for any remaining hardcoded UI strings.

## Acceptance criteria
- [ ] Zero hardcoded strings in Composables (enforced by code review per `CONTRIBUTING.md`).
EOF
)" \
  "migration-kmp,area:presentation,platform:common")

P4_4=$(create_issue \
  "Migrate navigation to Compose Multiplatform Navigation" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §2, §7.

Replace `androidx.navigation.compose` (Android-only) with `org.jetbrains.androidx.navigation` so the nav graph lives in `commonMain`.

## Work
- [ ] Add the Compose Multiplatform Navigation dependency to `commonMain`.
- [ ] Port `AppNavGraph.kt` to the multiplatform API.
- [ ] Verify deep navigation flows (chat <-> history) work on both platforms.

## Acceptance criteria
- [ ] Navigation between Chat and History works identically on Android and iOS.
EOF
)" \
  "migration-kmp,area:presentation,platform:common")

P4_5=$(create_issue \
  "Migrate the Material 3 theme to Compose Multiplatform" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §3.2, §7.

Move `ui/theme/` (`Color.kt`, `Theme.kt`, `Type.kt`) to `commonMain`, using the Compose Multiplatform Material 3 artifact.

## Work
- [ ] Move theme files to `commonMain`.
- [ ] Confirm typography/fonts resolve correctly on iOS (font loading can differ from Android).

## Acceptance criteria
- [ ] Visual parity (colors, typography) between Android and iOS builds.
EOF
)" \
  "migration-kmp,area:presentation,platform:common")

echo "==> Creating Phase 4 epic..."
create_epic \
  "[Epic] Phase 4 — Presentation layer to Compose Multiplatform" \
  "Move \`ui/chat\`, \`ui/history\`, \`ui/theme\`, navigation, and string resources into \`commonMain\`, per \`docs/ARCHITECTURE.md\` §7." \
  "migration-kmp,epic,area:presentation" \
  "$P4_1" "$P4_2" "$P4_3" "$P4_4" "$P4_5"

echo "==> Creating Phase 5 sub-issues (Testing)..."

P5_1=$(create_issue \
  "Migrate unit tests to commonTest with fakes" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §12, §12.1; `CONTRIBUTING.md` (Testing Requirements).

MockK is JVM-only and unavailable in `iosTest`. Migrate existing tests (`ChatRepositoryTest`, `ChatViewModelTest`, `FakeChatRepository`) to `commonTest`, replacing MockK usages with hand-written fakes where needed.

## Work
- [ ] Move `FakeChatRepository` and other shared fakes to a common `testutil`/`fake` package in `commonTest`.
- [ ] Port `ChatRepositoryImplTest` and `ChatViewModelTest` to `commonTest`, removing MockK-specific code.
- [ ] Add `SendMessageUseCaseTest`, `ObserveConversationHistoryUseCaseTest`, etc. for the new use cases.
- [ ] Keep `androidUnitTest` only for cases that genuinely cannot be faked (rare).

## Acceptance criteria
- [ ] `./gradlew :composeApp:allTests` passes, running the same test suite conceptually on both JVM (Android) and Kotlin/Native (iOS) targets.
EOF
)" \
  "migration-kmp,area:testing,platform:common")

P5_2=$(create_issue \
  "Set up the iosTest target" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §12.

Confirm `commonTest` actually executes against an iOS simulator target, not just JVM, and wire it into local developer workflow.

## Work
- [ ] Configure `iosSimulatorArm64Test`/`iosX64Test` source sets if any iOS-specific test code is needed.
- [ ] Document the command to run iOS tests locally (`./gradlew :composeApp:iosSimulatorArm64Test` or equivalent) in `README.md`/`CONTRIBUTING.md` if it differs from what's already written.

## Acceptance criteria
- [ ] `commonTest` suite runs green on an iOS simulator target, not just Android.
EOF
)" \
  "migration-kmp,area:testing,platform:ios")

P5_3=$(create_issue \
  "Relocate Android instrumented tests and Journeys under composeApp" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §12.2; `docs/SPEC.md` §1.1.

Move the existing Compose UI tests and Journey `.xml` files from `app/src/androidTest/` to `composeApp/src/androidInstrumentedTest/`, matching the paths already documented in `docs/ARCHITECTURE.md`/`docs/SPEC.md`.

## Work
- [ ] Move `send_message.xml`, `open_history_drawer.xml`, `select_conversation.xml`, `start_new_conversation.xml` to `composeApp/src/androidInstrumentedTest/journey/`.
- [ ] Move `ExampleInstrumentedTest.kt` (or its replacement) to `composeApp/src/androidInstrumentedTest/kotlin/`.
- [ ] Re-run each Journey against the migrated Chat/History screens to confirm they still pass end-to-end.

## Acceptance criteria
- [ ] All 4 existing Journeys pass against the Compose Multiplatform version of the app running on Android.
EOF
)" \
  "migration-kmp,area:testing,platform:android")

echo "==> Creating Phase 5 epic..."
create_epic \
  "[Epic] Phase 5 — Testing migration" \
  "Move the test suite to \`commonTest\` with fakes, stand up \`iosTest\`, and relocate Android instrumented tests/Journeys, per \`docs/ARCHITECTURE.md\` §12." \
  "migration-kmp,epic,area:testing" \
  "$P5_1" "$P5_2" "$P5_3"

echo "==> Creating Phase 6 sub-issues (CI/CD & quality)..."

P6_1=$(create_issue \
  "Set up CI pipeline with a macOS runner for iOS" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §14.

The current pipeline (if any) only builds/tests Android. Add the iOS leg, which requires a macOS runner.

## Work
- [ ] Lint job: ktlint + detekt (any runner).
- [ ] Android job: `./gradlew :composeApp:assembleDebug` + `./gradlew :composeApp:allTests`.
- [ ] iOS job (macOS runner): build the `composeApp` framework for simulator (`linkDebugFrameworkIosSimulatorArm64`) and `xcodebuild` the `iosApp` project.
- [ ] Wire secrets (`GROQ_API_KEY`) into both jobs without committing them (see #P7).

## Acceptance criteria
- [ ] CI fails the build if either the Android or the iOS leg breaks.
EOF
)" \
  "migration-kmp,area:ci-cd,platform:common")

P6_2=$(create_issue \
  "Wire ktlint/detekt across all KMP source sets" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §13.

Confirm ktlint/detekt run over `commonMain`, `androidMain`, and `iosMain`, not just the old `app/` module's Android sources.

## Work
- [ ] Update ktlint/detekt Gradle configuration to include `composeApp`'s new source sets.
- [ ] Fix any newly-surfaced violations from moved code.

## Acceptance criteria
- [ ] `./gradlew ktlintFormat`/`./gradlew detekt` cover the full `composeApp` module.
EOF
)" \
  "migration-kmp,area:ci-cd,platform:common")

echo "==> Creating Phase 6 epic..."
create_epic \
  "[Epic] Phase 6 — CI/CD and code quality" \
  "Extend CI to build/test iOS (macOS runner) alongside Android, and make sure linting covers every KMP source set, per \`docs/ARCHITECTURE.md\` §13-§14." \
  "migration-kmp,epic,area:ci-cd" \
  "$P6_1" "$P6_2"

echo "==> Creating Phase 7 sub-issue (Security)..."

P7_1=$(create_issue \
  "Set up Config.xcconfig + gitignore for the iOS API key" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §15; `README.md` (iOS setup section).

Mirror the existing `local.properties` mechanism (Android) on iOS: a local, gitignored config file feeding the API key into the app via `Info.plist`.

## Work
- [ ] Add `iosApp/Config.xcconfig` to `.gitignore`.
- [ ] Add an `Info.plist` entry referencing `$(GROQ_API_KEY)` from the xcconfig.
- [ ] Confirm the `groqApiKey()` `actual` (from #P2.4) reads this value correctly at runtime.
- [ ] Update onboarding docs if the exact file/steps differ from what's currently written in `README.md`.

## Acceptance criteria
- [ ] A fresh clone with no `Config.xcconfig` fails clearly instead of silently shipping an empty key.
- [ ] `Config.xcconfig` never appears in `git status`/`git diff` after being created.
EOF
)" \
  "migration-kmp,area:security,platform:ios")

echo "==> Creating Phase 7 epic..."
create_epic \
  "[Epic] Phase 7 — iOS secrets handling" \
  "Mirror Android's local.properties API key mechanism on iOS via Config.xcconfig, per \`docs/ARCHITECTURE.md\` §15." \
  "migration-kmp,epic,area:security" \
  "$P7_1"

echo "==> Creating Phase 8 sub-issue (QA parity)..."

P8_1=$(create_issue \
  "Spike: evaluate an iOS E2E tool equivalent to Journeys" \
  "$(cat <<'EOF'
## Context
Reference: `docs/ARCHITECTURE.md` §16 (open points); `docs/SPEC.md` §1.1.

Journeys (Android CLI + Gemini) has no iOS equivalent today, so critical flows (send message, switch conversation) are validated manually on iOS. This is a time-boxed investigation, not an implementation task.

## Work
- [ ] Evaluate XCUITest for natural-language-free but scriptable E2E coverage.
- [ ] Evaluate any AI-agent-driven iOS testing tool that might reach parity with Journeys.
- [ ] Write up a recommendation (adopt / wait / accept manual QA for now) and update `docs/ARCHITECTURE.md` §16 accordingly.

## Acceptance criteria
- [ ] A short decision doc/comment is added to this issue and reflected in `docs/ARCHITECTURE.md`.
EOF
)" \
  "migration-kmp,area:qa,platform:ios")

echo "==> Creating Phase 8 epic..."
create_epic \
  "[Epic] Phase 8 — iOS QA/E2E parity" \
  "Investigate and decide on an iOS equivalent to Android's Journeys tool, per \`docs/ARCHITECTURE.md\` §16." \
  "migration-kmp,epic,area:qa" \
  "$P8_1"

echo "==> Done. Created 9 epics + 23 sub-issues (32 total) in $REPO."
