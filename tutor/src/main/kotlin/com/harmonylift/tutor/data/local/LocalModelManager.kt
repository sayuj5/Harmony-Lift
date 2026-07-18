package com.harmonylift.tutor.data.local

import android.util.Log
import com.harmonylift.tutor.domain.repository.LLMService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

private const val TAG = "HarmonyLiftDebug"

class LocalModelManager {
    
    var isModelLoaded: Boolean = false
        private set

    private var activeService: LLMService? = null
    private val mutex = Mutex()

    // Config for low-memory environments
    private val nCtx = 512
    private val nThreads = 4

    suspend fun loadModel(modelFile: File): Result<LLMService> = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                Log.d(TAG, "[ModelManager] loadModel() called. path=${modelFile.absolutePath}")

                if (activeService != null && isModelLoaded) {
                    Log.d(TAG, "[ModelManager] Model already loaded. Returning cached service.")
                    return@withContext Result.success(activeService!!)
                }

                val exists = modelFile.exists()
                val size = if (exists) modelFile.length() else -1L
                val canRead = if (exists) modelFile.canRead() else false
                Log.d(TAG, "[ModelManager] exists=$exists canRead=$canRead sizeBytes=$size (~${"%.1f".format(size / 1_048_576.0)} MB)")

                if (!exists) {
                    Log.e(TAG, "[ModelManager] ERROR: Model file not found at ${modelFile.absolutePath}")
                    throw FileNotFoundException("Model file not found at ${modelFile.absolutePath}")
                }
                
                if (!canRead) {
                    Log.e(TAG, "[ModelManager] ERROR: Cannot read model file (permission denied).")
                    throw SecurityException("Insufficient permissions to read model file.")
                }

                // Check GGUF magic number header "GGUF" in hex: 46 55 47 47
                val magicNumber = ByteArray(4)
                modelFile.inputStream().use { it.read(magicNumber) }
                val isGguf = magicNumber[0] == 0x47.toByte() && magicNumber[1] == 0x47.toByte() && magicNumber[2] == 0x55.toByte() && magicNumber[3] == 0x46.toByte()
                val magic = magicNumber.joinToString(" ") { "0x%02X".format(it) }
                Log.d(TAG, "[ModelManager] GGUF header check: bytes=[$magic] isValidGguf=$isGguf")

                if (!isGguf) {
                    Log.e(TAG, "[ModelManager] ERROR: File is not a valid GGUF model (header mismatch).")
                    throw IllegalArgumentException("File is corrupted or not a valid GGUF model.")
                }

                Log.d(TAG, "[ModelManager] Calling LlamaNative.loadModel() nCtx=$nCtx nThreads=$nThreads")
                val pointer = try {
                    val startMs = System.currentTimeMillis()
                    val result = LlamaNative.loadModel(modelFile.absolutePath, nCtx, nThreads)
                    Log.d(TAG, "[ModelManager] LlamaNative.loadModel() completed in ${System.currentTimeMillis() - startMs} ms")
                    result
                } catch (e: UnsatisfiedLinkError) {
                    Log.e(TAG, "[ModelManager] UnsatisfiedLinkError: libllama-android.so not found. ${e.message}")
                    0L
                }

                Log.d(TAG, "[ModelManager] LlamaNative.loadModel() returned pointer=$pointer (${if (pointer == 0L) "NULL - FAILED" else "valid"})")

                if (pointer == 0L) {
                    Log.e(TAG, "[ModelManager] ERROR: Native model context pointer is null. Initialization failed (OOM or corrupted model?).")
                    throw IllegalStateException("Failed to initialize native LLM model. Pointer is null.")
                }

                val service = LlamaCppService(pointer)
                activeService = service
                isModelLoaded = true
                Log.d(TAG, "[ModelManager] Model loaded successfully. LlamaCppService created.")
                
                Result.success(service)
                
            } catch (e: Exception) {
                isModelLoaded = false
                Log.e(TAG, "[ModelManager] loadModel() failed: ${e.javaClass.simpleName}: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun unloadModel() = withContext(Dispatchers.IO) {
        mutex.withLock {
            Log.d(TAG, "[ModelManager] unloadModel() called. isModelLoaded=$isModelLoaded")
            try {
                activeService?.close()
                Log.d(TAG, "[ModelManager] LlamaCppService.close() called (native free).")
            } finally {
                activeService = null
                isModelLoaded = false
                Log.d(TAG, "[ModelManager] Model unloaded. activeService=null isModelLoaded=false")
            }
        }
    }
}

