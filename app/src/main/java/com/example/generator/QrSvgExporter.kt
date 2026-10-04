package com.example.generator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.EyeShape
import com.example.data.model.FrameStyle
import com.example.data.model.LogoPreset
import com.example.data.model.ModuleShape
import com.example.data.model.QrCodeEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object QrSvgExporter {

    fun generateSvg(qr: QrCodeEntity): String {
        val hasLogo = qr.logoPreset != LogoPreset.NONE.name || !qr.customLogoUri.isNullOrBlank()
        val textToEncode = if (qr.isDynamic) qr.rawContent else qr.rawContent
        val matrixResult = QrMatrixGenerator.generateMatrix(textToEncode, hasLogo = hasLogo)

        val hasFrameBanner = qr.frameStyle != FrameStyle.NONE.name
        val baseSize = 1000f
        val bannerHeight = if (hasFrameBanner) 160f else 0f
        val totalHeight = baseSize + bannerHeight

        val matrixSize = matrixResult.size
        val padding = baseSize * 0.06f
        val drawableArea = baseSize - (padding * 2)
        val moduleSize = drawableArea / matrixSize

        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $baseSize $totalHeight" width="100%" height="100%">
  <defs>
""")

        val fgColor = qr.fgColorHex
        val eyeColor = qr.eyeColorHex
        val eyeInnerColor = qr.eyeInnerColorHex
        val bgColor = qr.bgColorHex
        val frameColor = qr.frameColorHex

        val hasGradient = !qr.gradientEndHex.isNullOrBlank()
        if (hasGradient) {
            sb.append("""    <linearGradient id="qrGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="$fgColor" />
      <stop offset="100%" stop-color="${qr.gradientEndHex}" />
    </linearGradient>
""")
        }

        sb.append("""  </defs>
  <!-- Background -->
  <rect x="0" y="0" width="$baseSize" height="$totalHeight" fill="$bgColor" />
""")

        val fillRef = if (hasGradient) "url(#qrGrad)" else fgColor

        val moduleShape = try {
            ModuleShape.valueOf(qr.moduleShape)
        } catch (_: Exception) {
            ModuleShape.ROUNDED
        }

        val eyeShape = try {
            EyeShape.valueOf(qr.eyeShape)
        } catch (_: Exception) {
            EyeShape.ROUNDED
        }

        // 1. Draw Eyes / Finder patterns in SVG
        for (region in matrixResult.finderRegions) {
            val startX = padding + region.startX * moduleSize
            val startY = padding + region.startY * moduleSize
            val patSize = region.size * moduleSize

            when (eyeShape) {
                EyeShape.SQUARE -> {
                    sb.append("""  <rect x="$startX" y="$startY" width="$patSize" height="$patSize" fill="$eyeColor" />
  <rect x="${startX + moduleSize}" y="${startY + moduleSize}" width="${patSize - 2 * moduleSize}" height="${patSize - 2 * moduleSize}" fill="$bgColor" />
  <rect x="${startX + 2 * moduleSize}" y="${startY + 2 * moduleSize}" width="${patSize - 4 * moduleSize}" height="${patSize - 4 * moduleSize}" fill="$eyeInnerColor" />
""")
                }
                EyeShape.ROUNDED -> {
                    val rOuter = patSize * 0.28f
                    val rInner = patSize * 0.16f
                    sb.append("""  <rect x="$startX" y="$startY" width="$patSize" height="$patSize" rx="$rOuter" fill="$eyeColor" />
  <rect x="${startX + moduleSize}" y="${startY + moduleSize}" width="${patSize - 2 * moduleSize}" height="${patSize - 2 * moduleSize}" rx="${rOuter * 0.8f}" fill="$bgColor" />
  <rect x="${startX + 2 * moduleSize}" y="${startY + 2 * moduleSize}" width="${patSize - 4 * moduleSize}" height="${patSize - 4 * moduleSize}" rx="$rInner" fill="$eyeInnerColor" />
""")
                }
                EyeShape.CIRCLE -> {
                    val cx = startX + patSize / 2f
                    val cy = startY + patSize / 2f
                    sb.append("""  <circle cx="$cx" cy="$cy" r="${patSize / 2f}" fill="$eyeColor" />
  <circle cx="$cx" cy="$cy" r="${patSize / 2f - moduleSize}" fill="$bgColor" />
  <circle cx="$cx" cy="$cy" r="${patSize / 2f - 2 * moduleSize}" fill="$eyeInnerColor" />
""")
                }
            }
        }

        // 2. Draw Data Modules in SVG
        sb.append("  <!-- Data Modules -->\n  <g fill=\"$fillRef\">\n")
        for (y in 0 until matrixSize) {
            for (x in 0 until matrixSize) {
                if (QrMatrixGenerator.isFinderPattern(x, y, matrixResult.finderRegions)) continue
                if (hasLogo && QrMatrixGenerator.isInLogoArea(x, y, matrixResult.logoReservedArea)) continue

                if (matrixResult.matrix.get(x, y)) {
                    val left = padding + (x * moduleSize)
                    val top = padding + (y * moduleSize)

                    when (moduleShape) {
                        ModuleShape.SQUARE -> {
                            sb.append("    <rect x=\"$left\" y=\"$top\" width=\"$moduleSize\" height=\"$moduleSize\" />\n")
                        }
                        ModuleShape.DOTS -> {
                            val cx = left + moduleSize / 2f
                            val cy = top + moduleSize / 2f
                            val r = (moduleSize / 2f) * 0.92f
                            sb.append("    <circle cx=\"$cx\" cy=\"$cy\" r=\"$r\" />\n")
                        }
                        ModuleShape.ROUNDED -> {
                            val r = moduleSize * 0.35f
                            sb.append("    <rect x=\"$left\" y=\"$top\" width=\"$moduleSize\" height=\"$moduleSize\" rx=\"$r\" />\n")
                        }
                        ModuleShape.SQUIRCLE -> {
                            val r = moduleSize * 0.48f
                            sb.append("    <rect x=\"$left\" y=\"$top\" width=\"$moduleSize\" height=\"$moduleSize\" rx=\"$r\" />\n")
                        }
                        ModuleShape.DIAMOND -> {
                            val cx = left + moduleSize / 2f
                            val cy = top + moduleSize / 2f
                            val p1 = "$cx,$top"
                            val p2 = "${left + moduleSize},$cy"
                            val p3 = "$cx,${top + moduleSize}"
                            val p4 = "$left,$cy"
                            sb.append("    <polygon points=\"$p1 $p2 $p3 $p4\" />\n")
                        }
                    }
                }
            }
        }
        sb.append("  </g>\n")

        // 3. Center Logo badge in SVG
        if (hasLogo && matrixResult.logoReservedArea != null) {
            val centerArea = matrixResult.logoReservedArea
            val centerLeft = padding + centerArea.startX * moduleSize - 4
            val centerTop = padding + centerArea.startY * moduleSize - 4
            val bWidth = (centerArea.endX - centerArea.startX + 1) * moduleSize + 8
            val bHeight = (centerArea.endY - centerArea.startY + 1) * moduleSize + 8

            sb.append("""  <!-- Central Logo Badge -->
  <rect x="$centerLeft" y="$centerTop" width="$bWidth" height="$bHeight" rx="16" fill="#FFFFFF" stroke="$eyeColor" stroke-width="4" />
""")
            // Preset vector badge icon in SVG
            val cx = centerLeft + bWidth / 2f
            val cy = centerTop + bHeight / 2f
            val r = bWidth * 0.28f

            when (qr.logoPreset) {
                LogoPreset.STAR.name -> {
                    sb.append("  <polygon points=\"")
                    for (i in 0 until 10) {
                        val rad = Math.toRadians((i * 36 - 90).toDouble())
                        val curR = if (i % 2 == 0) r else r * 0.45f
                        val x = (cx + curR * Math.cos(rad)).toFloat()
                        val y = (cy + curR * Math.sin(rad)).toFloat()
                        sb.append("$x,$y ")
                    }
                    sb.append("\" fill=\"$eyeColor\" />\n")
                }
                LogoPreset.STORE.name -> {
                    sb.append("""  <polygon points="${cx - r},${cy - r * 0.3f} ${cx + r},${cy - r * 0.3f} ${cx + r * 0.7f},${cy - r} ${cx - r * 0.7f},${cy - r}" fill="$eyeColor" />
  <rect x="${cx - r * 0.8f}" y="${cy - r * 0.2f}" width="${r * 1.6f}" height="${r * 1.2f}" fill="none" stroke="$eyeColor" stroke-width="4" />
  <rect x="${cx - r * 0.3f}" y="${cy + r * 0.2f}" width="${r * 0.6f}" height="${r * 0.8f}" fill="$eyeColor" />
""")
                }
                else -> {
                    sb.append("""  <circle cx="$cx" cy="$cy" r="$r" fill="none" stroke="$eyeColor" stroke-width="4" />
  <ellipse cx="$cx" cy="$cy" rx="${r * 0.5f}" ry="$r" fill="none" stroke="$eyeColor" stroke-width="3" />
  <line x1="${cx - r}" y1="$cy" x2="${cx + r}" y2="$cy" stroke="$eyeColor" stroke-width="3" />
""")
                }
            }
        }

        // 4. Frame Callout Banner in SVG
        if (hasFrameBanner) {
            val bannerY = baseSize + 14f
            val bannerW = baseSize - (padding * 2)
            val bannerH = bannerHeight - 28f
            val text = qr.frameText.ifBlank { "SCANNEZ-MOI" }
            sb.append("""  <!-- Frame Callout Banner -->
  <rect x="$padding" y="$bannerY" width="$bannerW" height="$bannerH" rx="20" fill="$frameColor" />
  <text x="${baseSize / 2f}" y="${bannerY + bannerH * 0.62f}" fill="#FFFFFF" font-size="52" font-family="sans-serif" font-weight="bold" text-anchor="middle">$text</text>
""")
        }

        sb.append("</svg>")
        return sb.toString()
    }

    /**
     * Generates a high resolution Print-Ready PDF file with crop marks,
     * CMYK / Pantone guidelines, and professional instructions.
     */
    fun generatePrintReadyPdf(
        context: Context,
        qr: QrCodeEntity,
        outputFile: File
    ) {
        val pdfDocument = PdfDocument()

        // A4 page dimensions at 72 points per inch: 595 x 842 points
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Draw Page Background
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, 595f, 842f, paint)

        // 2. Draw Crop Marks & Registration lines for printer
        paint.color = Color.parseColor("#94A3B8")
        paint.strokeWidth = 0.75f
        val margin = 36f

        // Top-left marks
        canvas.drawLine(margin - 15f, margin, margin, margin, paint)
        canvas.drawLine(margin, margin - 15f, margin, margin, paint)
        // Top-right marks
        canvas.drawLine(595f - margin, margin, 595f - margin + 15f, margin, paint)
        canvas.drawLine(595f - margin, margin - 15f, 595f - margin, margin, paint)
        // Bottom-left marks
        canvas.drawLine(margin - 15f, 842f - margin, margin, 842f - margin, paint)
        canvas.drawLine(margin, 842f - margin, margin, 842f - margin + 15f, paint)
        // Bottom-right marks
        canvas.drawLine(595f - margin, 842f - margin, 595f - margin + 15f, 842f - margin, paint)
        canvas.drawLine(595f - margin, 842f - margin, 595f - margin, 842f - margin + 15f, paint)

        // 3. Header / Print Ticket
        paint.color = Color.parseColor("#0F172A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 18f
        canvas.drawText("QR STUDIO PRO — FICHE D'IMPRESSION HAUTE DÉFINITION", margin, 70f, paint)

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 10f
        paint.color = Color.parseColor("#64748B")
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date())
        canvas.drawText("Projet : ${qr.title} | Type : ${if (qr.isDynamic) "QR Dynamique Modifiable" else "QR Statique"} | Date : $dateStr", margin, 88f, paint)

        // Divider
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawLine(margin, 98f, 595f - margin, 98f, paint)

        // 4. Render High-Resolution QR in Center (360 x 360 points)
        val qrBitmap = QrBitmapRenderer.renderBitmap(context, qr, sizePx = 1080, includeFrame = true)
        val qrDestRect = RectF(117f, 130f, 477f, 490f + if (qr.frameStyle != FrameStyle.NONE.name) 58f else 0f)
        canvas.drawBitmap(qrBitmap, null, qrDestRect, Paint(Paint.FILTER_BITMAP_FLAG))

        // 5. Technical Specifications Box
        val boxTop = qrDestRect.bottom + 30f
        val specBox = RectF(margin, boxTop, 595f - margin, boxTop + 140f)
        paint.color = Color.parseColor("#F8FAFC")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(specBox, 8f, 8f, paint)

        paint.color = Color.parseColor("#CBD5E1")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(specBox, 8f, 8f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#1E293B")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("SPÉCIFICATIONS D'IMPRESSION (PRINT READY)", margin + 14f, boxTop + 24f, paint)

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 9.5f
        paint.color = Color.parseColor("#334155")

        val targetUrl = if (qr.isDynamic) qr.destinationUrl.ifBlank { qr.rawContent } else qr.rawContent
        val lines = listOf(
            "• Résolution recommandée : 300 à 600 DPI vectoriel pour affiches et packaging.",
            "• Contraste : Respecter un contraste minimum de 80% entre premier plan (${qr.fgColorHex}) et fond (${qr.bgColorHex}).",
            "• Correction d'erreur : Niveau H (High 30%) — Garantit la lecture même si le QR est altéré ou courbé.",
            "• Cible de redirection : $targetUrl",
            "• Statut dynamique : ${if (qr.isDynamic) "MODIFIABLE SANS RÉIMPRESSION via QR Studio Pro" else "Statique"}"
        )
        var lineY = boxTop + 44f
        for (line in lines) {
            canvas.drawText(line, margin + 14f, lineY, paint)
            lineY += 16f
        }

        // 6. Footer
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 8.5f
        canvas.drawText("Généré par QR Studio Pro — Conforme aux normes ISO/IEC 18004 pour codes à barres 2D.", margin, 810f, paint)

        pdfDocument.finishPage(page)

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
    }
}
