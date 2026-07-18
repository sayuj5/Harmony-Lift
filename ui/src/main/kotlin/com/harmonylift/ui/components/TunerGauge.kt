package com.harmonylift.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TunerGauge(
    centsDeviation: Int,
    currentNote: String?,
    modifier: Modifier = Modifier
) {
    // Animate the needle based on cents deviation
    val animatedCents by animateFloatAsState(
        targetValue = centsDeviation.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "needle_animation"
    )

    val isTuned = animatedCents in -5f..5f
    val needleColor = if (currentNote == null || currentNote == "--") MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) 
                      else if (isTuned) Color(0xFF4CAF50) 
                      else MaterialTheme.colorScheme.error

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.BottomCenter
    ) {
        val arcColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        val tunedColor = Color(0xFF4CAF50).copy(alpha = 0.4f)
        
        Canvas(modifier = Modifier.fillMaxSize().padding(top = 32.dp, start = 32.dp, end = 32.dp, bottom = 16.dp)) {
            val radius = size.width / 2f
            val center = Offset(size.width / 2f, size.height - 30.dp.toPx())

            // Draw background arc
            drawArc(
                color = arcColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(0f, size.height - radius - 30.dp.toPx()),
                size = Size(size.width, radius * 2)
            )

            // Draw tuned section arc (-10 to 10 cents roughly)
            drawArc(
                color = tunedColor,
                startAngle = 265f,
                sweepAngle = 10f,
                useCenter = false,
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(0f, size.height - radius - 30.dp.toPx()),
                size = Size(size.width, radius * 2)
            )

            // Draw tick marks
            for (i in -50..50 step 10) {
                val angle = 270f + (i * 1.5f) // map -50..50 cents to 195..345 degrees
                val angleRad = angle * (PI / 180f)
                val isMajor = i % 25 == 0
                val tickLength = if (isMajor) 16.dp.toPx() else 8.dp.toPx()
                val tickWidth = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx()
                
                val startX = center.x + (radius - 30.dp.toPx()) * cos(angleRad).toFloat()
                val startY = center.y + (radius - 30.dp.toPx()) * sin(angleRad).toFloat()
                
                val endX = center.x + (radius - 30.dp.toPx() - tickLength) * cos(angleRad).toFloat()
                val endY = center.y + (radius - 30.dp.toPx() - tickLength) * sin(angleRad).toFloat()

                drawLine(
                    color = arcColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickWidth,
                    cap = StrokeCap.Round
                )
            }

            // Draw Needle
            val needleAngle = 270f + (animatedCents.coerceIn(-50f, 50f) * 1.5f)
            val needleRad = needleAngle * (PI / 180f)
            val needleEndX = center.x + (radius - 20.dp.toPx()) * cos(needleRad).toFloat()
            val needleEndY = center.y + (radius - 20.dp.toPx()) * sin(needleRad).toFloat()

            drawLine(
                color = needleColor,
                start = center,
                end = Offset(needleEndX, needleEndY),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Draw center pivot
            drawCircle(
                color = needleColor,
                radius = 12.dp.toPx(),
                center = center
            )
        }
        
        // Overlay Cents Text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Flat",
                style = MaterialTheme.typography.labelMedium,
                color = if (animatedCents < -5f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (currentNote != null && currentNote != "--") "${animatedCents.toInt()}¢" else "--",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = needleColor
                )
            }

            Text(
                text = "Sharp",
                style = MaterialTheme.typography.labelMedium,
                color = if (animatedCents > 5f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
