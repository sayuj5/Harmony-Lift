package com.harmonylift.dsp.pitch

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class YinPitchDetectorTest {

    @Test
    fun testDetect440Hz() {
        val sampleRate = 44100f
        val detector = YinPitchDetector(sampleRate)
        
        // Generate 1 second of 440Hz sine wave
        val buffer = ShortArray(2048)
        val frequency = 440.0
        
        for (i in buffer.indices) {
            val t = i / sampleRate.toDouble()
            buffer[i] = (sin(2.0 * PI * frequency * t) * Short.MAX_VALUE).toInt().toShort()
        }

        val detectedPitch = detector.detect(buffer)
        
        // Allow a small margin of error (e.g. 1% -> ~4.4Hz)
        assertEquals(440f, detectedPitch, 5f)
    }
}
