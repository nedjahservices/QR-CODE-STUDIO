package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QrCodeEntity
import com.example.ui.components.QrCodeView

@Composable
fun DynamicManagerScreen(
    dynamicQrs: List<QrCodeEntity>,
    onEditDestination: (QrCodeEntity) -> Unit,
    onToggleActive: (QrCodeEntity) -> Unit,
    onSimulateScan: (QrCodeEntity) -> Unit,
    onExport: (QrCodeEntity) -> Unit,
    onCreateNewDynamic: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Concept Explanatory Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF2563EB), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White)
                        }
                        Column {
                            Text(
                                text = "Gestionnaire de QR Dynamiques",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1E3A8A)
                            )
                            Text(
                                text = "Modifiables après impression à l'infini",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Même une fois vos QR codes imprimés sur des milliers de flyers, vitrines de magasins ou packagings, vous pouvez changer leur cible de redirection en un clic ou les mettre en pause.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1E40AF)
                    )
                }
            }
        }

        // List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vos Campagnes Dynamiques Imprimées",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${dynamicQrs.size} actives",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (dynamicQrs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Aucun QR dynamique pour l'instant", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(onClick = onCreateNewDynamic) {
                            Text("Créer un QR Dynamique")
                        }
                    }
                }
            }
        } else {
            items(dynamicQrs, key = { it.id }) { qr ->
                DynamicQrCard(
                    qr = qr,
                    onEditDestination = { onEditDestination(qr) },
                    onToggleActive = { onToggleActive(qr) },
                    onSimulateScan = { onSimulateScan(qr) },
                    onExport = { onExport(qr) },
                    onTestUrl = {
                        try {
                            val url = if (qr.destinationUrl.startsWith("http://") || qr.destinationUrl.startsWith("https://")) {
                                qr.destinationUrl
                            } else {
                                "https://${qr.destinationUrl}"
                            }
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (_: Exception) {
                            Toast.makeText(context, "URL invalide", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DynamicQrCard(
    qr: QrCodeEntity,
    onEditDestination: () -> Unit,
    onToggleActive: () -> Unit,
    onSimulateScan: () -> Unit,
    onExport: () -> Unit,
    onTestUrl: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dynamic_qr_card_${qr.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (qr.isActive) Color(0xFF10B981) else Color(0xFFF59E0B),
                                CircleShape
                            )
                    )
                    Text(
                        text = if (qr.isActive) "EN SERVICE (ACTIF)" else "CAMPAGNE EN PAUSE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (qr.isActive) Color(0xFF047857) else Color(0xFFB45309)
                    )
                }

                Switch(
                    checked = qr.isActive,
                    onCheckedChange = { onToggleActive() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body with QR Code and redirection paths
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onExport() }
                ) {
                    QrCodeView(qr = qr, modifier = Modifier.fillMaxSize(), resolution = 350, includeFrame = false)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = qr.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // QR Printed Fixed Short URL
                    Text(
                        text = "Gravé / Imprimé :",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = qr.rawContent,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Destination URL that can change
                    Text(
                        text = "Redirection actuelle en direct :",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = qr.destinationUrl.ifBlank { "Aucune URL configurée" },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom control strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEditDestination,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Changer la Cible", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onTestUrl,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = "Tester")
                }

                OutlinedButton(
                    onClick = onSimulateScan,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Simuler Scan")
                }

                OutlinedButton(
                    onClick = onExport,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Exporter")
                }
            }
        }
    }
}
