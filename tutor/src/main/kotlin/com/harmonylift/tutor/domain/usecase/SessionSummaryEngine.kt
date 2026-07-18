package com.harmonylift.tutor.domain.usecase

import com.harmonylift.tutor.data.prompt.TutorPromptBuilder
import com.harmonylift.tutor.domain.model.MusicContext
import com.harmonylift.tutor.domain.repository.LLMService

class SessionSummaryEngine(
    private val llmService: LLMService
) {
    private val sessionHistory = mutableListOf<MusicContext>()

    fun recordContext(context: MusicContext) {
        sessionHistory.add(context)
        // Prevent unbounded memory growth
        if (sessionHistory.size > 500) {
            sessionHistory.removeAt(0)
        }
    }

    suspend fun generateSummary(): String {
        if (sessionHistory.isEmpty()) {
            return "No musical activity recorded in this session."
        }

        val prompt = TutorPromptBuilder.buildSummaryPrompt(sessionHistory)
        return try {
            llmService.generateResponse(prompt)
        } catch (e: Exception) {
            "Great practice session! Keep working on your chords."
        }
    }

    fun clearSession() {
        sessionHistory.clear()
    }
}
