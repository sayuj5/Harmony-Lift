package com.harmonylift.theory.domain.model

enum class IntervalType(val semitones: Int, val notation: String) {
    PERFECT_UNISON(0, "P1"),
    MINOR_SECOND(1, "m2"),
    MAJOR_SECOND(2, "M2"),
    MINOR_THIRD(3, "m3"),
    MAJOR_THIRD(4, "M3"),
    PERFECT_FOURTH(5, "P4"),
    TRITONE(6, "d5/A4"),
    PERFECT_FIFTH(7, "P5"),
    MINOR_SIXTH(8, "m6"),
    MAJOR_SIXTH(9, "M6"),
    MINOR_SEVENTH(10, "m7"),
    MAJOR_SEVENTH(11, "M7"),
    PERFECT_OCTAVE(12, "P8");

    companion object {
        fun fromSemitones(semitones: Int): IntervalType? {
            val normalized = (semitones % 12 + 12) % 12
            return entries.firstOrNull { it.semitones == normalized }
        }
    }
}

data class Interval(
    val type: IntervalType,
    val root: Note,
    val topNote: Note
) {
    override fun toString(): String = "${type.notation} (${root.noteClass.notation} to ${topNote.noteClass.notation})"
}
