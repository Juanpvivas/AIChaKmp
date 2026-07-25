# Spike: iOS E2E Testing Tool Evaluation

**Issue:** #31
**Date:** 2026-07-25
**Status:** Complete

## Context

The project uses **Journeys** (Android CLI + Gemini) for E2E testing on Android. Journeys is an AI-agent-driven tool that executes natural-language descriptions on the app, taking screenshots and verifying results. This tool is Android-only, leaving iOS without automated E2E coverage.

## Options Evaluated

### 1. XCUITest (Apple Native)

**Description:** Apple's native UI testing framework, integrated with Xcode.

| Aspect | Assessment |
|--------|------------|
| Platform support | iOS only |
| Language | Swift / Objective-C |
| AI-driven | No (selector-based) |
| CI/CD integration | Excellent (native Xcode) |
| Learning curve | Medium |
| Maintenance | Low (stable API) |

**Pros:**
- Native Apple framework, best stability
- Deep Xcode integration
- No external dependencies
- Fast execution on simulators

**Cons:**
- iOS only (not cross-platform)
- Requires Swift/Objective-C knowledge
- Selector-based (not natural language)
- Cannot reach parity with Journeys' AI-driven approach

### 2. Maestro (Recommended)

**Description:** Modern, YAML-based mobile UI testing framework with cross-platform support.

| Aspect | Assessment |
|--------|------------|
| Platform support | iOS + Android |
| Language | YAML (declarative) |
| AI-assisted | Partial (Maestro Studio) |
| CI/CD integration | Good (GitHub Actions, etc.) |
| Learning curve | Low |
| Maintenance | Low (YAML flows) |

**Pros:**
- **Cross-platform**: same flows work on iOS and Android
- **YAML declarative syntax**: similar philosophy to Journeys
- **Easy to write**: minimal boilerplate, human-readable
- **Maestro Studio**: visual test creation tool
- **Cloud option**: Maestro Cloud for parallel execution
- **Active community**: well-maintained, good documentation
- **CI/CD friendly**: easy to integrate with GitHub Actions

**Cons:**
- External dependency (not native Apple)
- Less mature than XCUITest
- May have edge cases with complex native interactions

**Example flow:**
```yaml
appId: com.juanpvivas.aichatjp.iosApp
---
- launchApp
- tapOn: "New Conversation"
- inputText: "Hello, how are you?"
- tapOn: "Send"
- waitForAnimationToEnd
- assertVisible: ".*response.*"
```

### 3. Appium

**Description:** Cross-platform mobile testing framework using WebDriver protocol.

| Aspect | Assessment |
|--------|------------|
| Platform support | iOS + Android |
| Language | Multiple (Java, JS, Python) |
| AI-driven | No |
| CI/CD integration | Good |
| Learning curve | High |
| Maintenance | High |

**Pros:**
- Cross-platform
- Multi-language support
- Large community

**Cons:**
- Complex setup and configuration
- Slow execution
- Flaky tests common
- High maintenance overhead

### 4. Detox (React Native)

**Not applicable** - Designed for React Native apps, not Kotlin Multiplatform.

## Recommendation

**Adopt Maestro** for iOS E2E testing.

### Rationale

1. **Parity with Journeys philosophy**: Both use declarative descriptions (YAML vs XML) that describe user flows in human-readable format.

2. **Cross-platform bonus**: The same Maestro flows can run on both iOS and Android, potentially replacing Journeys in the future for unified testing.

3. **Low barrier to entry**: YAML syntax is easy to learn and maintain. No Swift/Objective-C knowledge required.

4. **CI/CD ready**: Easy to integrate with the existing GitHub Actions pipeline (macOS runner already configured).

5. **Active development**: Maestro is actively maintained with regular updates and good community support.

### Implementation Plan (Future)

If approved, implementation would involve:

1. Install Maestro CLI in CI pipeline
2. Create initial flow for critical path: send message
3. Add flows for: new conversation, switch conversation
4. Integrate with GitHub Actions on PR checks

### Decision

**Adopt Maestro** — best balance of simplicity, cross-platform capability, and alignment with the project's testing philosophy.

## References

- [Maestro Documentation](https://maestro.mobile.dev/)
- [XCUITest Documentation](https://developer.apple.com/documentation/xctest/user_interface_tests)
- [Appium Documentation](https://appium.io/docs/en/latest/)
