package com.harmonylift.dsp.note

import com.harmonylift.theory.domain.model.Note
import com.harmonylift.theory.domain.model.NoteClass
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

class NoteMapper(
    private val a4Frequency: Float = 440.0f
) {
    private var lastNote: Note? = null
    private var currentStableNote: Note? = null
    private var noteStabilityCount = 0
    private var unstableFrames = 0
    
    private val debounceThreshold = 2
    private val dropThreshold = 5

    /**
     * Converts frequency in Hz to a Note object.
     * Returns null if frequency is invalid or unconfident (e.g. no pitch detected).
     */
    fun getNoteFromFrequency(frequency: Float): Note? {
        if (frequency <= 0f) {
            unstableFrames++
            if (unstableFrames >= dropThreshold) {
                currentStableNote = null
                lastNote = null
                noteStabilityCount = 0
            }
            return currentStableNote
        }

        // Calculate half steps from A4
        val halfStepsFromA4 = (12 * log2(frequency / a4Frequency)).roundToInt()
        
        // C0 is 57 half steps below A4
        val midiNote = halfStepsFromA4 + 69 

        if (midiNote < 0 || midiNote > 127) return currentStableNote

        val noteClassOrdinal = midiNote % 12
        val octave = (midiNote / 12) - 1

        val noteClass = NoteClass.entries[noteClassOrdinal]
        
        // Calculate exact frequency of this musical note
        val exactFrequency = a4Frequency * 2.0.pow((midiNote - 69) / 12.0).toFloat()
        
        // Confidence calculation (closer to exact frequency = higher confidence)
        val diff = kotlin.math.abs(frequency - exactFrequency)
        val tolerance = exactFrequency * 0.04f // 4% tolerance
        val confidence = (1.0f - (diff / tolerance)).coerceIn(0f, 1f)

        val centsDeviation = (1200 * log2(frequency / exactFrequency)).roundToInt()

        val newNote = Note(noteClass, octave, frequency, confidence, centsDeviation)

        // Debouncing logic with hysteresis
        if (lastNote?.noteClass == newNote.noteClass && lastNote?.octave == newNote.octave) {
            noteStabilityCount++
            unstableFrames = 0
        } else {
            noteStabilityCount = 0
            unstableFrames++
        }
        
        lastNote = newNote

        if (noteStabilityCount >= debounceThreshold) {
            currentStableNote = newNote
        } else if (unstableFrames >= dropThreshold) {
            // Fix: Actually clear the stable note when we have too many unstable frames
            currentStableNote = null
        }

        return currentStableNote
    }

    /**
     * Resets the mapper state so no previous note history bleeds into a new session.
     */
    fun reset() {
        lastNote = null
        currentStableNote = null
        noteStabilityCount = 0
        unstableFrames = 0
    }
}
