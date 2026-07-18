package com.harmonylift.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harmonylift.theory.domain.model.Note
import com.harmonylift.theory.domain.usecase.DetectChordUseCase
import com.harmonylift.theory.practice.PracticeMode
import com.harmonylift.theory.practice.PracticeSessionState
import com.harmonylift.theory.practice.SessionPhase
import com.harmonylift.app.data.local.PracticeSessionEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PracticeSessionViewModel(
    private val detectChordUseCase: DetectChordUseCase,
    private val localModelManager: com.harmonylift.tutor.data.local.LocalModelManager? = null,
    private val downloadState: StateFlow<com.harmonylift.app.download.ModelDownloadState>? = null,
    private val practiceRepository: com.harmonylift.app.data.PracticeRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(PracticeSessionState())
    val uiState: StateFlow<PracticeSessionState> = _uiState.asStateFlow()

    private val recentNotes = mutableListOf<Note>()
    private var exerciseCount = 0
    private val maxExercises = 5
    
    private var sessionStartTime: Long = 0

    fun startSession(mode: PracticeMode) {
        _uiState.value = PracticeSessionState(mode = mode, phase = SessionPhase.COUNTDOWN)
        exerciseCount = 0
        viewModelScope.launch {
            for (i in 3 downTo 1) {
                _uiState.update { it.copy(countdown = i) }
                delay(1000)
            }
            sessionStartTime = System.currentTimeMillis()
            _uiState.update { it.copy(phase = SessionPhase.EXERCISE, countdown = 0) }
            generateNextTarget()
        }
    }

    private fun generateNextTarget() {
        if (exerciseCount >= maxExercises) {
            endSession()
            return
        }
        exerciseCount++

        val state = _uiState.value
        when (state.mode) {
            PracticeMode.NOTE_RECOGNITION -> {
                val notes = listOf("C", "D", "E", "F", "G", "A", "B")
                _uiState.update { it.copy(targetNote = notes.random(), detectedNote = null, feedbackColor = "Gray") }
            }
            PracticeMode.CHORD_PRACTICE -> {
                val chords = listOf("C Maj", "G Maj", "F Maj", "A Min", "E Min", "D Min")
                _uiState.update { it.copy(targetChord = chords.random(), detectedChord = null, feedbackColor = "Gray") }
            }
            else -> {
                // Placeholder for other modes
                _uiState.update { it.copy(targetNote = "C", detectedNote = null, feedbackColor = "Gray") }
            }
        }
    }

    fun onNoteDetected(note: Note) {
        if (_uiState.value.phase != SessionPhase.EXERCISE) return

        recentNotes.add(note)
        if (recentNotes.size > 6) recentNotes.removeAt(0)

        val state = _uiState.value

        when (state.mode) {
            PracticeMode.NOTE_RECOGNITION -> {
                val detected = note.noteClass.name
                _uiState.update { it.copy(detectedNote = detected) }
                
                if (detected == state.targetNote) {
                    val newCombo = state.combo + 1
                    val newMaxCombo = maxOf(state.maxCombo, newCombo)
                    val newScore = state.score + 10 + (newCombo * 2)
                    _uiState.update { 
                        it.copy(
                            feedbackColor = "Green", 
                            score = newScore,
                            combo = newCombo,
                            maxCombo = newMaxCombo,
                            totalAttempts = it.totalAttempts + 1,
                            accuracy = calculateAccuracy(newScore, it.totalAttempts + 1)
                        ) 
                    }
                    viewModelScope.launch {
                        delay(1000)
                        generateNextTarget()
                    }
                } else if (note.confidence > 0.8f && state.feedbackColor != "Green") {
                    _uiState.update { 
                        it.copy(
                            feedbackColor = "Red",
                            combo = 0,
                            mistakes = it.mistakes + 1,
                            totalAttempts = it.totalAttempts + 1,
                            accuracy = calculateAccuracy(it.score, it.totalAttempts + 1)
                        ) 
                    }
                }
            }
            PracticeMode.CHORD_PRACTICE -> {
                val chord = detectChordUseCase(recentNotes)
                if (chord != null) {
                    val detected = chord.name
                    _uiState.update { it.copy(detectedChord = detected) }
                    if (detected == state.targetChord) {
                        val newCombo = state.combo + 1
                        val newMaxCombo = maxOf(state.maxCombo, newCombo)
                        val newScore = state.score + 20 + (newCombo * 5)
                        _uiState.update { 
                            it.copy(
                                feedbackColor = "Green", 
                                score = newScore, 
                                combo = newCombo,
                                maxCombo = newMaxCombo,
                                totalAttempts = it.totalAttempts + 1,
                                accuracy = calculateAccuracy(newScore, it.totalAttempts + 1)
                            ) 
                        }
                        viewModelScope.launch {
                            delay(1000)
                            recentNotes.clear()
                            generateNextTarget()
                        }
                    } else if (state.feedbackColor != "Green") {
                        _uiState.update { 
                            it.copy(
                                feedbackColor = "Red",
                                combo = 0,
                                mistakes = it.mistakes + 1,
                                totalAttempts = it.totalAttempts + 1,
                                accuracy = calculateAccuracy(it.score, it.totalAttempts + 1)
                            ) 
                        }
                    }
                }
            }
            else -> {}
        }
    }

    private fun calculateAccuracy(score: Int, attempts: Int): Float {
        if (attempts == 0) return 1.0f
        return (score.toFloat() / (attempts * 10).coerceAtLeast(1)).coerceIn(0f, 1f)
    }

    private fun calculateStars(accuracy: Float, maxCombo: Int): Int {
        if (accuracy > 0.9f && maxCombo > 3) return 3
        if (accuracy > 0.7f && maxCombo > 1) return 2
        if (accuracy > 0.4f) return 1
        return 0
    }

    fun endSession() {
        val state = _uiState.value
        val finalStars = calculateStars(state.accuracy, state.maxCombo)
        
        val endTime = System.currentTimeMillis()
        val durationMs = if (sessionStartTime > 0) endTime - sessionStartTime else 0L
        val notesDetected = state.totalAttempts
        val pitchStability = if (state.totalAttempts > 0) 100f - (state.mistakes * 5f).coerceAtMost(50f) else 100f
        
        _uiState.update { 
            it.copy(
                phase = SessionPhase.SUMMARY, 
                isComplete = true, 
                isAiAnalyzing = true, 
                stars = finalStars
            ) 
        }
        
        viewModelScope.launch {
            val entity = PracticeSessionEntity(
                timestamp = endTime,
                durationMs = durationMs,
                notesDetected = notesDetected,
                pitchStability = pitchStability,
                accuracy = state.accuracy,
                score = state.score,
                mode = state.mode.name,
                instrument = "Piano" // Future enhancement: pass instrument down to VM
            )
            practiceRepository?.saveSession(entity)
            
            if (localModelManager != null && downloadState != null) {
                val stateValue = downloadState.value
                if (stateValue is com.harmonylift.app.download.ModelDownloadState.Ready) {
                    val serviceResult = localModelManager.loadModel(stateValue.file)
                    val service = serviceResult.getOrNull()
                    if (service != null) {
                        val prompt = com.harmonylift.tutor.data.prompt.TutorPromptBuilder.buildPracticeFeedbackPrompt(
                            score = state.score,
                            accuracy = state.accuracy,
                            mistakes = state.mistakes
                        )
                        try {
                            val response = service.generateResponse(prompt)
                            _uiState.update { it.copy(aiFeedback = response, isAiAnalyzing = false) }
                        } catch (e: Exception) {
                            _uiState.update { it.copy(aiFeedback = "Great job on your practice! (AI Mentor unavailable)", isAiAnalyzing = false) }
                        }
                    } else {
                        _uiState.update { it.copy(aiFeedback = "Keep up the good work! Practice makes perfect.", isAiAnalyzing = false) }
                    }
                } else {
                    _uiState.update { it.copy(aiFeedback = "Keep up the good work! Practice makes perfect.", isAiAnalyzing = false) }
                }
            } else {
                _uiState.update { it.copy(aiFeedback = "Keep up the good work! Practice makes perfect.", isAiAnalyzing = false) }
            }
        }
    }

    fun resetSession() {
        recentNotes.clear()
        detectChordUseCase.reset()
        _uiState.update { PracticeSessionState() }
    }
}
