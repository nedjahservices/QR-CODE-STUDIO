package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.FolderCategory
import com.example.data.model.FolderEntity
import com.example.data.model.QrCodeEntity
import com.example.generator.QrBitmapRenderer

@Composable
fun FolderBrowserSection(
    folders: List<FolderEntity>,
    allQrCodes: List<QrCodeEntity>,
    selectedFolderId: Long?,
    selectedCategory: String,
    onSelectFolderId: (Long?) -> Unit,
    onSelectCategory: (String) -> Unit,
    onCreateFolderClick: () -> Unit,
    onEditFolderClick: (FolderEntity) -> Unit,
    onDeleteFolderClick: (FolderEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredFolders = remember(folders, selectedCategory) {
        if (selectedCategory == "ALL") {
            folders
        } else {
            folders.filter { it.category == selectedCategory }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Dossiers & Classement",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            OutlinedButton(
                onClick = onCreateFolderClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("create_folder_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nouveau Dossier", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Category Filter Chips (Campagne, Client, Département)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                "ALL" to "Tous les dossiers",
                FolderCategory.CAMPAIGN.name to "Campagnes",
                FolderCategory.CLIENT.name to "Clients",
                FolderCategory.DEPARTMENT.name to "Départements"
            ).forEach { (code, label) ->
                item {
                    FilterChip(
                        selected = selectedCategory == code,
                        onClick = { onSelectCategory(code) },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Horizontal Carousel of Folders
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // "Tous les QR codes" card
            item {
                val isAllSelected = selectedFolderId == null
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAllSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isAllSelected) 3.dp else 1.dp),
                    modifier = Modifier
                        .width(135.dp)
                        .height(115.dp)
                        .clickable { onSelectFolderId(null) }
                        .testTag("folder_all_card")
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color(0xFF64748B).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                        }

                        Column {
                            Text(
                                text = "Tous les codes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "${allQrCodes.size} code(s)",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // User defined folder cards
            items(filteredFolders, key = { it.id }) { folder ->
                val isSelected = selectedFolderId == folder.id
                val qrCount = allQrCodes.count { it.folderId == folder.id }
                val scanCount = allQrCodes.filter { it.folderId == folder.id }.sumOf { it.totalScans }
                val folderColor = Color(QrBitmapRenderer.safeParseColor(folder.colorHex, android.graphics.Color.BLUE))

                val catIcon = when (folder.category) {
                    FolderCategory.CAMPAIGN.name -> Icons.Default.Campaign
                    FolderCategory.CLIENT.name -> Icons.Default.Business
                    FolderCategory.DEPARTMENT.name -> Icons.Default.CorporateFare
                    else -> Icons.Default.Folder
                }

                var showMenu by remember { mutableStateOf(false) }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) {
                        androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    },
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
                    modifier = Modifier
                        .width(160.dp)
                        .height(115.dp)
                        .clickable {
                            if (isSelected) onSelectFolderId(null) else onSelectFolderId(folder.id)
                        }
                        .testTag("folder_card_${folder.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top row: icon + category badge + options menu
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(folderColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(catIcon, contentDescription = null, tint = folderColor, modifier = Modifier.size(15.dp))
                            }

                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Modifier le dossier", fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        onClick = {
                                            showMenu = false
                                            onEditFolderClick(folder)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Supprimer", fontSize = 12.sp, color = Color(0xFFDC2626)) },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp)) },
                                        onClick = {
                                            showMenu = false
                                            onDeleteFolderClick(folder)
                                        }
                                    )
                                }
                            }
                        }

                        // Bottom: Name + QR count + Scans
                        Column {
                            Text(
                                text = folder.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$qrCount code(s)",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "$scanCount scans",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = folderColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active folder filter banner
        if (selectedFolderId != null) {
            val currentFolder = folders.find { it.id == selectedFolderId }
            if (currentFolder != null) {
                val folderColor = Color(QrBitmapRenderer.safeParseColor(currentFolder.colorHex, android.graphics.Color.BLUE))
                Surface(
                    color = folderColor.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = folderColor, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Dossier actif : « ${currentFolder.name} »",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = folderColor
                            )
                        }

                        IconButton(
                            onClick = { onSelectFolderId(null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Réinitialiser", tint = folderColor, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
