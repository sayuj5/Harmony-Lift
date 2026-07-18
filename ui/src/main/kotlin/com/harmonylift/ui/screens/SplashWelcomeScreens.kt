package com.harmonylift.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.harmonylift.ui.components.HarmonyPrimaryButton
import com.harmonylift.ui.components.HarmonyTonalButton
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "alphaAnim"
    )
    
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scaleAnim"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000)
        onSplashComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alphaAnim)
                .scale(scaleAnim)
        ) {
            Image(
                painter = painterResource(id = com.harmonylift.ui.R.drawable.logo),
                contentDescription = "Harmony-Lift Logo",
                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(24.dp))
            )
        }
    }
}

@Composable
fun WelcomeScreen(
    onStartLearning: () -> Unit,
    onAlreadyPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(32.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = com.harmonylift.ui.R.drawable.logo),
                contentDescription = "Harmony-Lift Logo",
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "EVERYTHING\nYOU NEED.",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = androidx.compose.ui.unit.TextUnit(36f, androidx.compose.ui.unit.TextUnitType.Sp)
            ),
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No fluff. Just the power tools to master your instrument.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        HarmonyPrimaryButton(
            text = "Start Learning",
            onClick = onStartLearning,
            modifier = Modifier.fillMaxWidth().height(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        HarmonyTonalButton(
            text = "I Already Play",
            onClick = onAlreadyPlay,
            modifier = Modifier.fillMaxWidth().height(64.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
