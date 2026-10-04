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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FolderEntity
import com.example.data.model.QrCodeEntity
import com.example.ui.AnalyticsSummary
import com.example.ui.components.FolderBrowserSection
import com.example.ui.components.QrCodeView

@Composable
fun HomeScreen(
    qrCodes: List<QrCodeEntity>,
    allQrCodes: List<QrCodeEntity>,
    folders: List<FolderEntity>,
    selectedFolderId: Long?,
    selectedFolderCategory: String,
    analytics: AnalyticsSummary,
    searchQuery: String,
    selectedFilter: String,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onSelectFolderId: (Long?) -> Unit,
    onSelectFolderCategory: (String) -> Unit,
    onCreateFolderClick: () -> Unit,
    onEditFolderClick: (FolderEntity) -> Unit,
    onDeleteFolderClick: (FolderEntity) -> Unit,
    onMoveQrToFolderClick: (QrCodeEntity) -> Unit,
    onCreateQrClick: () -> Unit,
    onBatchClick: () -> Unit,
    onTemplatesClick: () -> Unit,
    onEditDynamicDestination: (QrCodeEntity) -> Unit,
    onExportQr: (QrCodeEntity) -> Unit,
    onSimulateScan: (QrCodeEntity) -> Unit,
    onToggleActive: (QrCodeEntity) -> Unit,
    onDeleteQr: (QrCodeEntity) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero banner & Metrics Summary
            item {
                EnterpriseHeroBanner(
                    analytics = analytics,
                    onCreateClick = onCreateQrClick,
                    onBatchClick = onBatchClick,
                    onTemplatesClick = onTemplatesClick
                )
            }

            // Folder Management & Categorization Section
            item {
                FolderBrowserSection(
                    folders = folders,
                    allQrCodes = allQrCodes,
                    selectedFolderId = selectedFolderId,
                    selectedCategory = selectedFolderCategory,
                    onSelectFolderId = onSelectFolderId,
                    onSelectCategory = onSelectFolderCategory,
                    onCreateFolderClick = onCreateFolderClick,
                    onEditFolderClick = onEditFolderClick,
                    onDeleteFolderClick = onDeleteFolderClick
                )
            }

            // Search & Filter Bar
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input"),
                        placeholder = { Text("Rechercher un QR code, tag, auteur...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { onFilterChange("ALL") },
                            label = { Text("Tous (${qrCodes.size})") }
                        )
                        FilterChip(
                            selected = selectedFilter == "DYNAMIC",
                            onClick = { onFilterChange("DYNAMIC") },
                            label = { Text("⚡ Dynamiques") }
                        )
                        FilterChip(
                            selected = selectedFilter == "STATIC",
                            onClick = { onFilterChange("STATIC") },
                            label = { Text("Statiques") }
                        )
                    }
                }
            }

            // QR Codes List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Codes QR de l'Entreprise",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${qrCodes.size} code(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (qrCodes.isEmpty()) {
                item {
                    EmptyQrState(
                        searchQuery = searchQuery,
                        onCreateClick = onCreateQrClick
                    )
                }
            } else {
                items(qrCodes, key = { it.id }) { qr ->
                    QrCardItem(
                        qr = qr,
                        onEditDynamicDestination = { onEditDynamicDestination(qr) },
                        onExportQr = { onExportQr(qr) },
                        onSimulateScan = { onSimulateScan(qr) },
                        onToggleActive = { onToggleActive(qr) },
                        onDeleteQr = { onDeleteQr(qr) },
                        onMoveToFolder = { onMoveQrToFolderClick(qr) }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onCreateQrClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_create_qr"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Créer un QR code")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Nouveau QR", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EnterpriseHeroBanner(
    analytics: AnalyticsSummary,
    onCreateClick: () -> Unit,
    onBatchClick: () -> Unit,
    onTemplatesClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "SUITE D'ENTREPRISE PRO",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "QR Studio Pro",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                    Text(
                        text = "QR codes dynamiques, logos, vectoriel haute résolution et analytics des scans.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Analytics Stats Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Total Scans",
                    value = "${analytics.totalScans}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "QR Dynamiques",
                    value = "${analytics.dynamicQrCount}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Scans 24h",
                    value = "${analytics.scansToday}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCreateClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Créer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onBatchClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.25f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("hero_batch_button")
                ) {
                    Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lot CSV", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onTemplatesClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Modèles", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = Color.White.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun QrCardItem(
    qr: QrCodeEntity,
    onEditDynamicDestination: () -> Unit,
    onExportQr: () -> Unit,
    onSimulateScan: () -> Unit,
    onToggleActive: () -> Unit,
    onDeleteQr: () -> Unit,
    onMoveToFolder: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("qr_card_${qr.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // QR Thumbnail preview
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onExportQr() }
                ) {
                    QrCodeView(
                        qr = qr,
                        modifier = Modifier.fillMaxSize(),
                        resolution = 400,
                        includeFrame = false
                    )
                }

                // Info Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Type / Dynamic badge
                        if (qr.isDynamic) {
                            Surface(
                                color = if (qr.isActive) Color(0xFFEFF6FF) else Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                if (qr.isActive) Color(0xFF2563EB) else Color(0xFFD97706),
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (qr.isActive) "⚡ DYNAMIQUE" else "⏸ EN PAUSE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (qr.isActive) Color(0xFF1E40AF) else Color(0xFFB45309)
                                    )
                                }
                            }
                        } else {
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "STATIQUE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Scans badge
                        Surface(
                            color = Color(0xFFECFDF5),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${qr.totalScans} scans",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = qr.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val targetDisplay = if (qr.isDynamic) qr.destinationUrl.ifBlank { qr.rawContent } else qr.rawContent
                    Text(
                        text = targetDisplay,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = qr.folderName.ifBlank { "Général" },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569)
                                )
                            }
                        }

                        Text(
                            text = "• Par ${qr.createdByUser}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Export Button
                Button(
                    onClick = onExportQr,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Exporter SVG", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // If dynamic: Edit destination button
                if (qr.isDynamic) {
                    Button(
                        onClick = onEditDynamicDestination,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEF3C7),
                            contentColor = Color(0xFFB45309)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Modifier Lien", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Simulate Scan button
                IconButton(
                    onClick = onSimulateScan,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = "Simuler un scan",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // More Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Déplacer dans un dossier...") },
                            leadingIcon = { Icon(Icons.Default.DriveFileMove, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMenu = false
                                onMoveToFolder()
                            }
                        )
                        if (qr.isDynamic) {
                            DropdownMenuItem(
                                text = { Text(if (qr.isActive) "Mettre en pause" else "Réactiver") },
                                onClick = {
                                    showMenu = false
                                    onToggleActive()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Supprimer ce QR code", color = Color.Red) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMenu = false
                                onDeleteQr()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyQrState(searchQuery: String, onCreateClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.QrCode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (searchQuery.isNotBlank()) "Aucun QR code trouvé pour « $searchQuery »" else "Aucun QR code créé",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Créez votre premier QR code dynamique haute résolution pour vos supports imprimés.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onCreateClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Générer un QR Code")
            }
        }
    }
}
