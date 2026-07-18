package com.harmonylift.theory.engine

import com.harmonylift.theory.domain.model.Chord
import com.harmonylift.theory.domain.model.Interval
import com.harmonylift.theory.domain.model.Note
import com.harmonylift.theory.domain.model.NoteClass
import com.harmonylift.theory.domain.model.Scale

class TheoryEngine {

    fun detectChord(notes: List<Note>): Chord? {
        return ChordDetector.detectChord(notes)
    }

    fun detectInterval(note1: Note, note2: Note): Interval? {
        return IntervalDetector.detectInterval(note1, note2)
    }

    fun detectScales(notes: List<NoteClass>): List<Scale> {
        return ScaleDetector.detectScales(notes)
    }
}
