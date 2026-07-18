package com.harmonylift.dsp.note

import com.harmonylift.theory.domain.model.NoteClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NoteMapperTest {

    @Test
    fun testNoteMappingC4() {
        val mapper = NoteMapper(a4Frequency = 440f)
        
        // Exact C4 frequency is ~261.63Hz
        var note = mapper.getNoteFromFrequency(261.63f)
        assertNull("Should be null on first hit due to debouncing", note)
        
        note = mapper.getNoteFromFrequency(261.63f)
        assertNull("Should be null on second hit due to debouncing", note)
        
        note = mapper.getNoteFromFrequency(261.63f)
        assertNotNull("Should not be null on third hit (debounce passed)", note)
        
        assertEquals(NoteClass.C, note?.noteClass)
        assertEquals(4, note?.octave)
        assertEquals(1.0f, note?.confidence ?: 0f, 0.05f)
    }
}
