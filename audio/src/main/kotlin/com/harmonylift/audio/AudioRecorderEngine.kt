package com.harmonylift.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext
import kotlin.math.abs
import kotlin.math.sqrt

private const val TAG = "HarmonyLiftDebug"

class AudioRecorderEngine(
    private val sampleRate: Int = 44100,
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT,
    private val bufferSizeMultiplier: Int = 2
) {
    private val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private val bufferSize = minBufferSize * bufferSizeMultiplier

    val audioBuffer = AudioBuffer(sampleRate * 2) // 2 seconds history

    @SuppressLint("MissingPermission")
    fun startRecording(): Flow<ShortArray> = flow {
        Log.d(TAG, "[AudioRecorder] startRecording() called. sampleRate=$sampleRate channelConfig=$channelConfig audioFormat=$audioFormat")
        Log.d(TAG, "[AudioRecorder] minBufferSize=$minBufferSize bufferSize=$bufferSize")

        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "[AudioRecorder] ERROR: getMinBufferSize() failed with code=$minBufferSize. Microphone may be unavailable.")
            throw IllegalStateException("AudioRecord.getMinBufferSize() failed: $minBufferSize")
        }

        val audioRecord = AudioRecord(
            // VOICE_RECOGNITION: hardware-tuned for clean capture, disables aggressive
            // noise suppression that CAMCORDER/MIC can apply which crushes amplitude.
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        val state = audioRecord.state
        Log.d(TAG, "[AudioRecorder] AudioRecord created. state=${if (state == AudioRecord.STATE_INITIALIZED) "STATE_INITIALIZED" else "STATE_UNINITIALIZED ($state)"}")

        if (state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "[AudioRecorder] ERROR: AudioRecord failed to initialize. state=$state. Check RECORD_AUDIO permission.")
            throw IllegalStateException("AudioRecord initialization failed. state=$state")
        }

        // Disable Automatic Gain Control — AGC compresses signal and makes RMS appear flat
        val sessionId = audioRecord.audioSessionId
        if (AutomaticGainControl.isAvailable()) {
            val agc = AutomaticGainControl.create(sessionId)
            agc?.enabled = false
            Log.d(TAG, "[AudioRecorder] AGC available. Disabled=${agc?.enabled?.not() ?: false}")
        } else {
            Log.d(TAG, "[AudioRecorder] AGC not available on this device.")
        }
        // Disable NoiseSuppressor — it can zero out instrument signal below a speech threshold
        if (NoiseSuppressor.isAvailable()) {
            val ns = NoiseSuppressor.create(sessionId)
            ns?.enabled = false
            Log.d(TAG, "[AudioRecorder] NoiseSuppressor available. Disabled=${ns?.enabled?.not() ?: false}")
        } else {
            Log.d(TAG, "[AudioRecorder] NoiseSuppressor not available on this device.")
        }

        audioRecord.startRecording()
        val recordingState = audioRecord.recordingState
        Log.d(TAG, "[AudioRecorder] startRecording() called. recordingState=${if (recordingState == AudioRecord.RECORDSTATE_RECORDING) "RECORDSTATE_RECORDING" else "NOT_RECORDING ($recordingState)"}")

        val readBuffer = ShortArray(bufferSize / 2)
        var frameCount = 0L

        try {
            while (coroutineContext.isActive) {
                val readResult = audioRecord.read(readBuffer, 0, readBuffer.size)
                frameCount++

                if (readResult > 0) {
                    // Compute true RMS (not average absolute value) for accurate amplitude report
                    val rms = run {
                        var sumSq = 0.0
                        for (i in 0 until readResult) sumSq += readBuffer[i].toDouble() * readBuffer[i]
                        sqrt(sumSq / readResult)
                    }
                    // Log every 10 frames so diagnostics are visible without spamming
                    if (frameCount % 10 == 0L) {
                        val peak = readBuffer.take(readResult).maxOf { abs(it.toInt()) }
                        Log.d(TAG, "[AudioRecorder] frame=$frameCount readResult=$readResult rms=${"%.1f".format(rms)} peak=$peak")
                    }
                    audioBuffer.write(readBuffer, readResult)
                    val outArray = ShortArray(readResult)
                    System.arraycopy(readBuffer, 0, outArray, 0, readResult)
                    emit(outArray)
                } else if (readResult < 0) {
                    Log.w(TAG, "[AudioRecorder] audioRecord.read() returned error code: $readResult (frame=$frameCount)")
                    kotlinx.coroutines.delay(10L)
                }
            }
            Log.d(TAG, "[AudioRecorder] Recording loop ended (coroutine cancelled). totalFrames=$frameCount")
        } finally {
            Log.d(TAG, "[AudioRecorder] finally: stopping and releasing AudioRecord.")
            try {
                audioRecord.stop()
                Log.d(TAG, "[AudioRecorder] AudioRecord.stop() OK")
            } catch (e: Exception) {
                Log.e(TAG, "[AudioRecorder] Exception in audioRecord.stop(): ${e.message}")
            } finally {
                audioRecord.release()
                Log.d(TAG, "[AudioRecorder] AudioRecord.release() OK")
            }
        }
    }.flowOn(Dispatchers.IO)
}

