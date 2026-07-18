package com.harmonylift.theory.domain.usecase

import com.harmonylift.theory.domain.model.Chord
import com.harmonylift.theory.domain.model.Note
import com.harmonylift.theory.engine.TheoryEngine

class DetectChordUseCase(private val theoryEngine: TheoryEngine) {
    
    private var lastDetectedChord: Chord? = null
    private var stabilityCount = 0
    private val debounceThreshold = 3

    operator fun invoke(notes: List<Note>): Chord? {
        var newChord = theoryEngine.detectChord(notes)
        if (newChord == null && notes.size > 3) {
            newChord = theoryEngine.detectChord(notes.takeLast(3))
        }
        
        if (newChord != null && newChord.name == lastDetectedChord?.name) {
            stabilityCount++
        } else {
            stabilityCount = 0
            lastDetectedChord = newChord
        }

        return if (stabilityCount >= debounceThreshold) lastDetectedChord else null
    }

    fun reset() {
        lastDetectedChord = null
        stabilityCount = 0
    }
}
