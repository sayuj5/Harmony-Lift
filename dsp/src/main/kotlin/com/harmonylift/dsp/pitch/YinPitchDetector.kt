package com.harmonylift.dsp.pitch

import android.util.Log
import kotlin.math.abs
import kotlin.math.sqrt

private const val TAG = "HarmonyLiftDebug"

/**
 * YIN pitch detector — corrected implementation.
 *
 * Key fixes vs. the previous version:
 *  1. **Thread-safe** — yinBuffer is local to each detect() call, no shared mutable state.
 *  2. **Fixed window** — always operates on [WINDOW_SIZE] samples regardless of incoming buffer
 *     size, using a ring buffer maintained by the caller (AudioToTheoryPipeline).
 *  3. **Noise gate** — rejects buffers where RMS < [RMS_THRESHOLD] (silence, hand on mic, etc.)
 *     so no spurious low-frequency readings are emitted.
 *  4. **Range clamp** — results outside [MIN_FREQ_HZ]..[MAX_FREQ_HZ] are discarded.
 *  5. **Correct confidence** — uses the CMNDF value at tauEstimate (lower = more confident)
 *     mapped to a [0..1] range. Previous formula based on 4% frequency tolerance was wrong.
 *
 * Detects reliably from guitar low-E (82 Hz) up to above high-C (1047 Hz).
 */
class YinPitchDetector(
    private val sampleRate: Float = 44100f,
    private val threshold: Float = 0.10f,   // lower = stricter pitch requirement
    private val rmsThreshold: Float = 30f,  // raw 16-bit amplitude; 30 ≈ 0.09% full-scale
    private val minFreqHz: Float = 60f,     // covers guitar low-E (82 Hz) with margin
    private val maxFreqHz: Float = 1400f    // covers treble range
) {
    companion object {
        const val WINDOW_SIZE = 4096  // must be power-of-2; gives half-window of 2048
    }

    /**
     * Detect the pitch from a fixed [WINDOW_SIZE] sample window.
     * Returns a [PitchResult] — check [PitchResult.valid] before using.
     * This method is **stateless and thread-safe** (all buffers are stack-local).
     *
     * @param window exactly [WINDOW_SIZE] samples of 16-bit PCM from the ring buffer.
     */
    fun detect(window: ShortArray): PitchResult {
        require(window.size == WINDOW_SIZE) {
            "YIN requires exactly $WINDOW_SIZE samples, got ${window.size}"
        }

        // --- Noise gate: skip detection on silence ---
        val rms = computeRms(window)
        if (rms < rmsThreshold) {
            return PitchResult.silent(rms)
        }

        val halfSize = WINDOW_SIZE / 2
        // yinBuffer is LOCAL — this makes the method thread-safe
        val yinBuffer = FloatArray(halfSize)

        // --- Step 1: Difference function ---
        for (tau in 0 until halfSize) {
            var sum = 0.0
            for (i in 0 until halfSize) {
                val delta = window[i].toDouble() - window[i + tau].toDouble()
                sum += delta * delta
            }
            yinBuffer[tau] = sum.toFloat()
        }

        // --- Step 2: Cumulative mean normalized difference (CMNDF) ---
        yinBuffer[0] = 1f
        var runningSum = 0.0
        for (tau in 1 until halfSize) {
            runningSum += yinBuffer[tau]
            yinBuffer[tau] = (yinBuffer[tau] * tau / runningSum).toFloat()
        }

        // --- Step 3: Absolute threshold — find first dip below threshold ---
        var tauEstimate = -1
        for (tau in 2 until halfSize) {
            if (yinBuffer[tau] < threshold) {
                // Descend to local minimum
                var t = tau
                while (t + 1 < halfSize && yinBuffer[t + 1] < yinBuffer[t]) {
                    t++
                }
                tauEstimate = t
                break
            }
        }

        if (tauEstimate < 2) {
            Log.d(TAG, "[YIN] No dip found below threshold=${threshold}. rms=${"%.1f".format(rms)}")
            return PitchResult.noPitch(rms)
        }

        // --- Step 4: Parabolic interpolation for sub-sample accuracy ---
        var betterTau = tauEstimate.toFloat()
        if (tauEstimate in 1 until halfSize - 1) {
            val s0 = yinBuffer[tauEstimate - 1]
            val s1 = yinBuffer[tauEstimate]
            val s2 = yinBuffer[tauEstimate + 1]
            val denom = s0 + s2 - 2f * s1
            if (abs(denom) > 1e-6f) {
                betterTau += (s0 - s2) / (2f * denom)
            }
        }

        val frequency = sampleRate / betterTau

        // --- Step 5: Range validation ---
        if (frequency < minFreqHz || frequency > maxFreqHz) {
            Log.d(TAG, "[YIN] Out-of-range: freq=${"%.1f".format(frequency)} Hz (valid: $minFreqHz–$maxFreqHz). Discarding.")
            return PitchResult.noPitch(rms)
        }

        // --- Step 6: Confidence from CMNDF value (lower CMNDF = higher confidence) ---
        val cmndfAtTau = yinBuffer[tauEstimate].coerceIn(0f, threshold)
        val confidence = 1f - (cmndfAtTau / threshold)

        Log.d(TAG, "[YIN] freq=${"%.2f".format(frequency)} Hz tau=$tauEstimate cmndf=${"%.4f".format(cmndfAtTau)} confidence=${"%.3f".format(confidence)} rms=${"%.1f".format(rms)}")

        return PitchResult(
            frequencyHz = frequency,
            confidence = confidence,
            rms = rms,
            valid = true
        )
    }

    private fun computeRms(window: ShortArray): Float {
        var sumSq = 0.0
        for (s in window) sumSq += s.toDouble() * s
        return sqrt(sumSq / window.size).toFloat()
    }
}

data class PitchResult(
    val frequencyHz: Float,
    val confidence: Float,
    val rms: Float,
    val valid: Boolean
) {
    companion object {
        fun silent(rms: Float) = PitchResult(0f, 0f, rms, false)
        fun noPitch(rms: Float) = PitchResult(0f, 0f, rms, false)
    }
}
