package com.example.generator

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.example.data.model.QrCodeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object BatchZipExporter {

    suspend fun createAndShareZip(
        context: Context,
        qrCodes: List<QrCodeEntity>,
        zipNamePrefix: String = "Lot_QR_Codes"
    ): File = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val dateStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.FRANCE).format(Date())
        val zipFile = File(exportDir, "${zipNamePrefix}_$dateStamp.zip")

        FileOutputStream(zipFile).use { fos ->
            ZipOutputStream(fos).use { zos ->
                // 1. Add Summary CSV Manifest
                val manifestContent = buildString {
                    append("ID,Titre,Type,Dynamique,Lien_QR_Grave,URL_Redirection_Cible,Date_Creation\n")
                    qrCodes.forEachIndexed { idx, qr ->
                        val safeTitle = qr.title.replace(",", " ")
                        val dest = (if (qr.isDynamic) qr.destinationUrl else qr.rawContent).replace(",", "%2C")
                        append("${idx + 1},\"$safeTitle\",${qr.type},${qr.isDynamic},\"${qr.rawContent}\",\"$dest\",${qr.createdAt}\n")
                    }
                }
                val manifestEntry = ZipEntry("MANIFESTE_CODES_QR.csv")
                zos.putNextEntry(manifestEntry)
                zos.write(manifestContent.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 2. Add Readme with Printing Recommendations
                val readme = """
======================================================
QR STUDIO PRO — PACK D'IMPRESSION PAR LOT (BATCH)
======================================================
Nombre de codes générés : ${qrCodes.size}
Date de génération      : ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date())}

CONSEILS D'IMPRESSION POUR L'ATELIER GRAPHIQUE :
1. Fichiers SVG : Utilisez les fichiers .svg vectoriels pour vos logiciels
   PAO (Adobe InDesign, Illustrator, Figma, découpe vinyle).
   Ils sont redimensionnables à l'infini sans perte de netteté.
2. Fichiers PNG : Fichiers haute résolution (2048px) prêts pour insertion directe.
3. QR Codes Dynamiques : Les URLs cibles peuvent être modifiées à tout moment
   depuis l'application QR Studio Pro sans devoir réimprimer vos supports !
======================================================
""".trimIndent()
                val readmeEntry = ZipEntry("LISEZMOI_CONSEILS_IMPRESSION.txt")
                zos.putNextEntry(readmeEntry)
                zos.write(readme.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 3. Add each QR code as SVG and high-res PNG
                qrCodes.forEachIndexed { index, qr ->
                    val cleanTitle = qr.title
                        .replace(Regex("[^a-zA-Z0-9_-]"), "_")
                        .take(25)
                        .ifBlank { "QR_${index + 1}" }
                    val baseFileName = "${String.format("%02d", index + 1)}_$cleanTitle"

                    // Write SVG Vector
                    val svgContent = QrSvgExporter.generateSvg(qr)
                    val svgEntry = ZipEntry("vectoriels_svg/$baseFileName.svg")
                    zos.putNextEntry(svgEntry)
                    zos.write(svgContent.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()

                    // Write PNG 2048px
                    val bitmap = QrBitmapRenderer.renderBitmap(context, qr, sizePx = 2048, includeFrame = true)
                    val byteStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 95, byteStream)
                    val pngBytes = byteStream.toByteArray()

                    val pngEntry = ZipEntry("images_png/$baseFileName.png")
                    zos.putNextEntry(pngEntry)
                    zos.write(pngBytes)
                    zos.closeEntry()
                }
            }
        }

        withContext(Dispatchers.Main) {
            shareZipFile(context, zipFile, "Archive ZIP complète des QR codes générés")
        }

        zipFile
    }

    private fun shareZipFile(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            putExtra(Intent.EXTRA_TEXT, "Pack d'impression ZIP généré par QR Studio Pro (${file.length() / 1024} Ko).")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
