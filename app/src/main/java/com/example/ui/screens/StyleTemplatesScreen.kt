package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PredefinedStyleTemplates
import com.example.data.model.QrCodeEntity
import com.example.data.model.QrStyleTemplate
import com.example.data.model.StyleCategory
import com.example.generator.QrBitmapRenderer
import com.example.ui.components.QrCodeView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleTemplatesScreen(
    onBack: () -> Unit,
    onSelectStyleToCreate: (QrCodeEntity) -> Unit,
    onExportTemplateDirect: (QrCodeEntity) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<StyleCategory>(StyleCategory.ALL) }
    var previewContentInput by remember { mutableStateOf("https://votre-entreprise.com/decouverte") }

    val filteredTemplates = remember(selectedCategory) {
        if (selectedCategory == StyleCategory.ALL) {
            PredefinedStyleTemplates.templates
        } else {
            PredefinedStyleTemplates.templates.filter { it.category == selectedCategory }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Styles Graphiques Professionnels", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Chartes visuelles prêtes pour l'impression", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Intro Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }

                            Column {
                                Text(
                                    text = "Styles Prédéfinis d'Entreprise",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Minimaliste, Brand-Focused ou Tech-Inspired",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Sélectionnez un univers graphique conçu selon les règles typographiques et de contraste ISO/IEC pour vos supports d'impression.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Interactive Playground: customize live sample URL
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Aperçu en direct avec votre lien :",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = previewContentInput,
                            onValueChange = { previewContentInput = it },
                            placeholder = { Text("https://mon-entreprise.com") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("style_preview_content_input")
                        )
                    }
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(StyleCategory.values()) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.displayName, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Count header
            item {
                Text(
                    text = "${filteredTemplates.size} style(s) disponible(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }

            // Template Cards List
            items(filteredTemplates, key = { it.id }) { template ->
                val previewEntity = remember(template, previewContentInput) {
                    template.toPreviewEntity(sampleContent = previewContentInput.ifBlank { "https://mon-entreprise.com" })
                }

                StyleTemplateCard(
                    template = template,
                    previewEntity = previewEntity,
                    onSelectToCreate = { onSelectStyleToCreate(previewEntity) },
                    onExportDirect = { onExportTemplateDirect(previewEntity) }
                )
            }
        }
    }
}

@Composable
fun StyleTemplateCard(
    template: QrStyleTemplate,
    previewEntity: QrCodeEntity,
    onSelectToCreate: () -> Unit,
    onExportDirect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("style_template_${template.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top info: category pill and color swatches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(QrBitmapRenderer.safeParseColor(template.accentBadgeColor, android.graphics.Color.BLUE)).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = template.category.displayName.uppercase(),
                        color = Color(QrBitmapRenderer.safeParseColor(template.accentBadgeColor, android.graphics.Color.BLUE)),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Swatches row
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    val fgColor = Color(QrBitmapRenderer.safeParseColor(template.fgColorHex, android.graphics.Color.BLACK))
                    val eyeColor = Color(QrBitmapRenderer.safeParseColor(template.eyeColorHex, android.graphics.Color.BLACK))
                    val bgColor = Color(QrBitmapRenderer.safeParseColor(template.bgColorHex, android.graphics.Color.WHITE))

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(fgColor)
                            .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(eyeColor)
                            .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                    )
                    if (template.gradientEndHex != null) {
                        val gradColor = Color(QrBitmapRenderer.safeParseColor(template.gradientEndHex, android.graphics.Color.BLUE))
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(gradColor)
                                .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(bgColor)
                            .border(1.dp, Color(0xFF94A3B8), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body: QR preview + description
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // QR View
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onExportDirect() }
                ) {
                    QrCodeView(
                        qr = previewEntity,
                        modifier = Modifier.fillMaxSize(),
                        resolution = 450,
                        includeFrame = true
                    )
                }

                // Details column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Style tags
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Points : ${template.moduleShape}",
                                fontSize = 9.sp,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        if (template.frameText.isNotBlank()) {
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = template.frameText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSelectToCreate,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Utiliser ce Style", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onExportDirect,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Exporter")
                }
            }
        }
    }
}
