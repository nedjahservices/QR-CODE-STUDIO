package com.example.generator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import com.example.data.model.EyeShape
import com.example.data.model.FrameStyle
import com.example.data.model.LogoPreset
import com.example.data.model.ModuleShape
import com.example.data.model.QrCodeEntity
import kotlin.math.min

object QrBitmapRenderer {

    fun renderBitmap(
        context: Context,
        qr: QrCodeEntity,
        sizePx: Int = 1024,
        includeFrame: Boolean = true
    ): Bitmap {
        val hasLogo = qr.logoPreset != LogoPreset.NONE.name || !qr.customLogoUri.isNullOrBlank()
        val textToEncode = if (qr.isDynamic) qr.rawContent else qr.rawContent
        val matrixResult = QrMatrixGenerator.generateMatrix(textToEncode, hasLogo = hasLogo)

        val hasFrameBanner = includeFrame && qr.frameStyle != FrameStyle.NONE.name
        val bannerHeight = if (hasFrameBanner) (sizePx * 0.16f).toInt() else 0
        val totalHeight = sizePx + bannerHeight

        val bitmap = Bitmap.createBitmap(sizePx, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgColor = safeParseColor(qr.bgColorHex, Color.WHITE)
        val fgColor = safeParseColor(qr.fgColorHex, Color.BLACK)
        val eyeColor = safeParseColor(qr.eyeColorHex, fgColor)
        val eyeInnerColor = safeParseColor(qr.eyeInnerColorHex, eyeColor)
        val frameColor = safeParseColor(qr.frameColorHex, eyeColor)

        // 1. Draw Background
        val bgPaint = Paint().apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, sizePx.toFloat(), totalHeight.toFloat(), bgPaint)

        // 2. Setup Dimensions & Quiet Zone
        val matrixSize = matrixResult.size
        val padding = sizePx * 0.06f
        val drawableArea = sizePx - (padding * 2)
        val moduleSize = drawableArea / matrixSize

        // 3. Prepare paints
        val modulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = fgColor
        }
        if (!qr.gradientEndHex.isNullOrBlank()) {
            val gradEnd = safeParseColor(qr.gradientEndHex, fgColor)
            modulePaint.shader = LinearGradient(
                0f, 0f, sizePx.toFloat(), sizePx.toFloat(),
                fgColor, gradEnd, Shader.TileMode.CLAMP
            )
        }

