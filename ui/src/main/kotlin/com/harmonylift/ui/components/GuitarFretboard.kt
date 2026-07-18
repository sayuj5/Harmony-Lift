package com.harmonylift.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.harmonylift.theory.domain.model.Note

@Composable
fun GuitarFretboard(
    highlightedNote: Note?,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val numStrings = 6
        val numFrets = 12
        val stringSpacing = size.height / (numStrings + 1)
        val fretSpacing = size.width / numFrets

        // Draw Strings
        for (i in 1..numStrings) {
            val y = i * stringSpacing
            val thickness = (numStrings - i + 1) * 0.5f // Thicker strings at the bottom (E string)
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = thickness.dp.toPx()
            )
        }

        // Draw Frets
        for (i in 0..numFrets) {
            val x = i * fretSpacing
            drawLine(
                color = Color.Gray,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Simplified highlight logic: standard tuning EADGBE mapping
        // In a full implementation, we'd map Note(C, 4) to specific String/Fret pairs.
        if (highlightedNote != null) {
            // Mocking a highlight for demonstration purposes on the 3rd string
            drawCircle(
                color = Color.Cyan.copy(alpha = 0.8f),
                radius = 12.dp.toPx(),
                center = Offset(fretSpacing * 2.5f, 3 * stringSpacing)
            )
        }
    }
}
