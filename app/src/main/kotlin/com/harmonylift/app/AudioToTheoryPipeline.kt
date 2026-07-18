package com.harmonylift.app

import android.util.Log
import com.harmonylift.audio.AudioRecorderEngine
import com.harmonylift.dsp.note.NoteMapper
import com.harmonylift.dsp.pitch.PitchResult
import com.harmonylift.dsp.pitch.YinPitchDetector
import com.harmonylift.theory.domain.model.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "HarmonyLiftDebug"

/**
 * Connects the audio capture pipeline to the music theory engine.
 *
 * Accumulates raw PCM samples from [AudioRecorderEngine] into a ring buffer
 * and dispatches YIN pitch detection once every [HOP_SIZE] new samples,
 * always operating on a full [YinPitchDetector.WINDOW_SIZE] window.
 *
 * 50% overlap (HOP_SIZE = WINDOW_SIZE / 2) balances latency vs. accuracy.
 */
class AudioToTheoryPipeline(
    private val audioRecorderEngine: AudioRecorderEngine,
    private val yinPitchDetector: YinPitchDetector = YinPitchDetector(),
    private val noteMapper: NoteMapper = NoteMapper()
) {
    companion object {
        private val WINDOW = YinPitchDetector.WINDOW_SIZE          // 4096
        private val HOP    = YinPitchDetector.WINDOW_SIZE / 2      // 2048 — 50% overlap
    }

    private var pipelineJob: Job? = null

    // Ring buffer for sample accumulation
    private val ringBuffer = ShortArray(WINDOW)
    private var ringWritePos = 0
    private var samplesAccumulated = 0

    // --- Exposed StateFlows (used by UI and debug overlay) ---
    private val _rawWaveform = MutableStateFlow(ShortArray(0))
    val rawWaveform: StateFlow<ShortArray> = _rawWaveform.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _pitchHz = MutableStateFlow(0f)
    val pitchHz: StateFlow<Float> = _pitchHz.asStateFlow()

    private val _confidence = MutableStateFlow(0f)
    val confidence: StateFlow<Float> = _confidence.asStateFlow()

    private val _detectedNote = MutableStateFlow<Note?>(null)
    val detectedNote: StateFlow<Note?> = _detectedNote.asStateFlow()

    var onNoteDetected: ((Note) -> Unit)? = null

    fun start(scope: CoroutineScope) {
        Log.d(TAG, "[Pipeline] start() called. WINDOW=$WINDOW HOP=$HOP")
        pipelineJob?.cancel()
        // Reset ring buffer state on each start
        ringBuffer.fill(0)
        ringWritePos = 0
        samplesAccumulated = 0
        
        // Fix: Reset all StateFlow values so previous session data does not carry over
        _rawWaveform.value = ShortArray(0)
        _rmsLevel.value = 0f
        _pitchHz.value = 0f
        _confidence.value = 0f
        _detectedNote.value = null
        
        // Reset the note mapper cache
        noteMapper.reset()

        pipelineJob = scope.launch(Dispatchers.Default) {
            Log.d(TAG, "[Pipeline] Collecting from AudioRecorderEngine...")
            try {
                audioRecorderEngine.startRecording().collect { buffer ->
                    _rawWaveform.value = buffer
                    processBuffer(buffer)
                }
            } catch (e: Exception) {
                Log.e(TAG, "[Pipeline] Exception: ${e.javaClass.simpleName}: ${e.message}", e)
            }
            Log.d(TAG, "[Pipeline] Collect ended.")
        }
    }

    /**
     * Appends incoming samples to the ring buffer.
     * Each time [HOP] new samples have been added, fires a YIN detection
     * on the current [WINDOW]-sized window.
     */
    private fun processBuffer(buffer: ShortArray) {
        var readPos = 0
        while (readPos < buffer.size) {
            ringBuffer[ringWritePos] = buffer[readPos]
            ringWritePos = (ringWritePos + 1) % WINDOW
            samplesAccumulated++
            readPos++

            // Fire detection every HOP new samples, once the buffer is filled
            if (samplesAccumulated >= WINDOW && samplesAccumulated % HOP == 0) {
                // Copy ring buffer into a contiguous window (oldest to newest)
                val window = ShortArray(WINDOW)
                for (i in 0 until WINDOW) {
                    window[i] = ringBuffer[(ringWritePos + i) % WINDOW]
                }
                dispatchDetection(window)
            }
        }
    }

    private fun dispatchDetection(window: ShortArray) {
        // Log pre-YIN window statistics on every call
        val windowRms = run {
            var s = 0.0; for (v in window) s += v.toDouble() * v; kotlin.math.sqrt(s / window.size)
        }
        val windowPeak = window.maxOf { kotlin.math.abs(it.toInt()) }
        val formattedRms = "%.1f".format(windowRms)
        Log.d(TAG, "[Pipeline] PreYIN: windowRms=$formattedRms windowPeak=$windowPeak")

        val result: PitchResult = yinPitchDetector.detect(window)

        _rmsLevel.value = result.rms
        _confidence.value = result.confidence

        if (!result.valid) {
            // Silence or out-of-range — clear current pitch/note
            if (_pitchHz.value != 0f) {
                Log.d(TAG, "[Pipeline] Silence/invalid — clearing pitch.")
                _pitchHz.value = 0f
                _detectedNote.value = null
            }
            return
        }

        _pitchHz.value = result.frequencyHz

        val note = noteMapper.getNoteFromFrequency(result.frequencyHz)
        _detectedNote.value = note

        val formattedFreq = "%.2f".format(result.frequencyHz)
        val formattedConf = "%.3f".format(result.confidence)
        Log.d(TAG, "[Pipeline] pitchHz=$formattedFreq confidence=$formattedConf note=${note?.noteClass}${note?.octave}")

        if (note != null && result.confidence > 0.6f) {
            Log.d(TAG, "[Pipeline] Emitting note: ${note.noteClass}${note.octave} conf=$formattedConf")
            onNoteDetected?.invoke(note)
        }
    }

    fun stop() {
        Log.d(TAG, "[Pipeline] stop() called. Cancelling pipelineJob.")
        pipelineJob?.cancel()
    }
}
