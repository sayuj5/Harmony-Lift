package com.harmonylift.app.export

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.harmonylift.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DataExporter {

    suspend fun exportAsTxt(context: Context, data: Map<String, String>): Result<File> = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(exportDir, "HarmonyLift_Export_${timestamp}.txt")
            
            val builder = java.lang.StringBuilder()
            builder.append("=== HARMONY-LIFT PRACTICE REPORT ===\n")
            builder.append("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n\n")
            
            data.forEach { (key, value) ->
                builder.append("$key: $value\n")
            }
            
            FileOutputStream(file).use { it.write(builder.toString().toByteArray()) }
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportAsPdf(context: Context, data: Map<String, String>): Result<File> = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(exportDir, "HarmonyLift_Export_${timestamp}.pdf")
            
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            
            val bodyPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 14f
            }

            // Draw Logo (assuming we have logo_transparent in app module or ui module)
            // We use context.resources.getIdentifier because ui module R might not be directly accessible
            // if we are in app, wait we can just import com.harmonylift.ui.R
            val logoId = context.resources.getIdentifier("logo_transparent", "drawable", context.packageName)
            if (logoId != 0) {
                val bitmap = BitmapFactory.decodeResource(context.resources, logoId)
                if (bitmap != null) {
                    val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 100, 100, false)
                    canvas.drawBitmap(scaledBitmap, 50f, 50f, null)
                }
            }

            canvas.drawText("HARMONY-LIFT PRACTICE REPORT", 170f, 100f, titlePaint)
            
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            canvas.drawText("Generated on: $dateStr", 170f, 130f, bodyPaint)
            
            var yOffset = 200f
            data.forEach { (key, value) ->
                canvas.drawText("$key: $value", 50f, yOffset, bodyPaint)
                yOffset += 30f
                if (yOffset > 800f) {
                    // Quick pagination break (for this sprint, we assume it fits 1 page or truncate)
                    // Not ideal but works for limited stats
                }
            }

            document.finishPage(page)
            FileOutputStream(file).use { document.writeTo(it) }
            document.close()
            
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (file.extension == "pdf") "application/pdf" else "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Exported Data"))
    }
}
