package com.harmonylift.tutor.data.local

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class LocalModelManagerTest {

    @Test
    fun testModelMissingFallback() = runBlocking {
        val manager = LocalModelManager()
        val nonExistentFile = File("does_not_exist.gguf")
        
        val result = manager.loadModel(nonExistentFile)
        
        assertTrue(result.isFailure)
        assertFalse(manager.isModelLoaded)
        
        // Ensure exception message matches our fallback logic
        val exception = result.exceptionOrNull()
        assertTrue(exception is java.io.FileNotFoundException)
    }

    @Test
    fun testCorruptedFileFallback() = runBlocking {
        val manager = LocalModelManager()
        
        // Create a fake file that is NOT a valid GGUF (missing GGUF magic header)
        val tempFile = Files.createTempFile("corrupted", ".gguf").toFile()
        tempFile.writeBytes(byteArrayOf(0x00, 0x01, 0x02, 0x03)) // Invalid magic number
        
        val result = manager.loadModel(tempFile)
        
        assertTrue(result.isFailure)
        assertFalse(manager.isModelLoaded)
        
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertTrue(exception?.message?.contains("not a valid GGUF") == true)
        
        tempFile.delete()
    }
}
