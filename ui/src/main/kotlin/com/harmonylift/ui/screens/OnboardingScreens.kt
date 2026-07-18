package com.harmonylift.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.harmonylift.ui.components.HarmonyPrimaryButton
import com.harmonylift.ui.components.HarmonyTonalButton

@Composable
fun MicrophonePermissionScreen(
    onPermissionGranted: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var permissionState by remember { mutableStateOf("NOT_REQUESTED") } 

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            val activity = context as? android.app.Activity
            val shouldShowRationale = activity?.let { 
                androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(it, android.Manifest.permission.RECORD_AUDIO) 
            } ?: false
            
            permissionState = if (shouldShowRationale) "DENIED" else "PERMANENTLY_DENIED"
        }
    }

    LaunchedEffect(Unit) {
        val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (isGranted) {
            onPermissionGranted()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "WE NEED TO HEAR YOU.",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val rationaleText = when (permissionState) {
            "DENIED" -> "We really need microphone access to analyze your playing. Please grant it to continue."
            "PERMANENTLY_DENIED" -> "Microphone access is permanently denied. You must enable it in Settings."
            else -> "Harmony-Lift analyzes your live playing to provide real-time feedback. We need microphone access."
        }

        Text(
            text = rationaleText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        if (permissionState == "PERMANENTLY_DENIED") {
            HarmonyPrimaryButton(
                text = "Open App Settings",
                onClick = {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(64.dp)
            )
        } else {
            HarmonyPrimaryButton(
                text = if (permissionState == "DENIED") "Try Again" else "Enable Microphone",
                onClick = {
                    permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                },
                modifier = Modifier.fillMaxWidth().height(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        HarmonyTonalButton(
            text = "Skip for Now",
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth().height(64.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun InstrumentSelectionScreen(
    onContinue: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedInstrument by remember { mutableStateOf<String?>(null) }
    val instruments = listOf("Piano", "Guitar", "Bass", "Vocals", "Ukulele", "Violin")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "CHOOSE YOUR WEAPON.",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "What will you be playing today?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(instruments) { instrument ->
                val isSelected = selectedInstrument == instrument
                
                val scale by animateFloatAsState(if (isSelected) 0.95f else 1f, spring(), label = "scaleAnim")
                val bgColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, label = "bgColorAnim")
                val textColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, label = "textColorAnim")
                
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .scale(scale)
                        .clip(RoundedCornerShape(24.dp))
                        .background(bgColor)
                        .clickable { selectedInstrument = instrument }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = instrument,
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        HarmonyPrimaryButton(
            text = "Continue",
            onClick = { selectedInstrument?.let { onContinue(it) } },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            enabled = selectedInstrument != null
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
