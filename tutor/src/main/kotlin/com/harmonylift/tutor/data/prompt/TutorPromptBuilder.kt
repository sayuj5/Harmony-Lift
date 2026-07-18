package com.harmonylift.tutor.data.prompt

import com.harmonylift.tutor.domain.model.MusicContext

object TutorPromptBuilder {

    private const val SYSTEM_PROMPT = """
        You are Harmony, a premium AI Music Tutor and Coach.
        Your tone is highly encouraging, professional, and knowledgeable.
        You teach music theory, ear training, and instrument skills.
        Keep your responses concise, accurate, and actionable. Avoid generic platitudes.
        Do NOT generate JSON yourself. You will receive context in JSON format.
        Focus on providing: Music Theory explanation, Ear Training advice, Chord/Scale guidance, or Mistake Analysis based on what the user asks.
    """

    /**
     * Formats the prompt using the Llama 3.2 Instruct template.
     */
    fun buildTutorPrompt(context: MusicContext, userQuestion: String? = null): String {
        val userContent = buildString {
            append("User's current playing context:\n")
            append(context.toStructuredString())
            if (userQuestion != null) {
                append("\nUser asks: $userQuestion")
            } else {
                append("\nAnalyze what I'm playing and give me a specific music theory tip.")
            }
        }

        return """
            <|begin_of_text|><|start_header_id|>system<|end_header_id|>

            ${SYSTEM_PROMPT.trimIndent()}<|eot_id|><|start_header_id|>user<|end_header_id|>

            $userContent<|eot_id|><|start_header_id|>assistant<|end_header_id|>

        """.trimIndent()
    }

    fun buildSummaryPrompt(history: List<MusicContext>): String {
        val mostPlayedChord = history.groupBy { it.detectedChord }.maxByOrNull { it.value.size }?.key
        val mostPlayedScale = history.groupBy { it.detectedScale }.maxByOrNull { it.value.size }?.key

        return """
            <|begin_of_text|><|start_header_id|>system<|end_header_id|>

            ${SYSTEM_PROMPT.trimIndent()}<|eot_id|><|start_header_id|>user<|end_header_id|>

            The user just finished a practice session. 
            Most played chord: $mostPlayedChord.
            Most played scale: $mostPlayedScale.
            Give them a 2-sentence wrap-up and suggest one specific advanced exercise to practice next.<|eot_id|><|start_header_id|>assistant<|end_header_id|>

        """.trimIndent()
    }

    fun buildPracticeFeedbackPrompt(score: Int, accuracy: Float, mistakes: Int): String {
        return """
            <|begin_of_text|><|start_header_id|>system<|end_header_id|>

            ${SYSTEM_PROMPT.trimIndent()}<|eot_id|><|start_header_id|>user<|end_header_id|>

            I just finished a practice session.
            My score: ${score}
            My accuracy: ${(accuracy * 100).toInt()}%
            Mistakes made: ${mistakes}
            Please give me brief, actionable, personalized feedback on what to improve next. Don't be generic. If I made mistakes, tell me how to practice better.<|eot_id|><|start_header_id|>assistant<|end_header_id|>

        """.trimIndent()
    }
}
