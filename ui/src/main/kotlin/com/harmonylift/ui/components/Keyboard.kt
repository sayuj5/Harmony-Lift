package com.harmonylift.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.harmonylift.theory.domain.model.Note
import com.harmonylift.theory.domain.model.NoteClass

/**
 * A polished Piano Keyboard composable spanning one octave.
 * Highlighted keys animate smoothly when [activeNotes] changes.
 */
@Composable
fun PianoKeyboard(
    activeNotes: Set<Note>,
    modifier: Modifier = Modifier
) {
    val whiteKeys = listOf(
        NoteClass.C, NoteClass.D, NoteClass.E,
        NoteClass.F, NoteClass.G, NoteClass.A, NoteClass.B
    )

    // null means no black key at that white-key gap (E-F, B-C)
    val blackKeys: List<NoteClass?> = listOf(
        NoteClass.C_SHARP, NoteClass.D_SHARP, null,
        NoteClass.F_SHARP, NoteClass.G_SHARP, NoteClass.A_SHARP, null
    )

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val outline = MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .border(1.5.dp, outline, RoundedCornerShape(8.dp))
    ) {
        // White keys layer
        Row(modifier = Modifier.fillMaxWidth()) {
            whiteKeys.forEach { noteClass ->
                val isActive = activeNotes.any { it.noteClass == noteClass }
                val keyColor by animateColorAsState(
                    targetValue = if (isActive) primary else Color.White,
                    animationSpec = tween(150),
                    label = "white_key_color_${noteClass.name}"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = 1.dp)
                        .background(
                            color = keyColor,
                            shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
                        )
                )
            }
        }

        // Black keys overlay (60% height)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            blackKeys.forEach { noteClass ->
                if (noteClass != null) {
                    val isActive = activeNotes.any { it.noteClass == noteClass }
                    val keyColor by animateColorAsState(
                        targetValue = if (isActive) secondary else Color(0xFF2A2A2A),
                        animationSpec = tween(150),
                        label = "black_key_color_${noteClass.name}"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .zIndex(1f)
                            .padding(horizontal = 3.dp)
                            .background(
                                color = keyColor,
                                shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
                            )
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.weight(0.5f))
        }
    }
}


