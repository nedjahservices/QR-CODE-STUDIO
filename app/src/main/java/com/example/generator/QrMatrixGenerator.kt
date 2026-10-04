package com.example.generator

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

data class QrMatrixResult(
    val matrix: BitMatrix,
    val size: Int,
    val finderRegions: List<FinderRegion>,
    val logoReservedArea: LogoArea?
)

data class FinderRegion(
    val startX: Int,
    val startY: Int,
    val size: Int = 7
)

data class LogoArea(
    val startX: Int,
    val startY: Int,
    val endX: Int,
    val endY: Int
)

object QrMatrixGenerator {

    fun generateMatrix(
        content: String,
        hasLogo: Boolean = false
    ): QrMatrixResult {
        val hints = HashMap<EncodeHintType, Any>().apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            // Use High error correction (30%) if logo is present or by default for robust print scannability
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            put(EncodeHintType.MARGIN, 1)
        }

        val writer = QRCodeWriter()
        val textToEncode = if (content.isBlank()) "https://qrcraft.biz" else content
        val bitMatrix = writer.encode(textToEncode, BarcodeFormat.QR_CODE, 0, 0, hints)
        val matrixSize = bitMatrix.width

        val finderRegions = listOf(
            FinderRegion(0, 0, 7),
            FinderRegion(matrixSize - 7, 0, 7),
            FinderRegion(0, matrixSize - 7, 7)
        )

        // Logo reserved area in the center: ~22% to 26% of QR size
        val logoReservedArea = if (hasLogo && matrixSize >= 21) {
            val logoModuleRadius = (matrixSize * 0.12).toInt().coerceAtLeast(2)
            val center = matrixSize / 2
            LogoArea(
                startX = center - logoModuleRadius,
                startY = center - logoModuleRadius,
                endX = center + logoModuleRadius,
                endY = center + logoModuleRadius
            )
        } else null

        return QrMatrixResult(
            matrix = bitMatrix,
            size = matrixSize,
            finderRegions = finderRegions,
            logoReservedArea = logoReservedArea
        )
    }

    fun isFinderPattern(x: Int, y: Int, finderRegions: List<FinderRegion>): Boolean {
        for (f in finderRegions) {
            if (x >= f.startX && x < f.startX + f.size &&
                y >= f.startY && y < f.startY + f.size
            ) {
                return true
            }
        }
        return false
    }

    fun isFinderOuterRing(x: Int, y: Int, finderRegions: List<FinderRegion>): Boolean {
        for (f in finderRegions) {
            if (x >= f.startX && x < f.startX + f.size &&
                y >= f.startY && y < f.startY + f.size
            ) {
                val relX = x - f.startX
                val relY = y - f.startY
                // 7x7 outer border: ring is at border (0 or 6) or inner pupil is (2..4)
                return (relX == 0 || relX == 6 || relY == 0 || relY == 6)
            }
        }
        return false
    }

    fun isFinderInnerPupil(x: Int, y: Int, finderRegions: List<FinderRegion>): Boolean {
        for (f in finderRegions) {
            if (x >= f.startX && x < f.startX + f.size &&
                y >= f.startY && y < f.startY + f.size
            ) {
                val relX = x - f.startX
                val relY = y - f.startY
                return (relX in 2..4 && relY in 2..4)
            }
        }
        return false
    }

    fun isInLogoArea(x: Int, y: Int, logoArea: LogoArea?): Boolean {
        if (logoArea == null) return false
        return x in logoArea.startX..logoArea.endX && y in logoArea.startY..logoArea.endY
    }
}
