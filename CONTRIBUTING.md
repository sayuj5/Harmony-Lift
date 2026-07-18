# Contributing to Harmony-Lift

Thank you for your interest in contributing to Harmony-Lift! We welcome all types of contributions: bug fixes, new features, documentation improvements, and performance optimizations.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Project Setup](#project-setup)
- [Branch Naming](#branch-naming)
- [Commit Message Convention](#commit-message-convention)
- [Pull Request Checklist](#pull-request-checklist)
- [Issue Workflow](#issue-workflow)
- [Coding Standards](#coding-standards)
- [Testing Requirements](#testing-requirements)

---

## 📜 Code of Conduct

Please read and follow our [Code of Conduct](./CODE_OF_CONDUCT.md). We expect all contributors to be respectful and collaborative.

---

## 🛠 Project Setup

### Requirements

| Tool | Minimum Version |
|------|----------------|
| Android Studio | Hedgehog 2023.1+ |
| Android SDK | API 26 |
| Android NDK | r25+ |
| Java | 21 |
| Gradle | 8.x |

### Steps

1. **Fork** the repository on GitHub.

2. **Clone** your fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/Harmony-Lift.git
   cd Harmony-Lift
   ```

3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/sayuj5/Harmony-Lift.git
   ```

4. **Open** the project in Android Studio.

5. **Build** to verify everything works:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🌿 Branch Naming

| Type | Pattern | Example |
|------|---------|---------|
| Feature | `feat/short-description` | `feat/midi-input-support` |
| Bug Fix | `fix/short-description` | `fix/model-loading-crash` |
| Documentation | `docs/short-description` | `docs/update-readme` |
| Performance | `perf/short-description` | `perf/pitch-detection-latency` |
| Refactor | `refactor/short-description` | `refactor/clean-arch-domain` |
| Test | `test/short-description` | `test/dsp-unit-tests` |
| Chore | `chore/short-description` | `chore/update-dependencies` |

---

## 📝 Commit Message Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/).

```
<type>(<scope>): <short summary>

[optional body]

[optional footer]
```

**Types:**

| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `style` | Formatting, no logic change |
| `refactor` | Code restructure |
| `perf` | Performance improvement |
| `test` | Adding or updating tests |
| `chore` | Maintenance, dependency updates |
| `ci` | CI/CD pipeline changes |

**Examples:**
```
feat(tutor): add streaming token output from LlamaCppService
fix(audio): handle AGC unavailability on MediaTek devices
docs(readme): add architecture mermaid diagram
perf(dsp): optimize YIN algorithm buffer allocation
```

---

## ✅ Pull Request Checklist

Before submitting a PR, please verify:

- [ ] Code compiles and builds (`./gradlew assembleDebug`)
- [ ] Lint passes (`./gradlew lint`)
- [ ] Unit tests pass (`./gradlew test`)
- [ ] No new warnings introduced
- [ ] No placeholder implementations or TODOs left
- [ ] Clean Architecture boundaries respected (no cross-module violations)
- [ ] Existing code comments and docstrings preserved
- [ ] PR description clearly explains the change
- [ ] Screenshots attached for UI changes
- [ ] Breaking changes documented

---

## 🐛 Issue Workflow

1. **Search** existing issues before creating a new one.
2. Use the appropriate **issue template** (Bug Report, Feature Request, etc.).
3. Provide **detailed reproduction steps** for bugs.
4. Include **device info and Android version** for hardware-related issues.
5. For model/AI issues, include the model filename and size.

---

## 🎨 Coding Standards

### Architecture Rules

- **Never** break Clean Architecture module boundaries.
- The `ui` module must **not** depend on `app`, `audio`, or `tutor` directly.
- All state must flow **unidirectionally** through ViewModels.
- `MainActivity` is the **only** allowed dependency injection point.

### Kotlin Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Use `data class` for models, `sealed class` for states.
- Prefer `StateFlow` over `LiveData`.
- Use `suspend fun` and `withContext(Dispatchers.IO)` for IO operations.
- No magic numbers — use named constants.

### Compose Rules

- All screens are `@Composable` functions receiving callbacks, not direct navigation controllers.
- Use `remember { }` and `derivedStateOf { }` appropriately to minimize recompositions.
- No business logic inside Composables.
- Material 3 `MaterialTheme` tokens only — no hardcoded colors.

### AI / JNI Rules

- All native calls must go through `LlamaNative` JNI wrapper.
- Model lifecycle is managed exclusively by `LocalModelManager` (Mutex-guarded).
- Never pass a raw JNI pointer outside the `tutor` module.

---

## 🧪 Testing Requirements

- Unit tests for all DSP logic (`dsp` module).
- Unit tests for Music Theory rules (`theory` module).
- Unit tests for ViewModels using fake repositories.
- Compose UI tests for critical user flows (optional but appreciated).

Run all tests:
```bash
./gradlew test
./gradlew connectedAndroidTest
```
