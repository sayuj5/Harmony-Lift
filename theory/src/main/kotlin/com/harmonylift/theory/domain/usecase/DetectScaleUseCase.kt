package com.harmonylift.theory.domain.usecase

import com.harmonylift.theory.domain.model.NoteClass
import com.harmonylift.theory.domain.model.Scale
import com.harmonylift.theory.engine.TheoryEngine

class DetectScaleUseCase(private val theoryEngine: TheoryEngine) {
    operator fun invoke(notes: List<NoteClass>): List<Scale> {
        return theoryEngine.detectScales(notes)
    }
}
