package com.harmonylift.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    instrument: String,
    onNavigateToLiveListening: () -> Unit,
    onNavigateToPracticeCoach: () -> Unit,
    onNavigateToAiTutor: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp))
            
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { 20 }) + fadeIn(animationSpec = tween(300))
            ) {
                Column {
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "READY TO PLAY?",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { 20 }, animationSpec = tween(300, delayMillis = 100)) + fadeIn(animationSpec = tween(300, delayMillis = 100))
            ) {
                // Hero Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(32.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "7 Day Streak!",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = instrument.uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        LinearProgressIndicator(
                            progress = 0.65f,
                            modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                            color = MaterialTheme.colorScheme.onPrimary,
                            trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { 20 }, animationSpec = tween(300, delayMillis = 200)) + fadeIn(animationSpec = tween(300, delayMillis = 200))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionCard(
                        title = "Live\nSession",
                        icon = Icons.Default.Mic,
                        onClick = onNavigateToLiveListening,
                        modifier = Modifier.weight(1f),
                        isSecondary = false
                    )
                    ActionCard(
                        title = "Quick\nPractice",
                        icon = Icons.Default.MusicNote,
                        onClick = onNavigateToPracticeCoach,
                        modifier = Modifier.weight(1f),
                        isSecondary = true
                    )
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { 20 }, animationSpec = tween(300, delayMillis = 300)) + fadeIn(animationSpec = tween(300, delayMillis = 300))
            ) {
                ActionCard(
                    title = "AI Music Tutor",
                    subtitle = "Ask theory questions & get feedback",
                    icon = Icons.Default.AutoAwesome,
                    onClick = onNavigateToAiTutor,
                    modifier = Modifier.fillMaxWidth(),
                    isSecondary = true
                )
            }
        }
        
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { 20 }, animationSpec = tween(300, delayMillis = 400)) + fadeIn(animationSpec = tween(300, delayMillis = 400))
            ) {
                ActionCard(
                    title = "Settings",
                    icon = Icons.Default.Settings,
                    onClick = onNavigateToSettings,
                    modifier = Modifier.fillMaxWidth(),
                    isSecondary = true
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSecondary: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )
    
    val backgroundColor = if (isSecondary) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondary
    val contentColor = if (isSecondary) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSecondary
    
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current
            ) { onClick() }
            .padding(24.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}
