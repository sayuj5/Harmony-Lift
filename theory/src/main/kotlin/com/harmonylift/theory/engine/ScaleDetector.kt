package com.harmonylift.theory.engine

import com.harmonylift.theory.domain.model.NoteClass
import com.harmonylift.theory.domain.model.Scale
import com.harmonylift.theory.domain.model.ScaleType

object ScaleDetector {

    /**
     * Detects potential scales that contain the given set of notes.
     */
    fun detectScales(notes: List<NoteClass>): List<Scale> {
        val distinctNotes = notes.distinct()
        if (distinctNotes.isEmpty()) return emptyList()

        val possibleScales = mutableListOf<Scale>()

        // Check against every possible root note
        for (root in NoteClass.entries) {
            for (scaleType in ScaleType.entries) {
                val scaleNotes = buildScaleNotes(root, scaleType)
                if (scaleNotes.containsAll(distinctNotes)) {
                    possibleScales.add(Scale(root, scaleType, scaleNotes))
                }
            }
        }

        return possibleScales
    }

    private fun buildScaleNotes(root: NoteClass, scaleType: ScaleType): List<NoteClass> {
        val notes = mutableListOf(root)
        var currentOrdinal = root.ordinal

        for (interval in scaleType.intervals) {
            currentOrdinal = (currentOrdinal + interval) % 12
            notes.add(NoteClass.entries[currentOrdinal])
        }

        // Remove the duplicated octave note at the end if present
        return notes.dropLast(1)
    }
}
