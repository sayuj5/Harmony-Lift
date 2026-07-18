package com.harmonylift.theory.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.harmonylift.theory.domain.model.Chord
import com.harmonylift.theory.domain.model.Interval
import com.harmonylift.theory.domain.model.Note
import com.harmonylift.theory.domain.model.Scale

/**
 * Stateless UI state for the TheoryTutor screen.
 * Populated by TheoryTutorViewModel (in :app) and passed down here.
 */
data class TheoryScreenState(
    val recentNotes: List<Note> = emptyList(),
    val detectedChord: Chord? = null,
    val detectedInterval: Interval? = null,
    val potentialScales: List<Scale> = emptyList(),
    val centsDeviation: Int = 0,
    val aiResponse: String = "",
    val isAiStreaming: Boolean = false,
    val aiError: String? = null,
    val lastUpdateTimestamp: Long = System.currentTimeMillis()
)

@Composable
fun TheoryTutorScreen(
    state: TheoryScreenState,
    onClearHistory: () -> Unit,
    onAskTutor: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Theory Engine Activity",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Recent Notes Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Recent Notes", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.recentNotes.forEach { note ->
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = note.toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Detected Chord
        state.detectedChord?.let { chord ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Detected Chord", style = MaterialTheme.typography.titleMedium, color = Color(0xFF2E7D32))
                    Text(
                        text = chord.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                }
            }
        }

        // Detected Interval
        state.detectedInterval?.let { interval ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Last Interval", style = MaterialTheme.typography.titleMedium, color = Color(0xFF1565C0))
                    Text(
                        text = interval.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0D47A1)
                    )
                }
            }
        }

        // Potential Scales
        if (state.potentialScales.isNotEmpty()) {
            Text("Potential Scales", style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.potentialScales.take(5)) { scale ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = scale.name,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onClearHistory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear History")
        }
    }
}
