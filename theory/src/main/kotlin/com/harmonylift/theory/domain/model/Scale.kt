package com.harmonylift.theory.domain.model

enum class ScaleType(val intervals: List<Int>, val notation: String) {
    MAJOR(listOf(2, 2, 1, 2, 2, 2, 1), "Major Scale"),
    NATURAL_MINOR(listOf(2, 1, 2, 2, 1, 2, 2), "Natural Minor Scale"),
    HARMONIC_MINOR(listOf(2, 1, 2, 2, 1, 3, 1), "Harmonic Minor Scale"),
    PENTATONIC_MAJOR(listOf(2, 2, 3, 2, 3), "Major Pentatonic"),
    PENTATONIC_MINOR(listOf(3, 2, 2, 3, 2), "Minor Pentatonic");
}

data class Scale(
    val root: NoteClass,
    val type: ScaleType,
    val notes: List<NoteClass>
) {
    val name: String
        get() = "${root.notation} ${type.notation}"
        
    override fun toString(): String = name
}
