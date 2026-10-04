package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class QrType(val displayName: String, val iconName: String) {
    URL("Lien Web / URL", "link"),
    VCARD("Carte de visite (vCard)", "badge"),
    WIFI("Réseau Wi-Fi", "wifi"),
    EMAIL("Courriel / Email", "email"),
    SMS("SMS / Message", "sms"),
    SOCIAL("Réseaux Sociaux", "share"),
    TEXT("Texte Brut", "text_snippet")
}

enum class ModuleShape(val displayName: String) {
    SQUARE("Carrés classiques"),
    DOTS("Points ronds"),
    ROUNDED("Coins arrondis"),
    SQUIRCLE("Super-ellipses"),
    DIAMOND("Losanges")
}

enum class EyeShape(val displayName: String) {
    SQUARE("Carré classique"),
    ROUNDED("Coins arrondis"),
    CIRCLE("Cercles concentriques")
}

enum class LogoPreset(val displayName: String, val iconResId: String) {
    NONE("Aucun", ""),
    GLOBE("Site Web", "language"),
    STORE("Boutique", "storefront"),
    RESTAURANT("Restaurant / Menu", "restaurant"),
    STAR("Avis / Étoile", "star"),
    BRIEFCASE("Entreprise", "business_center"),
    PHONE("Contact", "phone"),
    SHIELD("Sécurité", "verified_user"),
    SHOPPING("Panier / E-shop", "shopping_cart"),
    LOCATION("Localisation", "location_on")
}

enum class FrameStyle(val displayName: String, val defaultText: String) {
    NONE("Sans cadre", ""),
    SCAN_ME("Bandeau classique", "SCANNEZ-MOI"),
    VISIT_US("Découverte", "VISITEZ NOTRE SITE"),
    MENU("Restaurant", "VOIR LA CARTE"),
    OFFER("Offre Spéciale", "PROFITER DE L'OFFRE"),
    WIFI("Accès Wi-Fi", "CONNEXION WI-FI")
}

@Entity(tableName = "qr_codes")
data class QrCodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: String = QrType.URL.name,
    val isDynamic: Boolean = true,
    val dynamicShortCode: String = "",
    val rawContent: String,
    val destinationUrl: String = "",
    val isActive: Boolean = true,
    val fgColorHex: String = "#0F172A",
    val bgColorHex: String = "#FFFFFF",
    val eyeColorHex: String = "#2563EB",
    val eyeInnerColorHex: String = "#1D4ED8",
    val gradientEndHex: String? = null,
    val moduleShape: String = ModuleShape.ROUNDED.name,
    val eyeShape: String = EyeShape.ROUNDED.name,
    val logoPreset: String = LogoPreset.NONE.name,
    val customLogoUri: String? = null,
    val frameStyle: String = FrameStyle.SCAN_ME.name,
    val frameText: String = "SCANNEZ-MOI",
    val frameColorHex: String = "#2563EB",
    val createdByUser: String = "Alexandre V.",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val totalScans: Int = 0,
    val tags: String = "Entreprise",
    val folderId: Long? = null,
    val folderName: String = "Général"
)

@Entity(tableName = "scan_events")
data class ScanEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val qrCodeId: Long,
    val qrTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceType: String = "Android",
    val city: String = "Paris",
    val country: String = "France",
    val channel: String = "Affiche Vitrine",
    val userAgent: String = "Mozilla/5.0 (Mobile)",
    val destinationReached: String = ""
)

@Entity(tableName = "team_members")
data class TeamMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val role: String,
    val email: String,
    val avatarColorHex: String = "#3B82F6",
    val department: String = "Marketing",
    val qrCount: Int = 0
)
