package com.harmonylift.theory.engine

import com.harmonylift.theory.domain.model.ChordType
import com.harmonylift.theory.domain.model.Note
import com.harmonylift.theory.domain.model.NoteClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TheoryEngineTest {

    private val engine = TheoryEngine()

    @Test
    fun testCMajorDetection() {
        val notes = listOf(
            Note(NoteClass.C, 4),
            Note(NoteClass.E, 4),
            Note(NoteClass.G, 4)
        )

        val chord = engine.detectChord(notes)
        
        assertNotNull("Chord should not be null", chord)
        assertEquals("C", chord?.root?.notation)
        assertEquals(ChordType.MAJOR, chord?.type)
        assertEquals("C Major", chord?.name)
    }

    @Test
    fun testAMinorDetection() {
        val notes = listOf(
            Note(NoteClass.A, 3),
            Note(NoteClass.C, 4),
            Note(NoteClass.E, 4)
        )

        val chord = engine.detectChord(notes)
        
        assertNotNull("Chord should not be null", chord)
        assertEquals("A", chord?.root?.notation)
        assertEquals(ChordType.MINOR, chord?.type)
        assertEquals("A Minor", chord?.name)
    }

    @Test
    fun testIntervalDetection() {
        val noteC = Note(NoteClass.C, 4)
        val noteG = Note(NoteClass.G, 4)

        val interval = engine.detectInterval(noteC, noteG)
        
        assertNotNull(interval)
        assertEquals("P5 (C to G)", interval?.toString())
    }

    @Test
    fun testScaleDetection() {
        val notes = listOf(
            NoteClass.C, NoteClass.D, NoteClass.E, NoteClass.F, NoteClass.G, NoteClass.A, NoteClass.B
        )

        val scales = engine.detectScales(notes)
        val cMajorScale = scales.find { it.name == "C Major Scale" }
        
        assertNotNull(cMajorScale)
    }
}
