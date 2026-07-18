# Harmony-Lift Project Context

Project Name:
Harmony-Lift

Category:
AI-Powered Real-Time Music Tutor

Goal:
Build an Android-first application that listens to instrument audio in real time and provides note detection, chord recognition, guitar tab assistance, and beginner-friendly music theory explanations.

Core Principle:
Offline First

No cloud APIs.
No backend.
No audio upload.

Target Users:

* Beginner musicians
* Guitar learners
* Music students

Platform:
Android First

Future:
Compose Multiplatform

Allowed Technologies:

Frontend:

* Kotlin
* Jetpack Compose

Audio:

* Android AudioRecord

DSP:

* YIN Pitch Detection
* FFT

Architecture:

* Clean Architecture
* MVVM

State Management:

* StateFlow
* Coroutines

Dependency Injection:

* Koin

Storage:

* Room

AI:
Optional

Phase 1:
No AI

Phase 2:
Optional Gemma/Llama 3.2 integration

Important:

The application must remain fully functional even when no LLM is installed.

The LLM is only responsible for educational explanations.

The LLM must never process raw audio.

Raw audio processing is handled entirely by the DSP pipeline.

Performance Targets:

Pitch Detection < 50 ms

UI Updates < 16 ms

Audio Latency < 100 ms

Battery Efficient

Mid-range Android Device Support