        val eyeOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = eyeColor
        }

        val eyeInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = eyeInnerColor
        }

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

        // 4. Draw Eyes explicitly for clean shapes
        for (region in matrixResult.finderRegions) {
            drawFinderPattern(
                canvas = canvas,
                startX = padding + region.startX * moduleSize,
                startY = padding + region.startY * moduleSize,
                patternSize = region.size * moduleSize,
                moduleSize = moduleSize,
                eyeShape = eyeShape,
                outerColor = eyeColor,
                innerColor = eyeInnerColor,
                bgColor = bgColor
            )
        }

        // 5. Draw normal data modules
        for (y in 0 until matrixSize) {
            for (x in 0 until matrixSize) {
                // Skip finder patterns (drawn separately)
                if (QrMatrixGenerator.isFinderPattern(x, y, matrixResult.finderRegions)) continue

                // Skip logo area
                if (hasLogo && QrMatrixGenerator.isInLogoArea(x, y, matrixResult.logoReservedArea)) continue

                if (matrixResult.matrix.get(x, y)) {
                    val left = padding + (x * moduleSize)
                    val top = padding + (y * moduleSize)
                    val right = left + moduleSize
                    val bottom = top + moduleSize

                    drawModule(
                        canvas = canvas,
                        shape = moduleShape,
                        left = left,
                        top = top,
                        right = right,
                        bottom = bottom,
                        paint = modulePaint
                    )
                }
            }
        }

        // 6. Draw Logo in the center if configured
        if (hasLogo && matrixResult.logoReservedArea != null) {
            val centerArea = matrixResult.logoReservedArea
            val centerLeft = padding + centerArea.startX * moduleSize
            val centerTop = padding + centerArea.startY * moduleSize
            val centerRight = padding + (centerArea.endX + 1) * moduleSize
            val centerBottom = padding + (centerArea.endY + 1) * moduleSize

            val badgeRect = RectF(centerLeft - 4f, centerTop - 4f, centerRight + 4f, centerBottom + 4f)
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = eyeColor
                style = Paint.Style.STROKE
                strokeWidth = min(moduleSize * 0.4f, 6f)
            }

            canvas.drawRoundRect(badgeRect, 18f, 18f, badgePaint)
            canvas.drawRoundRect(badgeRect, 18f, 18f, borderPaint)

            // Draw custom logo if available, or preset icon
            var customBitmap: Bitmap? = null
            if (!qr.customLogoUri.isNullOrBlank()) {
                try {
                    val uri = Uri.parse(qr.customLogoUri)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        customBitmap = BitmapFactory.decodeStream(stream)
                    }
                } catch (_: Exception) {}
            }

            if (customBitmap != null) {
                val pad = 12f
                val destRect = RectF(
                    badgeRect.left + pad,
                    badgeRect.top + pad,
                    badgeRect.right - pad,
                    badgeRect.bottom - pad
                )
                canvas.drawBitmap(customBitmap!!, null, destRect, Paint(Paint.FILTER_BITMAP_FLAG))
            } else {
                drawPresetLogoIcon(
                    canvas = canvas,
                    preset = qr.logoPreset,
                    rect = badgeRect,
                    color = eyeColor
                )
            }
        }

        // 7. Draw Frame Callout Banner if requested
        if (hasFrameBanner) {
            val bannerTop = sizePx.toFloat()
            val bannerRect = RectF(
                padding,
                bannerTop + (bannerHeight * 0.1f),
                sizePx - padding,
                totalHeight - (bannerHeight * 0.15f)
            )

            val frameBannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = frameColor
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(bannerRect, 16f, 16f, frameBannerPaint)

            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = bannerHeight * 0.38f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val displayText = qr.frameText.ifBlank { "SCANNEZ-MOI" }
            val fontMetrics = textPaint.fontMetrics
            val baseline = bannerRect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2
            canvas.drawText(displayText, bannerRect.centerX(), baseline, textPaint)
        }

        return bitmap
    }

    private fun drawModule(
        canvas: Canvas,
        shape: ModuleShape,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        paint: Paint
    ) {
        val width = right - left
        val height = bottom - top
        when (shape) {
            ModuleShape.SQUARE -> {
                canvas.drawRect(left, top, right, bottom, paint)
            }
            ModuleShape.DOTS -> {
                val radius = (min(width, height) / 2f) * 0.92f
                canvas.drawCircle(left + width / 2f, top + height / 2f, radius, paint)
            }
            ModuleShape.ROUNDED -> {
                val r = width * 0.35f
                canvas.drawRoundRect(RectF(left, top, right, bottom), r, r, paint)
            }
            ModuleShape.SQUIRCLE -> {
                val r = width * 0.48f
                canvas.drawRoundRect(RectF(left, top, right, bottom), r, r, paint)
            }
            ModuleShape.DIAMOND -> {
                val path = Path().apply {
                    moveTo(left + width / 2f, top)
                    lineTo(right, top + height / 2f)
                    lineTo(left + width / 2f, bottom)
                    lineTo(left, top + height / 2f)
                    close()
                }
                canvas.drawPath(path, paint)
            }
        }
    }

    private fun drawFinderPattern(
        canvas: Canvas,
        startX: Float,
        startY: Float,
        patternSize: Float,
        moduleSize: Float,
        eyeShape: EyeShape,
        outerColor: Int,
        innerColor: Int,
        bgColor: Int
    ) {
        val outerRect = RectF(startX, startY, startX + patternSize, startY + patternSize)
        val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = outerColor
            style = Paint.Style.FILL
        }
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = innerColor
            style = Paint.Style.FILL
        }

        val middleRect = RectF(
            startX + moduleSize,
            startY + moduleSize,
            startX + patternSize - moduleSize,
            startY + patternSize - moduleSize
        )

        val pupilRect = RectF(
            startX + 2 * moduleSize,
            startY + 2 * moduleSize,
            startX + patternSize - 2 * moduleSize,
            startY + patternSize - 2 * moduleSize
        )

        when (eyeShape) {
            EyeShape.SQUARE -> {
                canvas.drawRect(outerRect, outerPaint)
                canvas.drawRect(middleRect, bgPaint)
                canvas.drawRect(pupilRect, innerPaint)
            }
            EyeShape.ROUNDED -> {
                val outerR = patternSize * 0.28f
                val midR = patternSize * 0.22f
                val pupilR = patternSize * 0.16f
                canvas.drawRoundRect(outerRect, outerR, outerR, outerPaint)
                canvas.drawRoundRect(middleRect, midR, midR, bgPaint)
                canvas.drawRoundRect(pupilRect, pupilR, pupilR, innerPaint)
            }
            EyeShape.CIRCLE -> {
                canvas.drawOval(outerRect, outerPaint)
                canvas.drawOval(middleRect, bgPaint)
                canvas.drawOval(pupilRect, innerPaint)
            }
        }
    }

    private fun drawPresetLogoIcon(
        canvas: Canvas,
        preset: String,
        rect: RectF,
        color: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = rect.width() * 0.08f
            strokeCap = Paint.Cap.ROUND
        }

        val cx = rect.centerX()
        val cy = rect.centerY()
        val r = rect.width() * 0.28f

        when (preset) {
            LogoPreset.STAR.name -> {
                val path = Path()
                val innerR = r * 0.45f
                for (i in 0 until 10) {
                    val rad = Math.toRadians((i * 36 - 90).toDouble())
                    val currentR = if (i % 2 == 0) r else innerR
                    val x = (cx + currentR * Math.cos(rad)).toFloat()
                    val y = (cy + currentR * Math.sin(rad)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                canvas.drawPath(path, paint)
            }
            LogoPreset.STORE.name -> {
                val roof = Path().apply {
                    moveTo(cx - r, cy - r * 0.3f)
                    lineTo(cx + r, cy - r * 0.3f)
                    lineTo(cx + r * 0.7f, cy - r)
                    lineTo(cx - r * 0.7f, cy - r)
                    close()
                }
                canvas.drawPath(roof, paint)
                canvas.drawRect(cx - r * 0.8f, cy - r * 0.2f, cx + r * 0.8f, cy + r, strokePaint)
                canvas.drawRect(cx - r * 0.3f, cy + r * 0.2f, cx + r * 0.3f, cy + r, paint)
            }
            LogoPreset.RESTAURANT.name -> {
                // Fork & Knife silhouette
                canvas.drawLine(cx - r * 0.4f, cy - r, cx - r * 0.4f, cy + r, strokePaint)
                canvas.drawLine(cx + r * 0.4f, cy - r, cx + r * 0.4f, cy + r, strokePaint)
                canvas.drawCircle(cx - r * 0.4f, cy - r * 0.6f, r * 0.3f, paint)
            }
            LogoPreset.BRIEFCASE.name -> {
                val body = RectF(cx - r, cy - r * 0.5f, cx + r, cy + r)
                canvas.drawRoundRect(body, 8f, 8f, strokePaint)
                val handle = RectF(cx - r * 0.4f, cy - r, cx + r * 0.4f, cy - r * 0.5f)
                canvas.drawRoundRect(handle, 4f, 4f, strokePaint)
            }
            else -> {
                // Globe / Corporate emblem
                canvas.drawCircle(cx, cy, r, strokePaint)
                canvas.drawOval(RectF(cx - r * 0.5f, cy - r, cx + r * 0.5f, cy + r), strokePaint)
                canvas.drawLine(cx - r, cy, cx + r, cy, strokePaint)
            }
        }
    }

    fun safeParseColor(colorStr: String?, defaultColor: Int): Int {
        if (colorStr.isNullOrBlank()) return defaultColor
        return try {
            Color.parseColor(colorStr)
        } catch (_: Exception) {
            defaultColor
        }
    }
}
