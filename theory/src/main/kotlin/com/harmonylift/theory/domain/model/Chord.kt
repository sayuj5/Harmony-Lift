package com.harmonylift.theory.domain.model

enum class ChordType(val intervals: List<IntervalType>, val notation: String) {
    MAJOR(listOf(IntervalType.PERFECT_UNISON, IntervalType.MAJOR_THIRD, IntervalType.PERFECT_FIFTH), "Major"),
    MINOR(listOf(IntervalType.PERFECT_UNISON, IntervalType.MINOR_THIRD, IntervalType.PERFECT_FIFTH), "Minor"),
    DIMINISHED(listOf(IntervalType.PERFECT_UNISON, IntervalType.MINOR_THIRD, IntervalType.TRITONE), "Diminished"),
    AUGMENTED(listOf(IntervalType.PERFECT_UNISON, IntervalType.MAJOR_THIRD, IntervalType.MINOR_SIXTH), "Augmented"),
    DOMINANT_SEVENTH(listOf(IntervalType.PERFECT_UNISON, IntervalType.MAJOR_THIRD, IntervalType.PERFECT_FIFTH, IntervalType.MINOR_SEVENTH), "7"),
    MAJOR_SEVENTH(listOf(IntervalType.PERFECT_UNISON, IntervalType.MAJOR_THIRD, IntervalType.PERFECT_FIFTH, IntervalType.MAJOR_SEVENTH), "maj7"),
    MINOR_SEVENTH(listOf(IntervalType.PERFECT_UNISON, IntervalType.MINOR_THIRD, IntervalType.PERFECT_FIFTH, IntervalType.MINOR_SEVENTH), "m7");
}

data class Chord(
    val root: NoteClass,
    val type: ChordType,
    val notes: List<Note>
) {
    val name: String
        get() = "${root.notation} ${type.notation}"
        
    override fun toString(): String = name
}
