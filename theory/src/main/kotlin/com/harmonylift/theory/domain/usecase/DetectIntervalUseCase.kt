package com.harmonylift.theory.domain.usecase

import com.harmonylift.theory.domain.model.Interval
import com.harmonylift.theory.domain.model.Note
import com.harmonylift.theory.engine.TheoryEngine

class DetectIntervalUseCase(private val theoryEngine: TheoryEngine) {
    operator fun invoke(note1: Note, note2: Note): Interval? {
        return theoryEngine.detectInterval(note1, note2)
    }
}
