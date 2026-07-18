package com.harmonylift.tutor.data.local

import android.util.Log
import com.harmonylift.tutor.domain.repository.LLMService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "HarmonyLiftDebug"

class LlamaCppService(
    private val contextPointer: Long
) : LLMService {

    private val isClosed = AtomicBoolean(false)

    override fun streamResponse(prompt: String): Flow<String> = callbackFlow {
        Log.d(TAG, "[LlamaCpp] streamResponse() called. contextPointer=$contextPointer isClosed=${isClosed.get()}")
        Log.d(TAG, "[LlamaCpp] Prompt (first 200 chars): ${prompt.take(200)}")

        if (isClosed.get() || contextPointer == 0L) {
            Log.e(TAG, "[LlamaCpp] streamResponse() aborted: isClosed=${isClosed.get()} contextPointer=$contextPointer")
            close()
            return@callbackFlow
        }

        withContext(Dispatchers.IO) {
            var tokenCount = 0
            try {
                Log.d(TAG, "[LlamaCpp] Calling LlamaNative.streamTokens()...")
                LlamaNative.streamTokens(contextPointer, prompt) { token ->
                    tokenCount++
                    if (tokenCount <= 5 || tokenCount % 20 == 0) {
                        Log.d(TAG, "[LlamaCpp] token #$tokenCount: \"${token.replace("\n", "\\n")}\"")
                    }
                    if (isActive) {
                        trySend(token)
                    }
                }
                Log.d(TAG, "[LlamaCpp] streamTokens() completed. totalTokens=$tokenCount")
            } catch (e: Exception) {
                Log.e(TAG, "[LlamaCpp] JNI Exception in streamTokens(): ${e.javaClass.simpleName}: ${e.message}", e)
                close(e)
            } finally {
                Log.d(TAG, "[LlamaCpp] streamResponse finally block. isActive=$isActive")
                if (isActive) {
                    close()
                }
            }
        }
        awaitClose {
            Log.d(TAG, "[LlamaCpp] callbackFlow awaitClose() — stream cancelled by collector.")
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "[LlamaCpp] generateResponse() called. contextPointer=$contextPointer isClosed=${isClosed.get()}")
        if (isClosed.get() || contextPointer == 0L) {
            Log.e(TAG, "[LlamaCpp] generateResponse() aborted: not ready.")
            return@withContext ""
        }
        
        val sb = java.lang.StringBuilder()
        var tokenCount = 0
        try {
            Log.d(TAG, "[LlamaCpp] Calling LlamaNative.streamTokens() for generateResponse...")
            LlamaNative.streamTokens(contextPointer, prompt) { token ->
                tokenCount++
                sb.append(token)
            }
            Log.d(TAG, "[LlamaCpp] generateResponse() done. totalTokens=$tokenCount responseLength=${sb.length}")
        } catch (e: Exception) {
            Log.e(TAG, "[LlamaCpp] JNI Exception in generateResponse(): ${e.javaClass.simpleName}: ${e.message}", e)
        }
        sb.toString()
    }

    override fun close() {
        if (isClosed.compareAndSet(false, true)) {
            Log.d(TAG, "[LlamaCpp] close() called. Freeing native model. contextPointer=$contextPointer")
            if (contextPointer != 0L) {
                try {
                    LlamaNative.freeModel(contextPointer)
                    Log.d(TAG, "[LlamaCpp] LlamaNative.freeModel() completed OK.")
                } catch (e: Exception) {
                    Log.e(TAG, "[LlamaCpp] Exception in LlamaNative.freeModel(): ${e.message}", e)
                }
            }
        } else {
            Log.d(TAG, "[LlamaCpp] close() called but already closed (isClosed=true). Skipping.")
        }
    }
}

