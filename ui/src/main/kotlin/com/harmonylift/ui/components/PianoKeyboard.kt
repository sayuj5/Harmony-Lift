package com.harmonylift.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.harmonylift.theory.domain.model.Note
import com.harmonylift.theory.domain.model.NoteClass

@Composable
fun PianoKeyboard(
    highlightedNote: Note?,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        val totalWhiteKeys = 14 // 2 octaves
        val whiteKeyWidth = size.width / totalWhiteKeys
        val whiteKeyHeight = size.height
        val blackKeyWidth = whiteKeyWidth * 0.6f
        val blackKeyHeight = whiteKeyHeight * 0.65f

        val whiteKeysMap = listOf(
            NoteClass.C, NoteClass.D, NoteClass.E, NoteClass.F, NoteClass.G, NoteClass.A, NoteClass.B
        )

        val blackKeysMap = listOf(
            NoteClass.C_SHARP, NoteClass.D_SHARP, null, NoteClass.F_SHARP, NoteClass.G_SHARP, NoteClass.A_SHARP, null
        )

        // Draw White Keys
        for (i in 0 until totalWhiteKeys) {
            val noteClass = whiteKeysMap[i % 7]
            val isHighlighted = highlightedNote?.noteClass == noteClass && 
                                (highlightedNote.octave == 4 + (i / 7) || highlightedNote.octave == 3 + (i/7)) // Rough octave mapping

            drawRoundRect(
                color = if (isHighlighted) Color.Green.copy(alpha = 0.5f) else Color.White,
                topLeft = Offset(i * whiteKeyWidth, 0f),
                size = Size(whiteKeyWidth - 2f, whiteKeyHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }

        // Draw Black Keys
        for (i in 0 until totalWhiteKeys) {
            val blackNoteClass = blackKeysMap[i % 7]
            if (blackNoteClass != null) {
                val isHighlighted = highlightedNote?.noteClass == blackNoteClass

                drawRoundRect(
                    color = if (isHighlighted) Color.Green else Color.Black,
                    topLeft = Offset(i * whiteKeyWidth + (whiteKeyWidth - blackKeyWidth / 2), 0f),
                    size = Size(blackKeyWidth, blackKeyHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }
        }
    }
}
