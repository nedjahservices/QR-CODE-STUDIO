package com.example.generator

import com.example.data.model.QrCodeEntity
import com.example.data.model.QrType
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID

data class BatchQrItem(
    val rowNumber: Int,
    val title: String,
    val content: String,
    val type: String = QrType.URL.name,
    val isDynamic: Boolean = true,
    val tags: String = "Import CSV",
    val frameText: String = "SCANNEZ-MOI",
    val fgColor: String? = null,
    val bgColor: String? = null,
    val isValid: Boolean = true,
    val errorReason: String? = null
)

object CsvQrParser {

    val SAMPLE_RESTAURANT_CSV = """title;destination_url;is_dynamic;tags;frame_text
Table 01 - Terrasse;https://mon-resto.fr/menu?table=1;true;Restaurant, Terrasse;TABLE 01
Table 02 - Terrasse;https://mon-resto.fr/menu?table=2;true;Restaurant, Terrasse;TABLE 02
Table 03 - Salle Intérieure;https://mon-resto.fr/menu?table=3;true;Restaurant, Salle;TABLE 03
Table 04 - Salle Intérieure;https://mon-resto.fr/menu?table=4;true;Restaurant, Salle;TABLE 04
Table 05 - VIP Lounge;https://mon-resto.fr/menu?table=5;true;Restaurant, VIP;SALON VIP
Comptoir Caisse & Avis;https://g.page/r/mon-resto/review;true;Avis, Comptoir;VOTRE AVIS
""".trimIndent()

    val SAMPLE_PRODUCTS_CSV = """title,destination_url,is_dynamic,tags,frame_text
Packaging Café Bio 250g,https://torrefacteur.fr/tracabilite/lot-8491,true,Packaging,SCAN PROVENANCE
Bouteille Huile d'Olive AOP,https://domaine-provence.fr/guide-degustation,true,Packaging,DEGUSTATION
Coffret Cadeau Découverte,https://boutique.com/vip-club,true,Marketing,CADEAU VIP
Catalogue Nouveautés Hiver,https://boutique.com/catalogue-pdf,true,Print,CATALOGUE 2026
""".trimIndent()

    fun parse(inputStream: InputStream): List<BatchQrItem> {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val text = reader.readText()
        return parse(text)
    }

    fun parse(csvContent: String): List<BatchQrItem> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        // Detect delimiter: semicolon, comma, or tab
        val firstLine = lines.first()
        val delimiter = when {
            firstLine.count { it == ';' } >= firstLine.count { it == ',' } && firstLine.contains(';') -> ';'
            firstLine.contains('\t') -> '\t'
            else -> ','
        }

        val parsedLines = lines.map { parseCsvLine(it, delimiter) }
        val headerTokens = parsedLines.first().map { it.trim().lowercase() }

        var titleIdx = -1
        var contentIdx = -1
        var isDynamicIdx = -1
        var tagsIdx = -1
        var frameIdx = -1
        var fgColorIdx = -1
        var bgColorIdx = -1
        var typeIdx = -1

        val hasHeader = headerTokens.any { token ->
            token.contains("title") || token.contains("titre") || token.contains("nom") ||
                    token.contains("url") || token.contains("content") || token.contains("destination") ||
                    token.contains("lien") || token.contains("cible")
        }

        val dataLines: List<Pair<Int, List<String>>>
        if (hasHeader) {
            headerTokens.forEachIndexed { index, token ->
                when {
                    token.contains("title") || token.contains("titre") || token.contains("nom") || token.contains("name") -> titleIdx = index
                    token.contains("url") || token.contains("content") || token.contains("destination") || token.contains("lien") || token.contains("cible") -> contentIdx = index
                    token.contains("dynamic") || token.contains("dynamique") -> isDynamicIdx = index
                    token.contains("tag") || token.contains("categorie") || token.contains("category") -> tagsIdx = index
                    token.contains("frame") || token.contains("cadre") || token.contains("texte") -> frameIdx = index
                    token.contains("fg") || token.contains("color") || token.contains("couleur") -> fgColorIdx = index
                    token.contains("bg") || token.contains("fond") -> bgColorIdx = index
                    token.contains("type") -> typeIdx = index
                }
            }
            dataLines = parsedLines.drop(1).mapIndexed { i, tokens -> Pair(i + 2, tokens) }
        } else {
            // Positional fallback
            titleIdx = 0
            contentIdx = 1
            if (firstLine.contains("http") || firstLine.contains("www") || firstLine.contains("/")) {
                // If first column looks like a URL and second is title
                if (parsedLines.first().size > 1 && (parsedLines.first()[0].contains("http") || parsedLines.first()[0].contains("www"))) {
                    contentIdx = 0
                    titleIdx = 1
                }
            }
            tagsIdx = 2
            frameIdx = 3
            dataLines = parsedLines.mapIndexed { i, tokens -> Pair(i + 1, tokens) }
        }

