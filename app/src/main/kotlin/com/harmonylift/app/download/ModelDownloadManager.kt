package com.harmonylift.app.download

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

object ModelDownloadManager {
    private val _state = MutableStateFlow<ModelDownloadState>(ModelDownloadState.Idle)
    val state: StateFlow<ModelDownloadState> = _state.asStateFlow()

    private const val DEFAULT_MODEL_URL = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q3_K_M.gguf"

    fun postState(newState: ModelDownloadState) {
        _state.value = newState
    }

    fun checkExistingModel(context: Context) {
        val destDir = File(context.filesDir, "models")
        if (!destDir.exists()) destDir.mkdirs()
        val modelFileName = "Llama-3.2-1B-Instruct-Q3_K_M.gguf"
        val destFile = File(destDir, modelFileName)
        
        if (destFile.exists() && destFile.length() > 0 && destFile.canRead()) {
            _state.value = ModelDownloadState.Ready(destFile)
            return
        }
        
        // Try to copy from assets if it exists
        try {
            val assets = context.assets.list("models")
            if (assets?.contains(modelFileName) == true) {
                _state.value = ModelDownloadState.Downloading(progress = 0, bytesDownloaded = 0, totalBytes = -1, speedMbps = 0f)
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        context.assets.open("models/$modelFileName").use { input ->
                            destFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        if (destFile.exists() && destFile.length() > 0) {
                            postState(ModelDownloadState.Ready(destFile))
                        } else {
                            postState(ModelDownloadState.Idle)
                        }
                    } catch (e: Exception) {
                        postState(ModelDownloadState.Idle)
                    }
                }
                return
            }
        } catch (e: Exception) {
            // Asset listing failed, continue to idle
        }

        _state.value = ModelDownloadState.Idle
    }

    fun startDownload(context: Context, url: String = DEFAULT_MODEL_URL, hash: String? = null) {
        if (_state.value is ModelDownloadState.Downloading || _state.value is ModelDownloadState.Ready) {
            return
        }
        
        val inputData = Data.Builder()
            .putString("url", url)
            .putString("hash", hash)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag("MODEL_DOWNLOAD")
            .build()

        _state.value = ModelDownloadState.Pending
        WorkManager.getInstance(context).enqueueUniqueWork(
            "model_download_work",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelDownload(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("MODEL_DOWNLOAD")
        _state.value = ModelDownloadState.Idle
    }
}
