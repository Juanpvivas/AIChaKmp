# E2E Testing with Maestro

This directory contains Maestro flows for end-to-end testing of AICha on iOS.

## Prerequisites

- macOS with Xcode installed
- iOS Simulator running (iPhone 17 Pro recommended)
- Java 17+

## Install Maestro CLI

**Option 1 - curl:**
```bash
curl -fsSL "https://get.maestro.mobile.dev" | bash
```

**Option 2 - Homebrew:**
```bash
brew tap mobile-dev-inc/tap
brew install mobile-dev-inc/tap/maestro
```

Verify installation:
```bash
maestro --help
```

## Running Flows

1. Start iOS Simulator:
```bash
open -a Simulator
```

2. Run a flow:
```bash
maestro test e2e/flows/send_message.yaml
```

3. Run all flows:
```bash
maestro test e2e/flows/
```

## Flows

| Flow | Description |
|------|-------------|
| `send_message.yaml` | Sends a message and verifies AI response |

## Writing New Flows

Maestro flows use YAML syntax. See [Maestro Docs](https://docs.maestro.dev/) for reference.

Basic structure:
```yaml
appId: com.juanpvivas.aichatjp.iosApp
---
- launchApp
- tapOn: "Button Text"
- inputText: "Hello"
- assertVisible: "Expected Text"
```
