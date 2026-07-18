# Harmony-Lift Technical Requirements

## Platform

Android First

Future:

* Compose Multiplatform Desktop

---

## Architecture

Clean Architecture

Presentation Layer
Domain Layer
Data Layer

Modules:

:app
:audio
:dsp
:pitch
:theory
:gemma
:tabs
:ui
:core

---

## Audio Pipeline

AudioRecord
↓
PCM Buffer
↓
YIN Pitch Detector
↓
Frequency Extraction
↓
Frequency To Note Mapper
↓
Chord Detector
↓
Theory Engine
↓
Gemma Explanation Layer
↓
Compose UI

---

## Technology Stack

Language:

* Kotlin 2.x

UI:

* Jetpack Compose

Concurrency:

* Coroutines
* Flow

Dependency Injection:

* Koin

Storage:

* Room

Audio:

* AudioRecord

DSP:

* YIN Algorithm
* FFT Visualization

AI:

* Gemma 3n E4B INT4 LiteRT

Model File:
gemma-3n-E4B-it-int4.litertlm

---

## AI Responsibilities

Gemma receives structured musical information.

Example Input:

{
"notes":["C4","E4","G4"],
"chord":"C Major",
"tempo":90
}

Gemma responsibilities:

* Explain chords
* Explain scales
* Explain intervals
* Generate practice suggestions

Gemma must never process raw audio.

---

## Performance Targets

Pitch Detection:
< 50 ms

AI Response:
< 2 seconds

Memory:
< 2 GB

Battery:
Optimized for mid-range Android devices
