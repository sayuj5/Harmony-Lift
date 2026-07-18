# Architecture

This document provides a comprehensive technical description of the Harmony-Lift architecture.

## 📋 Table of Contents

- [Principles](#principles)
- [Module Graph](#module-graph)
- [Clean Architecture Layers](#clean-architecture-layers)
- [Module Responsibilities](#module-responsibilities)
- [AI Pipeline](#ai-pipeline)
- [Audio Pipeline](#audio-pipeline)
- [Model Loading Flow](#model-loading-flow)
- [State Management](#state-management)
- [Navigation Graph](#navigation-graph)
- [Export Pipeline](#export-pipeline)
- [Dependency Graph (Gradle)](#dependency-graph-gradle)

---

## Principles

1. **Clean Architecture** — strict separation of Presentation, Domain, and Data layers.
2. **Offline-First** — all features function without any network connection after initial model download.
3. **Unidirectional Data Flow** — state flows down from ViewModels, events flow up from UI.
4. **No Cross-Module Violations** — `ui` never imports from `tutor`, `audio` never imports from `ui`.
5. **JNI Isolation** — all native (llama.cpp) code is contained within the `tutor` module's `data/local` package.

---

## Module Graph

```mermaid
graph TD
    app["📦 app\n(Navigation, DI, Exports)"]
    ui["🎨 ui\n(Compose Screens, Theme)"]
    tutor["🤖 tutor\n(LLM, Prompts, ViewModels)"]
    audio["🎵 audio\n(AudioRecord Engine)"]
    dsp["🔊 dsp\n(YIN Pitch Detection)"]
    theory["🎼 theory\n(Chord / Scale Rules)"]

    app --> ui
    app --> tutor
    app --> audio
    app --> dsp
    app --> theory
    tutor --> theory
    audio --> dsp
```

---

## Clean Architecture Layers

```mermaid
graph LR
    subgraph "Presentation"
        CS[Compose Screens]
        VM[ViewModels]
    end
    subgraph "Domain"
        UC[Use Cases]
        RI[Repository Interfaces]
    end
    subgraph "Data"
        LDS[Local Data Sources]
        DS[DataStore]
        JNI[JNI Bridge]
    end
    subgraph "Native"
        LC[llama.cpp]
        GGUF[GGUF Model File]
    end

    CS --> VM
    VM --> UC
    UC --> RI
    RI --> LDS
    LDS --> DS
    LDS --> JNI
    JNI --> LC
    LC --> GGUF
```

---

## Module Responsibilities

| Module | Responsibility |
|--------|---------------|
| `app` | `MainActivity`, Navigation graph, WorkManager download, DataExporter, DI wiring |
| `ui` | All `@Composable` screens, `ThemePreferences`, Material 3 theme tokens |
| `tutor` | `LocalModelManager`, `LlamaNative` JNI declarations, `LlamaCppService`, `TutorPromptBuilder`, ViewModels |
| `audio` | `AudioRecorderEngine` (AudioRecord wrapper), `AudioBuffer` ring buffer |
| `dsp` | `PitchDetector` (YIN algorithm implementation) |
| `theory` | `ChordDetector`, `ScaleDetector` — pure Kotlin music theory logic |

---

## AI Pipeline

```mermaid
sequenceDiagram
    participant User
    participant AiTutorScreen
    participant TheoryTutorViewModel
    participant TutorPromptBuilder
    participant LocalModelManager
    participant LlamaNative
    participant llama_cpp

    User->>AiTutorScreen: Types question
    AiTutorScreen->>TheoryTutorViewModel: sendMessage(text)
    TheoryTutorViewModel->>TutorPromptBuilder: build(history, question)
    TutorPromptBuilder-->>TheoryTutorViewModel: Llama 3.2 formatted prompt
    TheoryTutorViewModel->>LocalModelManager: generateResponse(prompt)
    LocalModelManager->>LlamaNative: generate(pointer, prompt, maxTokens)
    LlamaNative->>llama_cpp: native inference
    llama_cpp-->>LlamaNative: token stream
    LlamaNative-->>LocalModelManager: response string
    LocalModelManager-->>TheoryTutorViewModel: Result.success(response)
    TheoryTutorViewModel-->>AiTutorScreen: UiState updated
    AiTutorScreen-->>User: Displays AI response
```

---

## Audio Pipeline

```mermaid
graph LR
    MIC[Microphone] --> AR[AudioRecorderEngine\nVOICE_RECOGNITION source\nAGC + NoiseSuppressor disabled]
    AR --> FLOW[Flow of ShortArray frames]
    FLOW --> PITCH[PitchDetector\nYIN Algorithm]
    FLOW --> BUF[AudioBuffer Ring Buffer]
    PITCH --> CHORD[ChordDetector]
    PITCH --> SCALE[ScaleDetector]
    CHORD --> VM[PracticeSessionViewModel]
    SCALE --> VM
    VM --> UI[LiveListeningScreen]
```

---

## Model Loading Flow

```mermaid
stateDiagram-v2
    [*] --> Idle
    Idle --> CheckingFilesDir: App Startup
    CheckingFilesDir --> Ready: GGUF exists in filesDir/models/
    CheckingFilesDir --> CheckingAssets: GGUF not in filesDir
    CheckingAssets --> CopyingFromAssets: GGUF found in assets/
    CheckingAssets --> Idle: Not in assets either
    CopyingFromAssets --> Ready: Copy complete + verified
    Idle --> Downloading: User taps Download
    Downloading --> Verifying: Download complete
    Verifying --> Ready: GGUF magic bytes valid
    Verifying --> Failed: Checksum/header mismatch
    Failed --> Idle: User taps Retry
    Ready --> ModelLoaded: loadModel() called
    ModelLoaded --> [*]: App in use
```

---

## State Management

All UI state is managed via `StateFlow` in ViewModels:

```kotlin
// Pattern used throughout the codebase
class TheoryTutorViewModel(...) : ViewModel() {
    private val _uiState = MutableStateFlow(TheoryTutorUiState())
    val uiState: StateFlow<TheoryTutorUiState> = _uiState.asStateFlow()
}
```

Theme persistence uses DataStore:

```kotlin
class ThemePreferences(context: Context) {
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.valueOf(prefs[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name)
    }
    suspend fun saveThemeMode(mode: ThemeMode) { ... }
}
```

---

## Navigation Graph

```mermaid
graph LR
    S[splash] --> W[welcome]
    W --> OB[onboarding]
    OB --> D[dashboard]
    D --> LL[live_listening]
    D --> PC[practice_coach]
    D --> AT[ai_tutor]
    D --> SET[settings]
    SET --> PP[privacy_policy]
```

All navigation is handled in `MainActivity.kt` via `NavHost` and `NavController`.

---

## Export Pipeline

```mermaid
graph LR
    SET[SettingsScreen] --> DLG[Export Dialog\nPDF or TXT]
    DLG --> DE[DataExporter\napp module]
    DE --> CACHE[context.cacheDir/exports/]
    CACHE --> FP[FileProvider\nSecure URI generation]
    FP --> SHARE[Android Share Sheet\nIntent.ACTION_SEND]
```

---

## Dependency Graph (Gradle)

```
app
├── :ui
├── :tutor
│   └── :theory
├── :audio
│   └── :dsp
├── :dsp
└── :theory
```

Key external dependencies:

| Library | Purpose |
|---------|---------|
| `androidx.navigation:navigation-compose` | In-app navigation |
| `androidx.work:work-runtime-ktx` | Background model download |
| `androidx.datastore:datastore-preferences` | Persistent settings |
| `androidx.compose:compose-bom` | Compose UI framework |
| `llama.cpp` (native .so) | On-device AI inference |
