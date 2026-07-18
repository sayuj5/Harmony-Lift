package com.harmonylift.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarmonyLessonCard(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 48.dp)
        .semantics { contentDescription = "Lesson Card" }
    
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
    
    val shape = MaterialTheme.shapes.medium
    
    val contentBlock = @Composable {
        Column {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
            if (progress != null) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = shape,
            colors = cardColors,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
        ) {
            contentBlock()
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = shape,
            colors = cardColors,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            contentBlock()
        }
    }
}
