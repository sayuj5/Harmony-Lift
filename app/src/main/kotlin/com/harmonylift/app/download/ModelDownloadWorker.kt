package com.harmonylift.app.download

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

private const val TAG = "HarmonyLiftDebug"

class ModelDownloadWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val modelUrl = inputData.getString("url") ?: return@withContext Result.failure()
        val expectedHash = inputData.getString("hash")
        val fileName = modelUrl.substringAfterLast("/")

        val destDir = File(context.filesDir, "models").also { it.mkdirs() }
        
        val stat = android.os.StatFs(destDir.absolutePath)
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
        if (availableBytes < 1024L * 1024 * 1024) { // Require 1GB
            ModelDownloadManager.postState(ModelDownloadState.Failed("Insufficient storage space. Need at least 1GB."))
            return@withContext Result.failure()
        }

        val destFile = File(destDir, fileName)
        val tempFile = File(destDir, "$fileName.tmp")

        try {
            setForeground(createForegroundInfo(0))
            ModelDownloadManager.postState(ModelDownloadState.Pending)

            val url = URL(modelUrl)
            val connection = url.openConnection() as HttpURLConnection
            
            // Support resume
            var downloaded = 0L
            if (tempFile.exists()) {
                downloaded = tempFile.length()
                connection.setRequestProperty("Range", "bytes=$downloaded-")
            }
            
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                ModelDownloadManager.postState(ModelDownloadState.Failed("Server returned HTTP $responseCode"))
                return@withContext Result.failure()
            }

            val contentLength = connection.contentLengthCompat()
            val totalBytes = if (contentLength != -1L) downloaded + contentLength else -1L

            connection.inputStream.use { input ->
                val append = responseCode == HttpURLConnection.HTTP_PARTIAL
                val outputStream = if (append) {
                    java.io.FileOutputStream(tempFile, true)
                } else {
                    java.io.FileOutputStream(tempFile, false)
                }
                outputStream.use { output ->
                    val buffer = ByteArray(128 * 1024)
                    var read: Int
                    var lastUpdate = System.currentTimeMillis()
                    var bytesSinceLastUpdate = 0L

                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        bytesSinceLastUpdate += read

                        val now = System.currentTimeMillis()
                        if (now - lastUpdate > 500) {
                            val durationSeconds = (now - lastUpdate) / 1000f
                            val speedMbps = (bytesSinceLastUpdate * 8f) / (1024 * 1024) / durationSeconds
                            val progress = if (totalBytes > 0) ((downloaded * 100) / totalBytes).toInt() else 0
                            
                            ModelDownloadManager.postState(
                                ModelDownloadState.Downloading(
                                    progress = progress,
                                    bytesDownloaded = downloaded,
                                    totalBytes = totalBytes,
                                    speedMbps = speedMbps
                                )
                            )
                            setForeground(createForegroundInfo(progress))
                            
                            lastUpdate = now
                            bytesSinceLastUpdate = 0
                        }
                    }
                }
            }

            ModelDownloadManager.postState(ModelDownloadState.Verifying("Verifying file integrity..."))

            if (expectedHash != null) {
                if (!verifyHash(tempFile, expectedHash)) {
                    tempFile.delete()
                    ModelDownloadManager.postState(ModelDownloadState.Failed("Checksum verification failed."))
                    return@withContext Result.failure()
                }
            }

            if (!verifyGgufHeader(tempFile)) {
                tempFile.delete()
                ModelDownloadManager.postState(ModelDownloadState.Failed("Invalid GGUF header."))
                return@withContext Result.failure()
            }

            // Rename temp file to final file
            if (destFile.exists()) destFile.delete()
            tempFile.renameTo(destFile)

            Log.d(TAG, "[ModelWorker] Download and verification complete. Path=${destFile.absolutePath}")
            ModelDownloadManager.postState(ModelDownloadState.Ready(destFile))
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "[ModelWorker] Exception: ${e.message}", e)
            ModelDownloadManager.postState(ModelDownloadState.Failed("Error: ${e.localizedMessage}"))
            Result.retry()
        }
    }

    private fun HttpURLConnection.contentLengthCompat(): Long {
        return try {
            getHeaderField("Content-Length")?.toLong() ?: -1L
        } catch (e: Exception) {
            -1L
        }
    }

    private fun verifyHash(file: File, expectedHash: String): Boolean {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(256 * 1024)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            hash.equals(expectedHash, ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }

    private fun verifyGgufHeader(file: File): Boolean {
        return try {
            val magic = ByteArray(4)
            file.inputStream().use { it.read(magic) }
            magic[0] == 0x47.toByte() && magic[1] == 0x47.toByte() && magic[2] == 0x55.toByte() && magic[3] == 0x46.toByte()
        } catch (e: Exception) {
            false
        }
    }

    private fun createForegroundInfo(progress: Int): androidx.work.ForegroundInfo {
        val id = "model_download_channel"
        val title = "Downloading AI Model"
        
        val intent = androidx.work.WorkManager.getInstance(applicationContext).createCancelPendingIntent(getId())
            
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(id, title, android.app.NotificationManager.IMPORTANCE_LOW)
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = androidx.core.app.NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(if (progress >= 0) "Progress: $progress%" else "Downloading...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, "Cancel", intent)
            .build()
            
        return androidx.work.ForegroundInfo(1, notification)
    }
}
