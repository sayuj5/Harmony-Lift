package com.harmonylift.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.harmonylift.theory.practice.PracticeMode
import com.harmonylift.ui.components.HarmonyLessonCard
import com.harmonylift.ui.components.HarmonyPrimaryButton

@Composable
fun PracticeCoachScreen(
    totalXp: Int,
    onStartSession: (PracticeMode) -> Unit,
    onViewAnalytics: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame"
    )

    val currentLevel = (totalXp / 500) + 1
    val currentLevelXpStart = (currentLevel - 1) * 500
    val nextLevelXp = currentLevel * 500
    val xpInLevel = totalXp - currentLevelXpStart
    val progress = xpInLevel.toFloat() / 500f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Practice Coach",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black
            )
        }

        // Gamification Dashboard Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Level $currentLevel Musician", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = "Streak", tint = Color(0xFFFF9800), modifier = Modifier.size(24.dp).scale(flameScale))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("5 Day Streak", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("%,d XP".format(totalXp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        Text("%,d XP".format(nextLevelXp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    )
                }
            }
        }

        item {
            Text("Daily Objectives", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        item {
            HarmonyLessonCard(progress = 0.0f) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Note Recognition Warmup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("+50 XP", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    com.harmonylift.ui.components.HarmonyOutlinedButton(
                        text = "Start",
                        onClick = { onStartSession(PracticeMode.NOTE_RECOGNITION) },
                        modifier = Modifier.height(40.dp)
                    )
                }
            }
        }

        item {
            Text("Interactive Modules", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(PracticeMode.values()) { mode ->
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(140.dp)
                            .clickable { onStartSession(mode) }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Timeline, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(mode.title, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            com.harmonylift.ui.components.HarmonyOutlinedButton(
                text = "View Performance Analytics",
                onClick = onViewAnalytics,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
        }

        item {
            HarmonyPrimaryButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
        }
    }
}

@Composable
fun ProgressAnalyticsScreen(
    totalSessions: Int,
    overallAccuracy: Float,
    totalPracticeTimeMs: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accuracyInt = (overallAccuracy * 100).toInt()
    val minutes = totalPracticeTimeMs / 60000

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Performance Analytics",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                com.harmonylift.ui.components.HarmonyProgressCircular(
                    progress = overallAccuracy,
                    modifier = Modifier.padding(32.dp)
                )
                Text(
                    text = "$accuracyInt% Overall Accuracy",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Total Sessions: $totalSessions", style = MaterialTheme.typography.bodyLarge)
                Text("Total Practice: $minutes mins", style = MaterialTheme.typography.bodyLarge)
                Text("Pitch Stability: Excellent", style = MaterialTheme.typography.bodyLarge)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        HarmonyPrimaryButton(
            text = "Back",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        )
    }
}

@Composable
fun SessionSummaryScreen(
    durationMs: Long,
    notesDetected: Int,
    pitchStability: Float,
    onReturnHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    // This is the standalone SessionSummaryScreen (e.g. from Live Listening)
    val minutes = durationMs / 60000
    val seconds = (durationMs % 60000) / 1000
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
    val stabilityFormatted = "${pitchStability.toInt()}%"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Session Complete!",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary),
            contentAlignment = Alignment.Center
        ) {
            val grade = if (pitchStability > 90f) "A+" else if (pitchStability > 80f) "A" else if (pitchStability > 70f) "B" else "C"
            Text(grade, style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onTertiary, fontWeight = FontWeight.Black)
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(24.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Time Practiced", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(timeFormatted, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Notes Detected", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$notesDetected", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pitch Stability", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stabilityFormatted, style = MaterialTheme.typography.titleLarge, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        HarmonyPrimaryButton(
            text = "Return to Dashboard",
            onClick = onReturnHome,
            modifier = Modifier.fillMaxWidth().height(64.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
