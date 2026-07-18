package com.harmonylift.ui.components

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InstrumentRenderer(
    instrument: String,
    currentNote: String?,
    modifier: Modifier = Modifier
) {
    when (instrument.lowercase()) {
        "piano" -> PianoKeyboard(currentNote = currentNote, modifier = modifier)
        "guitar" -> StringedFretboard(currentNote = currentNote, strings = 6, modifier = modifier)
        "ukulele" -> StringedFretboard(currentNote = currentNote, strings = 4, modifier = modifier)
        "violin" -> StringedFretboard(currentNote = currentNote, strings = 4, modifier = modifier, isFretless = true)
        else -> PianoKeyboard(currentNote = currentNote, modifier = modifier)
    }
}

@Composable
fun PianoKeyboard(
    currentNote: String?,
    modifier: Modifier = Modifier
) {
    val whiteKeys = listOf("C", "D", "E", "F", "G", "A", "B")
    val blackKeys = listOf(0, 1, 3, 4, 5) // Indices after which a black key exists
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            whiteKeys.forEachIndexed { index, note ->
                val isHighlighted = currentNote?.startsWith(note) == true && currentNote?.contains("#") == false
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(0.5.dp, Color.LightGray)
                        .background(if (isHighlighted) MaterialTheme.colorScheme.primary else Color.White),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (isHighlighted) {
                        Box(modifier = Modifier.padding(bottom = 12.dp).size(24.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                            Text(
                                text = note,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
        
        // Draw black keys
        Row(modifier = Modifier.fillMaxSize()) {
            whiteKeys.forEachIndexed { index, _ ->
                Box(modifier = Modifier.weight(1f)) {
                    if (index in blackKeys) {
                        val isHighlighted = currentNote?.startsWith(whiteKeys[index]) == true && currentNote?.contains("#") == true
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(80.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (30).dp) // roughly half width
                                .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                                .background(if (isHighlighted) MaterialTheme.colorScheme.tertiary else Color(0xFF222222))
                                .border(1.dp, Color.Black, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StringedFretboard(
    currentNote: String?,
    strings: Int,
    isFretless: Boolean = false,
    modifier: Modifier = Modifier
) {
    val fretboardColor = Color(0xFF2C1A10)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(fretboardColor, fretboardColor.copy(alpha = 0.8f))
                )
            )
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Frets
        if (!isFretless) {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly) {
                for (i in 0..4) {
                    Box(modifier = Modifier.fillMaxHeight().width(3.dp).background(Brush.horizontalGradient(listOf(Color(0xFFB0B0B0), Color(0xFFD3D3D3)))))
                }
            }
            
            // Fret markers
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.6f)))
                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.6f)))
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        
        // Strings
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 0 until strings) {
                val stringThickness = if (isFretless) 1.5.dp else (1.dp + (i * 0.5).dp)
                val stringColor = if (isFretless) Color(0xFFCCCCCC) else Color(0xFFDDDDDD)
                Box(modifier = Modifier.fillMaxWidth().height(stringThickness).background(stringColor))
            }
        }
        
        // Finger Marker
        if (currentNote != null && currentNote != "--") {
            val infiniteTransition = rememberInfiniteTransition(label = "finger_pulse")
            val pulse by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "finger_pulse"
            )
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size((24 * pulse).dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentNote, 
                        color = MaterialTheme.colorScheme.onPrimary, 
                        fontWeight = FontWeight.ExtraBold, 
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
