package com.harmonylift.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow

@Composable
fun LiveWaveform(
    waveformFlow: StateFlow<ShortArray>,
    modifier: Modifier = Modifier,
    color: Color = Color.Cyan
) {
    val waveform by waveformFlow.collectAsState()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        if (waveform.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val centerY = height / 2

        val path = Path()
        
        // Downsample for rendering performance
        val step = (waveform.size / width).toInt().coerceAtLeast(1)
        
        path.moveTo(0f, centerY)

        for (i in 0 until width.toInt()) {
            val sampleIndex = i * step
            if (sampleIndex < waveform.size) {
                // Short.MAX_VALUE is 32767
                val normalizedSample = waveform[sampleIndex] / 32768f 
                val y = centerY - (normalizedSample * centerY)
                
                if (i == 0) {
                    path.moveTo(i.toFloat(), y)
                } else {
                    path.lineTo(i.toFloat(), y)
                }
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
