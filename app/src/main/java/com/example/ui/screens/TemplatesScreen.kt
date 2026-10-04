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
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EyeShape
import com.example.data.model.FrameStyle
import com.example.data.model.LogoPreset
import com.example.data.model.ModuleShape
import com.example.data.model.PredefinedStyleTemplates
import com.example.data.model.QrCodeEntity
import com.example.data.model.QrStyleTemplate
import com.example.data.model.QrType
import com.example.data.model.StyleCategory
import com.example.generator.QrBitmapRenderer
import com.example.ui.components.QrCodeView
import java.util.UUID

data class EnterpriseTemplate(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val icon: ImageVector,
    val themeColor: Color,
    val entity: QrCodeEntity
)

@Composable
fun TemplatesScreen(
    onSelectTemplate: (QrCodeEntity) -> Unit,
    onExportTemplateDirect: (QrCodeEntity) -> Unit = {}
) {
    // 0: Styles Graphiques Prédéfinis (Minimaliste, Brand-Focused, Tech-Inspired), 1: Cas d'Usage Métier
    var activeTab by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf(StyleCategory.ALL) }
    var previewUrlInput by remember { mutableStateOf("https://votre-entreprise.com") }

    val filteredStyleTemplates = remember(selectedCategory) {
        if (selectedCategory == StyleCategory.ALL) {
            PredefinedStyleTemplates.templates
        } else {
            PredefinedStyleTemplates.templates.filter { it.category == selectedCategory }
        }
    }

    val businessTemplates = listOf(
        EnterpriseTemplate(
            id = "menu_resto",
            name = "Restaurant & Menu Digital",
            category = "CHRD / Restauration",
            description = "QR dynamique sur tables et vitrine. Modifiez la carte des plats du jour en direct sans réimprimer vos supports.",
            icon = Icons.Default.Restaurant,
            themeColor = Color(0xFFD97706),
            entity = QrCodeEntity(
                title = "Menu Digital & Carte des Vins",
                type = QrType.URL.name,
                isDynamic = true,
                dynamicShortCode = "menu-${UUID.randomUUID().toString().take(5)}",
                rawContent = "https://qr.biz/d/menu-${UUID.randomUUID().toString().take(5)}",
                destinationUrl = "https://mon-restaurant.fr/carte-du-jour",
                fgColorHex = "#0F172A",
                bgColorHex = "#FFFFFF",
                eyeColorHex = "#D97706",
                eyeInnerColorHex = "#B45309",
                moduleShape = ModuleShape.ROUNDED.name,
                eyeShape = EyeShape.ROUNDED.name,
                logoPreset = LogoPreset.RESTAURANT.name,
                frameStyle = FrameStyle.MENU.name,
                frameText = "VOIR LA CARTE DU JOUR",
                frameColorHex = "#D97706",
                tags = "Restaurant, Table, Menu"
            )
        ),
        EnterpriseTemplate(
            id = "avis_google",
            name = "Avis Google & Réputation Vitrine",
            category = "Commerce & Retail",
            description = "À poser sur le comptoir de caisse ou vitrine. Redirige directement vos clients vers le formulaire d'avis 5 étoiles.",
            icon = Icons.Default.Star,
            themeColor = Color(0xFF0284C7),
            entity = QrCodeEntity(
                title = "Avis Google & Réputation Vitrine",
                type = QrType.URL.name,
                isDynamic = true,
                dynamicShortCode = "avis-${UUID.randomUUID().toString().take(5)}",
                rawContent = "https://qr.biz/d/avis-${UUID.randomUUID().toString().take(5)}",
                destinationUrl = "https://g.page/r/votre-boutique/review",
                fgColorHex = "#0C4A6E",
                bgColorHex = "#FFFFFF",
                eyeColorHex = "#0284C7",
                eyeInnerColorHex = "#0369A1",
                moduleShape = ModuleShape.DOTS.name,
                eyeShape = EyeShape.CIRCLE.name,
                logoPreset = LogoPreset.STAR.name,
                frameStyle = FrameStyle.VISIT_US.name,
                frameText = "VOTRE AVIS COMPTE",
                frameColorHex = "#0284C7",
                tags = "Avis, Google, Comptoir"
            )
        ),
        EnterpriseTemplate(
            id = "promo_flash",
            name = "Campagne Promo & Soldes",
            category = "Marketing & Événement",
            description = "Idéal pour affiches métros, packaging et flyers. Changez la promotion hebdomadaire sans toucher à l'impression.",
            icon = Icons.Default.ShoppingBag,
            themeColor = Color(0xFF4F46E5),
            entity = QrCodeEntity(
                title = "Campagne Affiches & Promo Flash",
                type = QrType.URL.name,
                isDynamic = true,
                dynamicShortCode = "promo-${UUID.randomUUID().toString().take(5)}",
                rawContent = "https://qr.biz/d/promo-${UUID.randomUUID().toString().take(5)}",
                destinationUrl = "https://mon-eshop.com/offres-speciales",
                fgColorHex = "#1E1B4B",
                bgColorHex = "#FFFFFF",
                eyeColorHex = "#4F46E5",
                eyeInnerColorHex = "#312E81",
                moduleShape = ModuleShape.SQUIRCLE.name,
                eyeShape = EyeShape.CIRCLE.name,
                logoPreset = LogoPreset.STORE.name,
                frameStyle = FrameStyle.OFFER.name,
                frameText = "FLASH PROMO -30%",
                frameColorHex = "#4F46E5",
                tags = "Promo, Flyer, Soldes"
            )
        ),
        EnterpriseTemplate(
            id = "vcard_pro",
            name = "Carte de Visite Corporate vCard",
            category = "Entreprise & Direction",
            description = "Fiche contact complète avec intégration automatique dans le carnet d'adresses du smartphone.",
            icon = Icons.Default.BusinessCenter,
            themeColor = Color(0xFF1E293B),
            entity = QrCodeEntity(
                title = "Carte de Visite vCard Executive",
                type = QrType.VCARD.name,
                isDynamic = false,
                rawContent = "BEGIN:VCARD\nVERSION:3.0\nFN:Alexandre Valois\nORG:TechCorp Group\nTITLE:Directeur Général\nTEL:+33140506070\nEMAIL:direction@techcorp.com\nURL:https://techcorp.com\nEND:VCARD",
                destinationUrl = "",
                fgColorHex = "#0F172A",
                bgColorHex = "#FAFAFA",
                eyeColorHex = "#2563EB",
                eyeInnerColorHex = "#1D4ED8",
                moduleShape = ModuleShape.ROUNDED.name,
                eyeShape = EyeShape.ROUNDED.name,
                logoPreset = LogoPreset.BRIEFCASE.name,
                frameStyle = FrameStyle.SCAN_ME.name,
                frameText = "CONTACT PRO",
                frameColorHex = "#2563EB",
                tags = "vCard, Impression Carte"
            )
        ),
        EnterpriseTemplate(
            id = "wifi_pro",
            name = "Wi-Fi Invité Sécurisé",
            category = "Bureaux & Salons",
            description = "Connexion instantanée des clients ou partenaires en scannant le code sans taper de mot de passe.",
            icon = Icons.Default.Wifi,
            themeColor = Color(0xFF059669),
            entity = QrCodeEntity(
                title = "Accès Wi-Fi Espace Réunion",
                type = QrType.WIFI.name,
                isDynamic = false,
                rawContent = "WIFI:S:Guest_Office_5G;T:WPA;P:Welcome2026!;;",
                destinationUrl = "",
                fgColorHex = "#064E3B",
                bgColorHex = "#FFFFFF",
                eyeColorHex = "#059669",
                eyeInnerColorHex = "#047857",
                moduleShape = ModuleShape.ROUNDED.name,
                eyeShape = EyeShape.ROUNDED.name,
                logoPreset = LogoPreset.SHIELD.name,
                frameStyle = FrameStyle.WIFI.name,
                frameText = "WI-FI GRATUIT",
                frameColorHex = "#059669",
                tags = "Wi-Fi, Bureau, Accueil"
            )
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Catalogue de Modèles & Styles",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Styles graphiques professionnels et gabarits prêts pour l'impression.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Top Switcher: Styles Graphiques vs Cas d'Usage
        item {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Styles Graphiques (Chartes)") },
                    icon = { Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Cas d'Usage Métier") },
                    icon = { Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        // === TAB 0: PRE-DEFINED STYLING TEMPLATES ===
        if (activeTab == 0) {
            // Live Preview Input
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Lien test pour prévisualiser tous les styles :",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = previewUrlInput,
                            onValueChange = { previewUrlInput = it },
                            placeholder = { Text("https://mon-entreprise.com") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("style_sample_url_input")
                        )
                    }
                }
            }

            // Categories Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(StyleCategory.values()) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.displayName, fontSize = 11.sp) }
                        )
                    }
                }
            }

            items(filteredStyleTemplates, key = { it.id }) { template ->
                val previewEntity = remember(template, previewUrlInput) {
                    template.toPreviewEntity(sampleContent = previewUrlInput.ifBlank { "https://mon-entreprise.com" })
                }
                StyleTemplateCard(
                    template = template,
                    previewEntity = previewEntity,
                    onSelectToCreate = { onSelectTemplate(previewEntity) },
                    onExportDirect = { onExportTemplateDirect(previewEntity) }
                )
            }
        }

        // === TAB 1: BUSINESS USE-CASE TEMPLATES ===
        if (activeTab == 1) {
            items(businessTemplates, key = { it.id }) { template ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("template_${template.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                QrCodeView(
                                    qr = template.entity,
                                    modifier = Modifier.fillMaxSize(),
                                    resolution = 380,
                                    includeFrame = false
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = template.themeColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = template.category.uppercase(),
                                        color = template.themeColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = template.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = template.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { onSelectTemplate(template.entity) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Utiliser ce Modèle d'Entreprise", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
