package com.harmonylift.theory.engine

import com.harmonylift.theory.domain.model.Chord
import com.harmonylift.theory.domain.model.ChordType
import com.harmonylift.theory.domain.model.IntervalType
import com.harmonylift.theory.domain.model.Note
import com.harmonylift.theory.domain.model.NoteClass

object ChordDetector {

    /**
     * Detects a chord from a given list of notes.
     * Example: C4, E4, G4 -> C Major
     */
    fun detectChord(notes: List<Note>): Chord? {
        if (notes.size < 3) return null

        val distinctNotes = notes.map { it.noteClass }.distinct()
        if (distinctNotes.size < 3) return null

        // Try treating each note as the root
        for (rootNoteClass in distinctNotes) {
            val intervalsFromRoot = distinctNotes.mapNotNull { noteClass ->
                val semitones = (noteClass.ordinal - rootNoteClass.ordinal + 12) % 12
                IntervalType.fromSemitones(semitones)
            }.toSet()

            // Find matching chord type
            for (chordType in ChordType.entries) {
                if (intervalsFromRoot.containsAll(chordType.intervals) && 
                    chordType.intervals.size == intervalsFromRoot.size) {
                    
                    // Found a match
                    return Chord(
                        root = rootNoteClass,
                        type = chordType,
                        notes = notes.sortedBy { it.midiNumber }
                    )
                }
            }
        }

        return null
    }
}
