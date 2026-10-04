package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ShapeLine
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
import com.example.data.model.FolderEntity
import com.example.generator.QrBitmapRenderer
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EyeShape
import com.example.data.model.FrameStyle
import com.example.data.model.LogoPreset
import com.example.data.model.ModuleShape
import com.example.data.model.QrCodeEntity
import com.example.data.model.QrType
import com.example.data.model.VCardContact
import com.example.ui.components.ColorPickerRow
import com.example.ui.components.DetailedVCardForm
import com.example.ui.components.QrCodeView
import com.example.ui.components.StyleTemplatePickerSheet
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQrScreen(
    initialQr: QrCodeEntity? = null,
    availableFolders: List<FolderEntity> = emptyList(),
    onBack: () -> Unit,
    onSaveAndExport: (QrCodeEntity) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(initialQr?.title ?: "Nouveau QR Code") }
    var selectedType by remember { mutableStateOf(initialQr?.type ?: QrType.URL.name) }
    var isDynamic by remember { mutableStateOf(initialQr?.isDynamic ?: true) }

    // Folder classification
    var selectedFolderId by remember { mutableStateOf<Long?>(initialQr?.folderId) }
    var selectedFolderName by remember { mutableStateOf(initialQr?.folderName ?: "Général") }

    // Content fields
    var urlInput by remember { mutableStateOf(initialQr?.destinationUrl?.ifBlank { "https://mon-entreprise.com" } ?: "https://mon-entreprise.com") }
    var vcardContact by remember {
        mutableStateOf(
            if (initialQr?.type == QrType.VCARD.name && initialQr.rawContent.isNotBlank()) {
                VCardContact.fromRawVCard(initialQr.rawContent)
            } else {
                VCardContact.defaultSample()
            }
        )
    }

    var wifiSsid by remember { mutableStateOf("TechCorp_Guest") }
    var wifiPassword by remember { mutableStateOf("ProWifi2026") }

    var tags by remember { mutableStateOf(initialQr?.tags ?: "Marketing, Impression") }

    // Colors
    var fgColorHex by remember { mutableStateOf(initialQr?.fgColorHex ?: "#0F172A") }
    var bgColorHex by remember { mutableStateOf(initialQr?.bgColorHex ?: "#FFFFFF") }
    var eyeColorHex by remember { mutableStateOf(initialQr?.eyeColorHex ?: "#2563EB") }
    var eyeInnerColorHex by remember { mutableStateOf(initialQr?.eyeInnerColorHex ?: "#1D4ED8") }
    var gradientEndHex by remember { mutableStateOf(initialQr?.gradientEndHex) }

    // Shapes & Logo
    var moduleShape by remember { mutableStateOf(initialQr?.moduleShape ?: ModuleShape.ROUNDED.name) }
    var eyeShape by remember { mutableStateOf(initialQr?.eyeShape ?: EyeShape.ROUNDED.name) }
    var logoPreset by remember { mutableStateOf(initialQr?.logoPreset ?: LogoPreset.BRIEFCASE.name) }
    var customLogoUri by remember { mutableStateOf(initialQr?.customLogoUri) }

    // Frame
    var frameStyle by remember { mutableStateOf(initialQr?.frameStyle ?: FrameStyle.SCAN_ME.name) }
    var frameText by remember { mutableStateOf(initialQr?.frameText ?: "SCANNEZ-MOI") }
    var frameColorHex by remember { mutableStateOf(initialQr?.frameColorHex ?: "#2563EB") }

    // Tab state: 0: Contenu, 1: Couleurs, 2: Formes & Logo, 3: Cadre
    var selectedTab by remember { mutableIntStateOf(0) }
    var showStylePickerSheet by remember { mutableStateOf(false) }

    // Photo picker launcher (zero-permission Android Photo Picker compliant with Google Play policy)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customLogoUri = uri.toString()
            logoPreset = LogoPreset.NONE.name
            Toast.makeText(context, "Logo d'entreprise importé !", Toast.LENGTH_SHORT).show()
        }
    }

    // Dynamic shortcode
    val shortCode = remember(initialQr) {
        initialQr?.dynamicShortCode?.ifBlank { "biz-${UUID.randomUUID().toString().take(6)}" }
            ?: "biz-${UUID.randomUUID().toString().take(6)}"
    }

    val computedRawContent = remember(selectedType, isDynamic, urlInput, vcardContact, wifiSsid, wifiPassword, shortCode) {
        if (isDynamic) {
            "https://qr.biz/d/$shortCode"
        } else {
            when (selectedType) {
                QrType.VCARD.name -> vcardContact.toVCard3()
                QrType.WIFI.name -> "WIFI:S:$wifiSsid;T:WPA;P:$wifiPassword;;"
                else -> urlInput
            }
        }
    }

    val previewQr = QrCodeEntity(
        id = initialQr?.id ?: 0L,
        title = title.ifBlank { "Mon QR Code" },
        type = selectedType,
        isDynamic = isDynamic,
        dynamicShortCode = shortCode,
        rawContent = computedRawContent,
        destinationUrl = if (isDynamic) urlInput else "",
        isActive = true,
        fgColorHex = fgColorHex,
        bgColorHex = bgColorHex,
        eyeColorHex = eyeColorHex,
        eyeInnerColorHex = eyeInnerColorHex,
        gradientEndHex = gradientEndHex,
        moduleShape = moduleShape,
        eyeShape = eyeShape,
        logoPreset = logoPreset,
        customLogoUri = customLogoUri,
        frameStyle = frameStyle,
        frameText = frameText,
        frameColorHex = frameColorHex,
        tags = tags,
        folderId = selectedFolderId,
        folderName = selectedFolderName
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialQr != null) "Modifier le QR Code" else "Nouveau QR Code d'Entreprise") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    Button(
                        onClick = { onSaveAndExport(previewQr) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_qr_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Enregistrer")
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
            // Live Preview Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Aperçu en Direct Haute Définition",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.size(240.dp)) {
                            QrCodeView(
                                qr = previewQr,
                                modifier = Modifier.fillMaxSize(),
                                resolution = 800,
                                includeFrame = true
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isDynamic) "⚡ QR Dynamique : Redirection modifiable après impression" else "QR Statique direct",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDynamic) Color(0xFF1D4ED8) else Color(0xFF475569),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Quick Preset Style Launcher Button
            item {
                OutlinedButton(
                    onClick = { showStylePickerSheet = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("apply_preset_style_button")
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Appliquer un Style Prédéfini (Minimaliste, Marque, Tech)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }

            // Tabs Selector
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Contenu", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Couleurs", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Logo & Formes", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Cadre", fontSize = 12.sp) }
                    )
                }
            }

            // Tab 0: Contenu & Dynamic Switch
            if (selectedTab == 0) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Nom du QR Code / Campagne") },
                            placeholder = { Text("Ex: Menu Été, Vitrine Promo, Carte PDG...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("qr_title_input"),
                            singleLine = true
                        )

                        // Dynamic QR Toggle Card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDynamic) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.FlashOn,
                                            contentDescription = null,
                                            tint = if (isDynamic) Color(0xFF2563EB) else Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "QR Code Dynamique Modifiable",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDynamic) Color(0xFF1E40AF) else Color(0xFF334155)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Permet de modifier l'adresse web de redirection même après que vos affiches, flyers ou packaging soient imprimés.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDynamic) Color(0xFF1E3A8A) else Color(0xFF64748B)
                                    )
                                }
                                Switch(
                                    checked = isDynamic,
                                    onCheckedChange = { isDynamic = it },
                                    modifier = Modifier.testTag("dynamic_switch")
                                )
                            }
                        }

                        // Type selector chips
                        Text(
                            text = "Type de contenu :",
                            style = MaterialTheme.typography.titleSmall
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(QrType.values()) { type ->
                                FilterChip(
                                    selected = selectedType == type.name,
                                    onClick = { selectedType = type.name },
                                    label = { Text(type.displayName) }
                                )
                            }
                        }

                        // Conditional content inputs
                        when (selectedType) {
                            QrType.URL.name -> {
                                OutlinedTextField(
                                    value = urlInput,
                                    onValueChange = { urlInput = it },
                                    label = { Text("Adresse Web / URL de destination") },
                                    placeholder = { Text("https://mon-entreprise.com/offre") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("url_destination_input"),
                                    singleLine = false,
                                    maxLines = 3
                                )
                            }
                            QrType.VCARD.name -> {
                                DetailedVCardForm(
                                    contact = vcardContact,
                                    onContactChange = { updated ->
                                        vcardContact = updated
                                        if (title.isBlank() || title == "Nouveau QR Code" || title.startsWith("Carte de Visite")) {
                                            val name = listOf(updated.firstName, updated.lastName).filter { it.isNotBlank() }.joinToString(" ")
                                            if (name.isNotBlank()) {
                                                title = "Carte vCard - $name"
                                            }
                                        }
                                    }
                                )
                            }
                            QrType.WIFI.name -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = wifiSsid,
                                        onValueChange = { wifiSsid = it },
                                        label = { Text("Nom du réseau Wi-Fi (SSID)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = wifiPassword,
                                        onValueChange = { wifiPassword = it },
                                        label = { Text("Mot de passe") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                            }
                            else -> {
                                OutlinedTextField(
                                    value = urlInput,
                                    onValueChange = { urlInput = it },
                                    label = { Text("Contenu du message") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 4
                                )
                            }
                        }

                        OutlinedTextField(
                            value = tags,
                            onValueChange = { tags = it },
                            label = { Text("Tags / Support d'impression") },
                            placeholder = { Text("Ex: Affiche Vitrine, Menu Restaurant, Packaging...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Folder classification dropdown
                        if (availableFolders.isNotEmpty()) {
                            var expandedFolderDropdown by remember { mutableStateOf(false) }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Dossier de classement (Campagne / Client / Département) :",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { expandedFolderDropdown = true },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("select_folder_dropdown_button")
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Folder,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(selectedFolderName, fontWeight = FontWeight.SemiBold)
                                            }
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = expandedFolderDropdown,
                                        onDismissRequest = { expandedFolderDropdown = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Général (Aucun dossier)") },
                                            leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                                            onClick = {
                                                selectedFolderId = null
                                                selectedFolderName = "Général"
                                                expandedFolderDropdown = false
                                            }
                                        )
                                        availableFolders.forEach { f ->
                                            DropdownMenuItem(
                                                text = { Text(f.name) },
                                                leadingIcon = {
                                                    val color = Color(QrBitmapRenderer.safeParseColor(f.colorHex, android.graphics.Color.BLUE))
                                                    Icon(Icons.Default.Folder, contentDescription = null, tint = color)
                                                },
                                                onClick = {
                                                    selectedFolderId = f.id
                                                    selectedFolderName = f.name
                                                    expandedFolderDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab 1: Couleurs
            if (selectedTab == 1) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ColorPickerRow(
                            label = "Couleur principale des modules :",
                            selectedHex = fgColorHex,
                            onColorSelected = { fgColorHex = it }
                        )

                        ColorPickerRow(
                            label = "Couleur des repères (Yeux extérieurs) :",
                            selectedHex = eyeColorHex,
                            onColorSelected = {
                                eyeColorHex = it
                                eyeInnerColorHex = it
                            }
                        )

                        ColorPickerRow(
                            label = "Couleur de fond (Arrière-plan) :",
                            selectedHex = bgColorHex,
                            onColorSelected = { bgColorHex = it },
                            palette = listOf("#FFFFFF", "#FAFAFA", "#F8FAFC", "#F1F5F9", "#FEF3C7", "#EFF6FF")
                        )

                        // Dégradé optionnel
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Activer un dégradé bicolore", style = MaterialTheme.typography.titleSmall)
                            Switch(
                                checked = gradientEndHex != null,
                                onCheckedChange = { checked ->
                                    gradientEndHex = if (checked) "#0284C7" else null
                                }
                            )
                        }

                        if (gradientEndHex != null) {
                            ColorPickerRow(
                                label = "Seconde couleur du dégradé :",
                                selectedHex = gradientEndHex!!,
                                onColorSelected = { gradientEndHex = it }
                            )
                        }
                    }
                }
            }

            // Tab 2: Logo & Formes
            if (selectedTab == 2) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Module Shape
                        Text(
                            text = "Forme des points (Modules) :",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ModuleShape.values().take(3).forEach { shape ->
                                FilterChip(
                                    selected = moduleShape == shape.name,
                                    onClick = { moduleShape = shape.name },
                                    label = { Text(shape.displayName, fontSize = 11.sp) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ModuleShape.values().drop(3).forEach { shape ->
                                FilterChip(
                                    selected = moduleShape == shape.name,
                                    onClick = { moduleShape = shape.name },
                                    label = { Text(shape.displayName, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Eye Shape
                        Text(
                            text = "Forme des coins de repérage (Yeux) :",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EyeShape.values().forEach { shape ->
                                FilterChip(
                                    selected = eyeShape == shape.name,
                                    onClick = { eyeShape = shape.name },
                                    label = { Text(shape.displayName, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom Photo Picker Logo
                        Text(
                            text = "Intégration du Logo d'Entreprise :",
                            style = MaterialTheme.typography.titleSmall
                        )

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (customLogoUri != null) "Logo personnalisé sélectionné (Changer)" else "Importer mon logo (Galerie)"
                            )
                        }

                        if (customLogoUri != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Logo personnalisé actif",
                                    color = Color(0xFF16A34A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Button(
                                    onClick = { customLogoUri = null },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color.Red),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Retirer le logo", fontSize = 11.sp)
                                }
                            }
                        }

                        Text(
                            text = "Ou choisir un symbole d'entreprise prédéfini :",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Preset logos row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(LogoPreset.values()) { preset ->
                                val isSelected = logoPreset == preset.name && customLogoUri == null
                                val iconVector: ImageVector = when (preset) {
                                    LogoPreset.NONE -> Icons.Default.Check
                                    LogoPreset.GLOBE -> Icons.Default.Language
                                    LogoPreset.STORE -> Icons.Default.Storefront
                                    LogoPreset.RESTAURANT -> Icons.Default.Restaurant
                                    LogoPreset.STAR -> Icons.Default.Star
                                    LogoPreset.BRIEFCASE -> Icons.Default.BusinessCenter
                                    LogoPreset.PHONE -> Icons.Default.Phone
                                    LogoPreset.SHIELD -> Icons.Default.VerifiedUser
                                    LogoPreset.SHOPPING -> Icons.Default.ShoppingCart
                                    LogoPreset.LOCATION -> Icons.Default.LocationOn
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1)
                                    ),
                                    modifier = Modifier.clickable {
                                        logoPreset = preset.name
                                        customLogoUri = null
                                    }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = iconVector,
                                            contentDescription = preset.displayName,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF475569),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = preset.displayName,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab 3: Cadre & Callout Text
            if (selectedTab == 3) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Style du Cadre / Bandeau d'Appel à l'Action :",
                            style = MaterialTheme.typography.titleSmall
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(FrameStyle.values()) { style ->
                                FilterChip(
                                    selected = frameStyle == style.name,
                                    onClick = {
                                        frameStyle = style.name
                                        if (style.defaultText.isNotBlank()) {
                                            frameText = style.defaultText
                                        }
                                    },
                                    label = { Text(style.displayName) }
                                )
                            }
                        }

                        if (frameStyle != FrameStyle.NONE.name) {
                            OutlinedTextField(
                                value = frameText,
                                onValueChange = { frameText = it },
                                label = { Text("Texte affiché dans le bandeau") },
                                placeholder = { Text("SCANNEZ-MOI, VOIR LE MENU...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            ColorPickerRow(
                                label = "Couleur du bandeau :",
                                selectedHex = frameColorHex,
                                onColorSelected = { frameColorHex = it }
                            )
                        }
                    }
                }
            }

            // Bottom CTA Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onSaveAndExport(previewQr) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enregistrer et Exporter (SVG / PDF / PNG)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showStylePickerSheet) {
        StyleTemplatePickerSheet(
            onDismiss = { showStylePickerSheet = false },
            onSelectStyle = { style ->
                fgColorHex = style.fgColorHex
                bgColorHex = style.bgColorHex
                eyeColorHex = style.eyeColorHex
                eyeInnerColorHex = style.eyeInnerColorHex
                gradientEndHex = style.gradientEndHex
                moduleShape = style.moduleShape
                eyeShape = style.eyeShape
                if (customLogoUri == null) logoPreset = style.logoPreset
                frameStyle = style.frameStyle
                if (style.frameText.isNotBlank()) frameText = style.frameText
                frameColorHex = style.frameColorHex
                Toast.makeText(context, "Style « ${style.name} » appliqué !", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
