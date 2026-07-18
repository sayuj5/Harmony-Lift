package com.harmonylift.theory.engine

import com.harmonylift.theory.domain.model.Interval
import com.harmonylift.theory.domain.model.IntervalType
import com.harmonylift.theory.domain.model.Note

object IntervalDetector {

    /**
     * Detects the interval between two notes.
     */
    fun detectInterval(note1: Note, note2: Note): Interval? {
        val (lower, higher) = if (note1.midiNumber <= note2.midiNumber) {
            note1 to note2
        } else {
            note2 to note1
        }

        val semitones = higher.midiNumber - lower.midiNumber
        val intervalType = IntervalType.fromSemitones(semitones)

        return intervalType?.let {
            Interval(
                type = it,
                root = lower,
                topNote = higher
            )
        }
    }
}
