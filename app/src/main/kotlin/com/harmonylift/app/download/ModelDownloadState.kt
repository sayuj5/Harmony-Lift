package com.harmonylift.app.download

import java.io.File

sealed class ModelDownloadState {
    object Idle : ModelDownloadState()
    object Pending : ModelDownloadState()
    data class Downloading(
        val progress: Int,
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val speedMbps: Float
    ) : ModelDownloadState()
    data class Verifying(val message: String) : ModelDownloadState()
    data class Ready(val file: File) : ModelDownloadState()
    data class Failed(val error: String) : ModelDownloadState()
}
