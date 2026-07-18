package com.harmonylift.app.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harmonylift.theory.domain.model.Note
import com.harmonylift.theory.domain.usecase.DetectChordUseCase
import com.harmonylift.theory.domain.usecase.DetectIntervalUseCase
import com.harmonylift.theory.domain.usecase.DetectScaleUseCase
import com.harmonylift.tutor.data.local.LocalModelManager
import com.harmonylift.tutor.data.prompt.TutorPromptBuilder
import com.harmonylift.tutor.domain.model.MusicContext
import com.harmonylift.tutor.domain.repository.LLMService
import com.harmonylift.theory.presentation.TheoryScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "HarmonyLiftDebug"

class TheoryTutorViewModel(
    private val detectChordUseCase: DetectChordUseCase,
    private val detectIntervalUseCase: DetectIntervalUseCase,
    private val detectScaleUseCase: DetectScaleUseCase,
    private val localModelManager: LocalModelManager? = null,
    private val downloadState: StateFlow<com.harmonylift.app.download.ModelDownloadState>? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(TheoryScreenState())
    val uiState: StateFlow<TheoryScreenState> = _uiState.asStateFlow()

    private var llmService: LLMService? = null

    init {
        Log.d(TAG, "[TutorVM] init. localModelManager=${localModelManager != null} hasDownloadState=${downloadState != null}")
        if (localModelManager != null && downloadState != null) {
            viewModelScope.launch {
                downloadState.collect { state ->
                    Log.d(TAG, "[TutorVM] ModelDownloadState changed: $state")
                    when (state) {
                        is com.harmonylift.app.download.ModelDownloadState.Idle,
                        is com.harmonylift.app.download.ModelDownloadState.Pending -> {
                            _uiState.update { it.copy(aiError = "AI model not ready.") }
                        }
                        is com.harmonylift.app.download.ModelDownloadState.Downloading -> {
                            _uiState.update { it.copy(aiError = "Downloading model...") }
                        }
                        is com.harmonylift.app.download.ModelDownloadState.Verifying -> {
                            _uiState.update { it.copy(aiError = "Verifying model...") }
                        }
                        is com.harmonylift.app.download.ModelDownloadState.Ready -> {
                            Log.d(TAG, "[TutorVM] State.Ready — loading model from ${state.file.absolutePath}")
                            _uiState.update { it.copy(aiError = "Loading AI Tutor...") }
                            val result = localModelManager.loadModel(state.file)
                            if (result.isSuccess) {
                                llmService = result.getOrNull()
                                Log.d(TAG, "[TutorVM] Model loaded successfully. llmService=$llmService")
                                _uiState.update { it.copy(aiError = null) }
                            } else {
                                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                                Log.e(TAG, "[TutorVM] Model load failed: $errorMsg", result.exceptionOrNull())
                                _uiState.update { it.copy(aiError = "Init failed: $errorMsg") }
                            }
                        }
                        is com.harmonylift.app.download.ModelDownloadState.Failed -> {
                            Log.e(TAG, "[TutorVM] State.Failed: ${state.error}")
                            _uiState.update { it.copy(aiError = "Model download failed: ${state.error}") }
                        }
                    }
                }
            }
        } else {
            Log.e(TAG, "[TutorVM] localModelManager or downloadState is null — AI features disabled.")
            _uiState.update { it.copy(aiError = "AI Tutor not configured.") }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "[TutorVM] onCleared() — unloading model.")
        viewModelScope.launch {
            localModelManager?.unloadModel()
        }
    }

    fun onNoteDetected(note: Note) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val updatedNotes = (currentState.recentNotes + note).takeLast(6)
                val chord = detectChordUseCase(updatedNotes)
                val interval = if (updatedNotes.size >= 2) {
                    val lastTwo = updatedNotes.takeLast(2)
                    detectIntervalUseCase(lastTwo[0], lastTwo[1])
                } else null
                val scales = detectScaleUseCase(updatedNotes.map { it.noteClass })

                Log.d(TAG, "[TutorVM] StateFlow emit: note=${note.noteClass}${note.octave} chord=${chord?.name} interval=${interval?.toString()} scales=${scales.map { it.name }}")

                currentState.copy(
                    recentNotes = updatedNotes,
                    detectedChord = chord ?: currentState.detectedChord,
                    detectedInterval = interval,
                    potentialScales = scales,
                    centsDeviation = note.centsDeviation,
                    lastUpdateTimestamp = System.currentTimeMillis()
                )
            }
        }
    }

    fun askTutor(userQuestion: String? = null) {
        Log.d(TAG, "[TutorVM] askTutor() called. question=\"$userQuestion\" llmService=${llmService != null} aiError=${_uiState.value.aiError}")

        if (llmService == null) {
            val currentErr = _uiState.value.aiError
            Log.e(TAG, "[TutorVM] askTutor() aborted: llmService is null. aiError=$currentErr")
            // Show current initialization state (not a generic message)
            if (currentErr == null) {
                _uiState.update { it.copy(aiError = "AI Tutor is not yet available.") }
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAiStreaming = true, aiResponse = "", aiError = null) }

            val currentState = uiState.value
            val context = MusicContext(
                detectedChord = currentState.detectedChord?.name,
                detectedScale = currentState.potentialScales.firstOrNull()?.name,
                recentNotes = currentState.recentNotes.map { it.toString() },
                confidence = currentState.recentNotes.lastOrNull()?.confidence ?: 0f
            )
            val prompt = TutorPromptBuilder.buildTutorPrompt(context, userQuestion)
            Log.d(TAG, "[TutorVM] Built prompt (length=${prompt.length}): ${prompt.take(300)}")

            try {
                var tokenCount = 0
                llmService!!.streamResponse(prompt).collect { token ->
                    tokenCount++
                    _uiState.update { it.copy(aiResponse = it.aiResponse + token) }
                    if (tokenCount % 10 == 0) {
                        Log.d(TAG, "[TutorVM] aiResponse update: tokenCount=$tokenCount responseLength=${_uiState.value.aiResponse.length}")
                    }
                }
                Log.d(TAG, "[TutorVM] streamResponse done. totalTokens=$tokenCount")
            } catch (e: Exception) {
                Log.e(TAG, "[TutorVM] Exception during streamResponse: ${e.javaClass.simpleName}: ${e.message}", e)
                _uiState.update { it.copy(aiError = "Tutor error: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isAiStreaming = false) }
                Log.d(TAG, "[TutorVM] isAiStreaming -> false")
            }
        }
    }

    fun clearHistory() {
        Log.d(TAG, "[TutorVM] clearHistory() called.")
        _uiState.update { TheoryScreenState() }
    }

    fun resetSession() {
        Log.d(TAG, "[TutorVM] resetSession() called. Clearing state and caches.")
        detectChordUseCase.reset()
        _uiState.update { TheoryScreenState() }
    }
}
