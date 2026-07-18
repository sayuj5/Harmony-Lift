package com.harmonylift.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class AudioBufferTest {

    @Test
    fun testCircularWriteAndRead() {
        val buffer = AudioBuffer(10)
        
        // Write 6 samples
        buffer.write(shortArrayOf(1, 2, 3, 4, 5, 6), 6)
        
        val out = ShortArray(4)
        buffer.readRecent(out, 4) // Should read last 4: 3, 4, 5, 6
        
        assertEquals(3.toShort(), out[0])
        assertEquals(4.toShort(), out[1])
        assertEquals(5.toShort(), out[2])
        assertEquals(6.toShort(), out[3])
        
        // Write 6 more samples, causing wrap around
        buffer.write(shortArrayOf(7, 8, 9, 10, 11, 12), 6)
        
        val out2 = ShortArray(5)
        buffer.readRecent(out2, 5) // Should read last 5: 8, 9, 10, 11, 12
        
        assertEquals(8.toShort(), out2[0])
        assertEquals(12.toShort(), out2[4])
    }
}
