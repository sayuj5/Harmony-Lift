package com.harmonylift.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.harmonylift.app.download.ModelDownloadState

@Composable
fun ModelDownloadScreen(
    state: ModelDownloadState,
    onStartDownload: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (state is ModelDownloadState.Failed) Icons.Default.Warning else Icons.Default.Download,
            contentDescription = "Download Model",
            modifier = Modifier.size(80.dp),
            tint = if (state is ModelDownloadState.Failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "AI Model Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Harmony-Lift needs an AI model to power the Theory Tutor and Practice Coach.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        when (state) {
            is ModelDownloadState.Idle, is ModelDownloadState.Failed -> {
                if (state is ModelDownloadState.Failed) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                Text(
                    text = "Download size: ~600 MB",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = onStartDownload,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state is ModelDownloadState.Failed) "Retry Download" else "Download Now")
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onCancel) {
                    Text("Not right now")
                }
            }
            is ModelDownloadState.Pending -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Connecting to server...")
            }
            is ModelDownloadState.Downloading -> {
                LinearProgressIndicator(
                    progress = state.progress / 100f,
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val downloadedMb = "%.1f".format(state.bytesDownloaded / 1048576f)
                    val totalMb = if (state.totalBytes > 0) "%.1f".format(state.totalBytes / 1048576f) else "?"
                    Text("$downloadedMb / $totalMb MB")
                    Text("${"%.1f".format(state.speedMbps)} Mbps")
                }
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onCancel) {
                    Text("Cancel Download", color = MaterialTheme.colorScheme.error)
                }
            }
            is ModelDownloadState.Verifying -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(state.message)
            }
            is ModelDownloadState.Ready -> {
                Text("Download Complete!", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
