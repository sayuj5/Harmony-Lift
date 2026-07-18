package com.harmonylift.audio

import java.util.concurrent.atomic.AtomicInteger

/**
 * A thread-safe circular buffer for storing continuous PCM audio frames.
 */
class AudioBuffer(val capacity: Int) {
    private val buffer = ShortArray(capacity)
    private val writeIndex = AtomicInteger(0)

    fun write(data: ShortArray, length: Int) {
        val currentWrite = writeIndex.get()
        for (i in 0 until length) {
            buffer[(currentWrite + i) % capacity] = data[i]
        }
        writeIndex.set((currentWrite + length) % capacity)
    }

    /**
     * Reads the most recent [length] samples into the provided [outBuffer].
     */
    fun readRecent(outBuffer: ShortArray, length: Int) {
        require(length <= capacity) { "Cannot read more than buffer capacity" }
        require(outBuffer.size >= length) { "outBuffer is too small" }
        
        val currentWrite = writeIndex.get()
        var startRead = currentWrite - length
        if (startRead < 0) {
            startRead += capacity
        }

        for (i in 0 until length) {
            outBuffer[i] = buffer[(startRead + i) % capacity]
        }
    }
}