        // If title and content indices are still undefined, fallback
        if (titleIdx == -1) titleIdx = 0
        if (contentIdx == -1) contentIdx = if (titleIdx == 0) 1 else 0

        val items = mutableListOf<BatchQrItem>()

        for ((rowNum, tokens) in dataLines) {
            val title = tokens.getOrNull(titleIdx)?.trim() ?: ""
            val rawContent = tokens.getOrNull(contentIdx)?.trim() ?: ""

            if (title.isBlank() && rawContent.isBlank()) continue

            if (rawContent.isBlank()) {
                items.add(
                    BatchQrItem(
                        rowNumber = rowNum,
                        title = title.ifBlank { "Ligne $rowNum" },
                        content = "",
                        isValid = false,
                        errorReason = "URL ou contenu manquant"
                    )
                )
                continue
            }

            val dynamicVal = if (isDynamicIdx != -1) {
                val str = tokens.getOrNull(isDynamicIdx)?.trim()?.lowercase() ?: "true"
                str == "true" || str == "1" || str == "oui" || str == "yes" || str == "vrai"
            } else true

            val tagsVal = if (tagsIdx != -1) {
                tokens.getOrNull(tagsIdx)?.trim()?.ifBlank { "Lot CSV" } ?: "Lot CSV"
            } else "Lot CSV"

            val frameVal = if (frameIdx != -1) {
                tokens.getOrNull(frameIdx)?.trim()?.ifBlank { "SCANNEZ-MOI" } ?: "SCANNEZ-MOI"
            } else "SCANNEZ-MOI"

            val fgVal = if (fgColorIdx != -1) tokens.getOrNull(fgColorIdx)?.trim() else null
            val bgVal = if (bgColorIdx != -1) tokens.getOrNull(bgColorIdx)?.trim() else null

            val typeVal = if (typeIdx != -1) {
                val t = tokens.getOrNull(typeIdx)?.trim()?.uppercase() ?: QrType.URL.name
                if (QrType.values().any { it.name == t }) t else QrType.URL.name
            } else QrType.URL.name

            items.add(
                BatchQrItem(
                    rowNumber = rowNum,
                    title = title.ifBlank { "QR Lot #$rowNum" },
                    content = rawContent,
                    type = typeVal,
                    isDynamic = dynamicVal,
                    tags = tagsVal,
                    frameText = frameVal,
                    fgColor = fgVal,
                    bgColor = bgVal,
                    isValid = true
                )
            )
        }

        return items
    }

    private fun parseCsvLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        sb.append('"')
                        i++ // Skip escaped quote
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == delimiter && !inQuotes -> {
                    result.add(sb.toString().trim())
                    sb.setLength(0)
                }
                else -> {
                    sb.append(c)
                }
            }
            i++
        }
        result.add(sb.toString().trim())
        return result
    }

    fun toEntities(
        batchItems: List<BatchQrItem>,
        authorName: String,
        defaultFgColor: String = "#0F172A",
        defaultBgColor: String = "#FFFFFF",
        defaultEyeColor: String = "#2563EB",
        moduleShape: String = "ROUNDED",
        eyeShape: String = "ROUNDED",
        logoPreset: String = "BRIEFCASE",
        frameStyle: String = "SCAN_ME"
    ): List<QrCodeEntity> {
        val now = System.currentTimeMillis()
        return batchItems.filter { it.isValid }.mapIndexed { index, item ->
            val shortCode = "csv-${UUID.randomUUID().toString().take(6)}"
            val encodedRaw = if (item.isDynamic) "https://qr.biz/d/$shortCode" else item.content

            QrCodeEntity(
                id = 0L,
                title = item.title,
                type = item.type,
                isDynamic = item.isDynamic,
                dynamicShortCode = shortCode,
                rawContent = encodedRaw,
                destinationUrl = if (item.isDynamic) item.content else "",
                isActive = true,
                fgColorHex = item.fgColor?.ifBlank { null } ?: defaultFgColor,
                bgColorHex = item.bgColor?.ifBlank { null } ?: defaultBgColor,
                eyeColorHex = defaultEyeColor,
                eyeInnerColorHex = defaultEyeColor,
                moduleShape = moduleShape,
                eyeShape = eyeShape,
                logoPreset = logoPreset,
                frameStyle = frameStyle,
                frameText = item.frameText,
                frameColorHex = defaultEyeColor,
                createdByUser = authorName,
                createdAt = now + index * 50,
                updatedAt = now + index * 50,
                totalScans = 0,
                tags = item.tags
            )
        }
    }
}
