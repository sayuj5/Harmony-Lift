# Harmony-Lift PRD

## Product Vision

Harmony-Lift is a privacy-first AI music tutor that listens to live instrument audio and provides real-time note detection, chord identification, guitar tab suggestions, and beginner-friendly music theory explanations entirely on-device.

The application must function offline and never upload user audio to external servers.

---

## Target Users

* Beginner guitarists
* Music students
* Self-taught musicians
* Music educators

---

## Core User Flow

1. User opens app
2. User grants microphone permission
3. App listens in real time
4. App detects frequency and note
5. App identifies chords and intervals
6. App displays notes visually
7. App provides music theory explanations
8. App suggests practice exercises

---

## Features

### Live Pitch Detection

Input:

* Microphone audio

Output:

* Frequency
* Musical note
* Confidence score

### Chord Detection

Examples:

* C E G → C Major
* A C E → A Minor

### Theory Tutor

Examples:

* Explain chord structure
* Explain intervals
* Explain scales
* Suggest exercises

### Guitar Tab Assistance

Convert note sequences into basic tablature suggestions.

### Offline Mode

All processing occurs locally.

No cloud dependency.

---

## Non Functional Requirements

* Latency < 100 ms
* Offline-first
* Battery efficient
* Mid-range Android device support
* No user account required
* No audio storage by default
