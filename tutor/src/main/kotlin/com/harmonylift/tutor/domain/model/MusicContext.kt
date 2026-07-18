package com.harmonylift.tutor.domain.model

data class MusicContext(
    val detectedChord: String?,
    val detectedScale: String?,
    val recentNotes: List<String>,
    val confidence: Float,
    val skillLevel: String = "Beginner"
) {
    /**
     * Serializes context to a lightweight structured format for the LLM.
     */
    fun toStructuredString(): String {
        return """
            {
              "detectedChord":"${detectedChord ?: "Unknown"}",
              "detectedScale":"${detectedScale ?: "Unknown"}",
              "recentNotes":${recentNotes.joinToString(prefix="[", postfix="]", separator=", ") { "\"$it\"" }},
              "confidence":$confidence,
              "skillLevel":"$skillLevel"
            }
        """.trimIndent()
    }
}
