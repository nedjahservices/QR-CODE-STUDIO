package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FolderCategory
import com.example.data.model.FolderEntity
import com.example.data.model.QrCodeEntity
import com.example.generator.QrBitmapRenderer

@Composable
fun MoveToFolderDialog(
    qrCode: QrCodeEntity,
    folders: List<FolderEntity>,
    onDismiss: () -> Unit,
    onMoveToFolder: (FolderEntity?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Déplacer dans un dossier", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = "QR code : « ${qrCode.title} »",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option 0: No folder / General
                    item {
                        val isCurrent = qrCode.folderId == null
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMoveToFolder(null) }
                                .testTag("move_to_general_folder")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF64748B).copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Général (Aucun dossier)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Classé dans la racine par défaut", fontSize = 11.sp, color = Color.Gray)
                                }

                                if (isCurrent) {
                                    Icon(Icons.Default.Check, contentDescription = "Actuel", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    // User folders
                    items(folders, key = { it.id }) { folder ->
                        val isCurrent = qrCode.folderId == folder.id
                        val color = Color(QrBitmapRenderer.safeParseColor(folder.colorHex, android.graphics.Color.BLUE))
                        val icon = when (folder.category) {
                            FolderCategory.CAMPAIGN.name -> Icons.Default.Campaign
                            FolderCategory.CLIENT.name -> Icons.Default.Business
                            FolderCategory.DEPARTMENT.name -> Icons.Default.CorporateFare
                            else -> Icons.Default.Folder
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMoveToFolder(folder) }
                                .testTag("move_to_folder_${folder.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(color.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(folder.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Surface(
                                        color = color.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        val catLabel = when (folder.category) {
                                            FolderCategory.CAMPAIGN.name -> "Campagne"
                                            FolderCategory.CLIENT.name -> "Client"
                                            FolderCategory.DEPARTMENT.name -> "Département"
                                            else -> folder.category
                                        }
                                        Text(
                                            text = catLabel,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = color,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                if (isCurrent) {
                                    Icon(Icons.Default.Check, contentDescription = "Actuel", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}
