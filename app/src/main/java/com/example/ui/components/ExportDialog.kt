package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QrCodeEntity
import com.example.generator.ExportHelper
import com.example.generator.QrSvgExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    qr: QrCodeEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedFormat by remember { mutableStateOf("SVG") } // "SVG", "PDF", "PNG_2048", "PNG_4096"
    var showSvgCode by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Export Haute Résolution",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = qr.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Format Selector Chips
            Text(
                text = "Format d'exportation pour l'impression :",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFormat == "SVG",
                    onClick = { selectedFormat = "SVG" },
                    label = { Text("Vectoriel SVG") },
                    leadingIcon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = selectedFormat == "PDF",
                    onClick = { selectedFormat = "PDF" },
                    label = { Text("Fiche PDF Pro") },
                    leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFormat == "PNG_2048",
                    onClick = { selectedFormat = "PNG_2048" },
                    label = { Text("PNG HD (2048px)") }
                )
                FilterChip(
                    selected = selectedFormat == "PNG_4096",
                    onClick = { selectedFormat = "PNG_4096" },
                    label = { Text("PNG Ultra-HD (4096px - 300 DPI)") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Format Description Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (selectedFormat) {
                        "SVG" -> Color(0xFFEFF6FF)
                        "PDF" -> Color(0xFFFEF2F2)
                        else -> Color(0xFFF0FDF4)
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    when (selectedFormat) {
                        "SVG" -> {
                            Text(
                                text = "SVG — Format Vectoriel Infini",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Recommandé pour les imprimeurs professionnels, graphistes, découpe vinyle, enseignes et packaging. Agrandissable sans aucune perte de netteté.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1E3A8A)
                            )
                        }
                        "PDF" -> {
                            Text(
                                text = "PDF — Fiche d'Impression Print-Ready",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Document A4 complet avec repères de coupe, profil technique 300 DPI, instructions de pose et métadonnées du QR code.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                        else -> {
                            Text(
                                text = "PNG — Format Matriciel Ultra-Haute Définition",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Fichier raster 300 DPI idéal pour insertion directe dans Word, Photoshop, InDesign ou publication web.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF14532D)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Button(
                onClick = {
                    when (selectedFormat) {
                        "SVG" -> ExportHelper.exportAndShareSvg(context, qr)
                        "PDF" -> ExportHelper.exportAndSharePdf(context, qr)
                        "PNG_2048" -> ExportHelper.exportAndSharePng(context, qr, 2048)
                        "PNG_4096" -> ExportHelper.exportAndSharePng(context, qr, 4096)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_share_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (selectedFormat) {
                        "SVG" -> "Exporter & Partager le SVG"
                        "PDF" -> "Générer & Partager la Fiche PDF"
                        else -> "Exporter le fichier PNG"
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedFormat == "SVG") {
                OutlinedButton(
                    onClick = { showSvgCode = !showSvgCode },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (showSvgCode) "Masquer le code SVG" else "Afficher le code XML SVG")
                }
            }

            if (showSvgCode && selectedFormat == "SVG") {
                Spacer(modifier = Modifier.height(12.dp))
                val svgCode = remember(qr) { QrSvgExporter.generateSvg(qr) }
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Code source SVG vectoriel",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(svgCode))
                                    Toast.makeText(context, "Code SVG copié dans le presse-papier !", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copier",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = svgCode,
                            color = Color(0xFF38BDF8),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
