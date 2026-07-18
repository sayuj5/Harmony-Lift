package com.harmonylift.theory.practice

enum class PracticeMode(val title: String) {
    NOTE_RECOGNITION("Note Recognition"),
    CHORD_PRACTICE("Chord Practice"),
    SCALE_PRACTICE("Scale Practice"),
    EAR_TRAINING("Ear Training"),
    RHYTHM_PRACTICE("Rhythm Practice"),
    TUNING_PRACTICE("Tuning Practice")
}

enum class SessionPhase {
    IDLE,
    COUNTDOWN,
    WARMUP,
    EXERCISE,
    SCORING,
    SUMMARY
}

data class PracticeSessionState(
    val phase: SessionPhase = SessionPhase.IDLE,
    val mode: PracticeMode = PracticeMode.NOTE_RECOGNITION,
    val countdown: Int = 3,
    val score: Int = 0,
    val accuracy: Float = 1.0f,
    val mistakes: Int = 0,
    val totalAttempts: Int = 0,
    val targetNote: String? = null,
    val targetChord: String? = null,
    val detectedNote: String? = null,
    val detectedChord: String? = null,
    val feedbackColor: String = "Gray", // "Green", "Yellow", "Red"
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val stars: Int = 0,
    val timeRemainingSeconds: Int = 60,
    val bestNote: String? = null,
    val isComplete: Boolean = false,
    val aiFeedback: String? = null,
    val isAiAnalyzing: Boolean = false
)
