package com.harmonylift.tutor.domain.repository

import kotlinx.coroutines.flow.Flow

interface LLMService {
    /**
     * Starts inference and returns a Flow of streamed text tokens.
     */
    fun streamResponse(prompt: String): Flow<String>

    /**
     * Synchronously generates a complete text response.
     */
    suspend fun generateResponse(prompt: String): String

    /**
     * Safely releases any allocated C++ resources.
     */
    fun close()
}
