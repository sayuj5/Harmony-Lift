package com.harmonylift.theory.domain.model

enum class NoteClass(val notation: String) {
    C("C"),
    C_SHARP("C#"),
    D("D"),
    D_SHARP("D#"),
    E("E"),
    F("F"),
    F_SHARP("F#"),
    G("G"),
    G_SHARP("G#"),
    A("A"),
    A_SHARP("A#"),
    B("B");

    companion object {
        fun fromNotation(notation: String): NoteClass {
            return entries.firstOrNull { it.notation == notation }
                ?: throw IllegalArgumentException("Unknown note notation: $notation")
        }
    }
}

data class Note(
    val noteClass: NoteClass,
    val octave: Int = 4,
    val frequency: Float = 0f,
    val confidence: Float = 1f,
    val centsDeviation: Int = 0
) {
    val midiNumber: Int
        get() = (octave + 1) * 12 + noteClass.ordinal

    override fun toString(): String = "${noteClass.notation}$octave"
}
