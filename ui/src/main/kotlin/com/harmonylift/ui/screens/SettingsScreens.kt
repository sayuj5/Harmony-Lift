package com.harmonylift.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigatePrivacyPolicy: () -> Unit,
    onExportData: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val themePreferences = remember { com.harmonylift.ui.theme.ThemePreferences(context) }
    val scope = rememberCoroutineScope()
    
    val themeMode by themePreferences.themeMode.collectAsState(initial = com.harmonylift.ui.theme.ThemeMode.SYSTEM)
    val isDarkMode = themeMode == com.harmonylift.ui.theme.ThemeMode.DARK

    var notificationsEnabled by remember { mutableStateOf(true) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SETTINGS.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            SettingsToggleCard(
                title = "Dark Mode",
                subtitle = "Toggle app theme",
                icon = Icons.Default.DarkMode,
                checked = isDarkMode,
                onCheckedChange = { isDark ->
                    scope.launch {
                        themePreferences.saveThemeMode(if (isDark) com.harmonylift.ui.theme.ThemeMode.DARK else com.harmonylift.ui.theme.ThemeMode.LIGHT)
                    }
                }
            )
            
            SettingsActionCard(
                title = "Instrument",
                subtitle = "Piano",
                icon = Icons.Default.MusicNote,
                onClick = { /* Change instrument */ }
            )

            SettingsToggleCard(
                title = "Notifications",
                subtitle = "Practice reminders",
                icon = Icons.Default.Notifications,
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "AI Model",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            SettingsActionCard(
                title = "Model Status",
                subtitle = "Llama-3.2-1B-Instruct (Ready)",
                icon = Icons.Default.SmartToy,
                onClick = { /* Status */ }
            )
            
            SettingsActionCard(
                title = "Verify Integrity",
                subtitle = "Check for corruption",
                icon = Icons.Default.VerifiedUser,
                onClick = { /* Verify */ }
            )
            
            SettingsActionCard(
                title = "Delete Model",
                subtitle = "Free up 600 MB",
                icon = Icons.Default.Delete,
                onClick = { /* Delete */ }
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Data & Privacy",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            SettingsActionCard(
                title = "Export Data",
                subtitle = "Save your progress",
                icon = Icons.Default.Download,
                onClick = { showExportDialog = true }
            )
            
            SettingsActionCard(
                title = "Privacy Policy",
                subtitle = "How we protect your data",
                icon = Icons.Default.PrivacyTip,
                onClick = { onNavigatePrivacyPolicy() }
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            

            SettingsActionCard(
                title = "Version",
                subtitle = "2.0.0 (Production)",
                icon = Icons.Default.Info,
                onClick = { /* Nothing */ }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Format", fontWeight = FontWeight.Bold) },
            text = { Text("Choose a format to export your practice data.") },
            confirmButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    onExportData("PDF")
                }) {
                    Text("PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    onExportData("TXT")
                }) {
                    Text("TXT")
                }
            }
        )
    }
}

@Composable
fun SettingsToggleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    }
}

@Composable
fun SettingsActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
