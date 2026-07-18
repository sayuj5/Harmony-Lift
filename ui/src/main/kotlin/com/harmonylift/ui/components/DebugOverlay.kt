package com.harmonylift.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harmonylift.ui.BuildConfig
import com.harmonylift.theory.domain.model.Note

/**
 * An overlay that displays realtime DSP and engine statistics.
 * Only rendered in debug builds.
 */
@Composable
fun DebugOverlay(
    modifier: Modifier = Modifier,
    rmsLevel: Float,
    pitchHz: Float,
    confidence: Float,
    detectedNote: Note?
) {
    if (!BuildConfig.DEBUG) return

    Box(
        modifier = modifier
            .padding(8.dp)
            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(12.dp)
            .wrapContentSize()
    ) {
        Column {
            Text(
                text = "DSP DEBUG",
                color = Color.Yellow,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            DebugRow("RMS (Gate 150)", "%.1f".format(rmsLevel))
            DebugRow("Pitch (Hz)", "%.1f".format(pitchHz))
            DebugRow("Confidence", "%.2f".format(confidence))
            DebugRow("Note", "${detectedNote?.noteClass ?: "-"}${detectedNote?.octave ?: ""}")
            
            val stateColor = if (rmsLevel > 150f && confidence > 0.6f) Color.Green else Color.LightGray
            Text(
                text = if (rmsLevel < 150f) "SILENCE" else if (pitchHz > 0) "DETECTING" else "NO PITCH",
                color = stateColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun DebugRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(0.4f),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = Color.Cyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}
