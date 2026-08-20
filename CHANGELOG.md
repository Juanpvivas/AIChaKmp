# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Centralized Groq configuration layer (`GroqConfig`, `GroqModelResolver`, `GroqPreferences`)
- Automatic model detection from Groq API
- Intelligent model selection based on provider preference and context size
- Unit tests for `GroqModelResolver` and `GroqConfigImpl`
- iOS E2E testing with Maestro (`e2e/flows/`)
- Groq configuration guide (`docs/GROQ_CONFIGURATION.md`)

### Changed
- Updated Groq model from `llama-3.3-70b-versatile` to `qwen/qwen3.6-27b` (Issue #70)
- Refactored `ChatRemoteDataSourceImpl` to use centralized configuration
- Updated documentation with new Groq configuration architecture
- Updated testing documentation to reflect iOS E2E implementation

### Fixed
- API key configuration issues with Groq model availability

## [1.0.0] - 2026-07-25

### Added
- Initial release
- Chat with AI using Groq API
- Conversation history with Room Multiplatform
- Multiplatform support (Android + iOS)
- Clean Architecture implementation
- Koin dependency injection
- Compose Multiplatform UI

---

## How to Update This File

1. Add new entries under `[Unreleased]`
2. When releasing, move `[Unreleased]` entries to a new version section
3. Update the version number and date
4. Follow the categories: Added, Changed, Deprecated, Removed, Fixed, Security
