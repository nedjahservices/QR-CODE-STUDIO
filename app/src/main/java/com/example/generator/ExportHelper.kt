package com.example.generator

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.example.data.model.QrCodeEntity
import java.io.File
import java.io.FileOutputStream

object ExportHelper {

    fun exportAndShareSvg(context: Context, qr: QrCodeEntity): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeTitle = qr.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)
        val file = File(exportDir, "QR_${safeTitle}_vector.svg")

        val svgContent = QrSvgExporter.generateSvg(qr)
        file.writeText(svgContent)

        shareFile(context, file, "image/svg+xml", "Exporter le QR code vectoriel SVG")
        return file
    }

    fun exportAndSharePng(context: Context, qr: QrCodeEntity, resolution: Int = 2048): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeTitle = qr.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)
        val file = File(exportDir, "QR_${safeTitle}_${resolution}px.png")

        val bitmap = QrBitmapRenderer.renderBitmap(context, qr, sizePx = resolution, includeFrame = true)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        shareFile(context, file, "image/png", "Exporter le QR code PNG Haute Définition")
        return file
    }

    fun exportAndSharePdf(context: Context, qr: QrCodeEntity): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeTitle = qr.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)
        val file = File(exportDir, "Fiche_Impression_QR_${safeTitle}.pdf")

        QrSvgExporter.generatePrintReadyPdf(context, qr, file)
        shareFile(context, file, "application/pdf", "Exporter la Fiche Impression PDF")
        return file
    }

    private fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            putExtra(Intent.EXTRA_TEXT, "Fichier d'impression haute définition généré avec QR Studio Pro.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
