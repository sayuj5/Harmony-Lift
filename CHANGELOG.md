# Changelog

All notable changes to Harmony-Lift are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [2.0.0] - 2026-07-14

### 🚀 Added

- **Local AI Tutor** powered by Llama 3.2 1B Instruct (Q3_K_M GGUF) via llama.cpp JNI bridge
- **Real-time Pitch Detection** using YIN algorithm with < 50ms latency
- **Chord and Scale Detection** from live audio
- **Practice Coach Screen** with structured exercise sessions
- **DataStore Dark Mode** — persistent theme switching with Material 3
- **Privacy Policy Screen** — scrollable, offline-accessible privacy disclosure
- **Export Data** — generate PDF and TXT practice reports with native Android Share Sheet
- **WorkManager Model Download** — resumable, background download with progress notifications
- **Storage Safety Check** — pre-download validation requiring 1GB free space
- **GGUF Header Validation** — corrupted model detection before loading into native memory
- **Foreground Service Notification** for model download with Cancel action
- **FileProvider** configuration for secure file sharing
- **`proguard-rules.pro`** — JNI symbol preservation for Release builds
- **R8 Minification** and **resource shrinking** enabled for Release builds
- **`noCompress += "gguf"`** in build config to prevent APK packaging errors
- **`filesDir/models/`** as the canonical model storage location
- **Asset-to-filesDir copy pipeline** for bundled model deployment
- Splash screen redesigned with `logo_transparent.png` (96dp centered)
- Onboarding screens redesigned with corrected typography and layout
- `DashboardScreen` ActionCard with proper ripple feedback

### 🔄 Changed

- **Migrated from Qwen to Llama 3.2** — all model references, URLs, filenames, and prompt templates updated
- `TutorPromptBuilder` updated to Llama 3.2 Instruct format (`<|begin_of_text|>...<|eot_id|>`)
- `ModelDownloadManager.checkExistingModel()` now checks `filesDir` first, then assets, before going idle
- `SettingsScreen` now takes `onNavigatePrivacyPolicy` and `onExportData` callbacks
- `MainActivity` navigation graph extended with `privacy_policy` destination
- `DashboardScreen` uses `LocalIndication.current` instead of `indication = null`
- README completely rewritten with professional badges, diagrams, and tables

### 🗑️ Removed

- **Reset Tutorial card** removed from Settings screen (dead feature)
- All placeholder Qwen-related documentation and code paths
- Dependency on `build/intermediates` for any runtime behavior

### 🐛 Fixed

- `Suspend function can only be called from a coroutine` compile error in `SettingsScreens.kt`
- `Unresolved reference 'launch'` — added `import kotlinx.coroutines.launch` to all required files
- `Could not find EOCD` APK packaging failure caused by GGUF asset compression
- `displayLarge` overflow on Onboarding screens (migrated to `headlineLarge`)

### 🔒 Security

- All audio processing is strictly on-device
- No analytics, tracking, or cloud calls
- FileProvider configured with `android:exported="false"`
- Native JNI pointer never exposed outside the `tutor` module

### ⚡ Performance

- R8 minification enabled for Release builds
- Resource shrinking enabled (`isShrinkResources = true`)
- `noCompress` for GGUF prevents wasteful APK compression of binary blobs
- `AudioRecord` uses `VOICE_RECOGNITION` source with AGC and NoiseSuppressor disabled for clean instrument capture

---

## [1.0.0] - 2026-06-01

### Added
- Initial project scaffold with Clean Architecture multi-module setup
- Basic Compose UI skeleton
- Placeholder AI integration

---

[2.0.0]: https://github.com/sayuj5/Harmony-Lift/compare/v1.0.0...v2.0.0
[1.0.0]: https://github.com/sayuj5/Harmony-Lift/releases/tag/v1.0.0
