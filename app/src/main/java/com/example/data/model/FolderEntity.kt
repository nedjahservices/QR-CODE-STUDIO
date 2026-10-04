package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FolderCategory(val displayName: String, val iconName: String) {
    ALL("Tous les dossiers", "folder"),
    CAMPAIGN("Campagnes Marketing", "campaign"),
    CLIENT("Clients & Comptes", "business"),
    DEPARTMENT("Départements & Pôles", "corporate_fare")
}

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String = FolderCategory.CAMPAIGN.name,
    val colorHex: String = "#2563EB",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
