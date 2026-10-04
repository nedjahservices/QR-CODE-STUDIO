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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FolderCategory
import com.example.data.model.FolderEntity
import com.example.generator.QrBitmapRenderer

@Composable
fun CreateEditFolderDialog(
    initialFolder: FolderEntity? = null,
    onDismiss: () -> Unit,
    onSave: (FolderEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialFolder?.name ?: "") }
    var selectedCategory by remember {
        mutableStateOf(initialFolder?.category ?: FolderCategory.CAMPAIGN.name)
    }
    var selectedColorHex by remember { mutableStateOf(initialFolder?.colorHex ?: "#2563EB") }
    var description by remember { mutableStateOf(initialFolder?.description ?: "") }

    val colorPalette = listOf(
        "#2563EB", // Bleu Roi
        "#4F46E5", // Indigo
        "#7C3AED", // Violet
        "#D97706", // Ambre
        "#059669", // Émeraude
        "#DC2626", // Rubis
        "#0891B2", // Cyan
        "#0F172A"  // Ardoise
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Color(QrBitmapRenderer.safeParseColor(selectedColorHex, android.graphics.Color.BLUE)).copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color(QrBitmapRenderer.safeParseColor(selectedColorHex, android.graphics.Color.BLUE)),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = if (initialFolder == null) "Nouveau Dossier" else "Modifier le Dossier",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du dossier *") },
                    placeholder = { Text("Ex: Campagne Printemps 2026") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("folder_name_input"),
                    singleLine = true
                )

                // Category Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Catégorisation :",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            FolderCategory.CAMPAIGN to "Campagne",
                            FolderCategory.CLIENT to "Client",
                            FolderCategory.DEPARTMENT to "Département"
                        ).forEach { (cat, label) ->
                            FilterChip(
                                selected = selectedCategory == cat.name,
                                onClick = { selectedCategory = cat.name },
                                label = { Text(label, fontSize = 11.sp) },
                                leadingIcon = {
                                    val icon = when (cat) {
                                        FolderCategory.CAMPAIGN -> Icons.Default.Campaign
                                        FolderCategory.CLIENT -> Icons.Default.Business
                                        FolderCategory.DEPARTMENT -> Icons.Default.CorporateFare
                                        else -> Icons.Default.Folder
                                    }
                                    Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            )
                        }
                    }
                }

                // Color Swatches
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Couleur du dossier :",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(colorPalette) { hex ->
                            val color = Color(QrBitmapRenderer.safeParseColor(hex, android.graphics.Color.BLUE))
                            val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColorHex = hex }
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optionnel)") },
                    placeholder = { Text("Ex: Affiches métros, brochures et vitrine...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val folder = FolderEntity(
                            id = initialFolder?.id ?: 0L,
                            name = name.trim(),
                            category = selectedCategory,
                            colorHex = selectedColorHex,
                            description = description.trim(),
                            createdAt = initialFolder?.createdAt ?: System.currentTimeMillis()
                        )
                        onSave(folder)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_folder_button")
            ) {
                Text(if (initialFolder == null) "Créer" else "Enregistrer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
