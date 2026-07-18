package com.harmonylift.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harmonylift.theory.presentation.TheoryScreenState
import com.harmonylift.ui.components.HarmonyLessonCard
import com.harmonylift.ui.components.LiveWaveform
import com.harmonylift.ui.components.InstrumentRenderer
import com.harmonylift.ui.components.TunerGauge
import com.harmonylift.ui.components.DebugOverlay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun LiveListeningScreen(
    state: TheoryScreenState,
    waveformFlow: StateFlow<ShortArray>,
    rmsFlow: StateFlow<Float> = MutableStateFlow(0f),
    pitchFlow: StateFlow<Float> = MutableStateFlow(0f),
    confidenceFlow: StateFlow<Float> = MutableStateFlow(0f),
    instrument: String = "Piano",
    onStopListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rms by rmsFlow.collectAsState()
    val pitch by pitchFlow.collectAsState()
    val conf by confidenceFlow.collectAsState()
    val lastDetectedNote = state.recentNotes.lastOrNull()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val recordingPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recording_pulse"
    )

    val currentNoteStr = state.recentNotes.lastOrNull()?.toString()
    val isDetecting = currentNoteStr != null
    val confidence = state.recentNotes.lastOrNull()?.confidence ?: 0.0

    val glowScale by animateFloatAsState(
        targetValue = if (isDetecting) 1.1f + (confidence.toFloat() * 0.2f) else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "glow_scale"
    )

    // Glassmorphism background effect
    val glassBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    )

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(glassBrush)
                .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP HEADER: Instrument & Recording State
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Live Session",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Text(
                        text = instrument,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .scale(recordingPulse)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REC",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        // CENTER: Tuner Gauge & Note Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                // Top section of the card: Tuner Gauge
                TunerGauge(
                    centsDeviation = state.centsDeviation,
                    currentNote = currentNoteStr,
                    modifier = Modifier.height(180.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Central Note Display with Glow and Confidence Ring
                Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    if (isDetecting) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .scale(glowScale)
                                .drawBehind {
                                    drawCircle(
                                        color = primaryColor.copy(alpha = 0.2f),
                                        radius = size.minDimension / 2
                                    )
                                }
                        )
                    }

                    CircularProgressIndicator(
                        progress = confidence.toFloat(),
                        modifier = Modifier.size(160.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 6.dp,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )

                    AnimatedContent(
                        targetState = currentNoteStr ?: "--",
                        transitionSpec = {
                            (scaleIn(tween(300, easing = FastOutSlowInEasing)) + fadeIn(tween(200)))
                                .togetherWith(scaleOut(tween(200)) + fadeOut(tween(150)))
                        },
                        label = "note_transition"
                    ) { noteText ->
                        Text(
                            text = noteText,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 72.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // Frequency display placeholder (we don't have raw Hz right now, showing a dummy for aesthetics per design)
                if (isDetecting) {
                    Text(
                        text = "440 Hz", 
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CHORD & SCALE CARDS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HarmonyLessonCard(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "CHORD",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AnimatedContent(targetState = state.detectedChord?.name ?: "--", label = "chord") { chord ->
                        Text(
                            text = chord,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            
            HarmonyLessonCard(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SCALE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AnimatedContent(targetState = state.potentialScales.firstOrNull()?.name ?: "--", label = "scale") { scale ->
                        Text(
                            text = scale,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // WAVEFORM
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            LiveWaveform(
                waveformFlow = waveformFlow,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DYNAMIC INSTRUMENT RENDERER
        Box(modifier = Modifier.weight(0.8f).fillMaxWidth()) {
            InstrumentRenderer(
                instrument = instrument,
                currentNote = currentNoteStr?.takeWhile { !it.isDigit() },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // STOP SESSION FAB
        FloatingActionButton(
            onClick = onStopListening,
            modifier = Modifier
                .size(72.dp),
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp,
                pressedElevation = 2.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop Session",
                modifier = Modifier.size(36.dp)
            )
        }

        }

        // Overlay the debug UI on top (only renders if BuildConfig.DEBUG == true)
        DebugOverlay(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp),
            rmsLevel = rms,
            pitchHz = pitch,
            confidence = conf,
            detectedNote = lastDetectedNote
        )
    }
}
