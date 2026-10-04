package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EyeShape
import com.example.data.model.FrameStyle
import com.example.data.model.LogoPreset
import com.example.data.model.ModuleShape
import com.example.data.model.QrCodeEntity
import com.example.generator.BatchQrItem
import com.example.generator.BatchZipExporter
import com.example.generator.CsvQrParser
import com.example.ui.components.ColorPickerRow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchQrScreen(
    currentAuthorName: String,
    onBack: () -> Unit,
    onBatchSaved: (List<QrCodeEntity>) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var inputMode by remember { mutableIntStateOf(0) } // 0: Import Fichier CSV, 1: Texte CSV Direct
    var csvText by remember { mutableStateOf(CsvQrParser.SAMPLE_RESTAURANT_CSV) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    // Batch Design settings
    var batchFgColor by remember { mutableStateOf("#0F172A") }
    var batchBgColor by remember { mutableStateOf("#FFFFFF") }
    var batchEyeColor by remember { mutableStateOf("#2563EB") }
    var batchModuleShape by remember { mutableStateOf(ModuleShape.ROUNDED.name) }
    var batchEyeShape by remember { mutableStateOf(EyeShape.ROUNDED.name) }
    var batchLogoPreset by remember { mutableStateOf(LogoPreset.BRIEFCASE.name) }
    var batchFrameStyle by remember { mutableStateOf(FrameStyle.SCAN_ME.name) }

    var isProcessing by remember { mutableStateOf(false) }
    var isExportingZip by remember { mutableStateOf(false) }
    var generatedList by remember { mutableStateOf<List<QrCodeEntity>?>(null) }

    // Parse items reactively
    val parsedItems = remember(csvText) {
        try {
            CsvQrParser.parse(csvText)
        } catch (_: Exception) {
            emptyList()
        }
    }
    val validCount = parsedItems.count { it.isValid }
    val invalidCount = parsedItems.count { !it.isValid }

    // CSV File Picker launcher
    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val text = stream.bufferedReader(Charsets.UTF_8).readText()
                    csvText = text
                    selectedFileName = uri.lastPathSegment ?: "fichier.csv"
                    Toast.makeText(context, "Fichier CSV chargé avec succès !", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur de lecture du fichier CSV", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Génération par Lot (CSV)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Workflow d'entreprise & Impression en masse", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
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
            // Header Explanation Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = Color.White)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Génération Industrielle de QR Codes",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Créez en 1 clic jusqu'à des centaines de QR codes prêts pour l'impression (menus de table, badges, inventaire de packaging) avec export ZIP groupé.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // Input Method Tabs
            item {
                TabRow(
                    selectedTabIndex = inputMode,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = inputMode == 0,
                        onClick = { inputMode = 0 },
                        text = { Text("Fichier CSV") },
                        icon = { Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = inputMode == 1,
                        onClick = { inputMode = 1 },
                        text = { Text("Éditeur Texte CSV") },
                        icon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            // Tab 0: File upload
            if (inputMode == 0) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (selectedFileName != null) "Fichier sélectionné : $selectedFileName" else "Téléversez votre fichier CSV",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Colonnes supportées : titre, destination_url, is_dynamic, tags, frame_text",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    csvPickerLauncher.launch(arrayOf("text/*", "application/csv", "*/*"))
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("pick_csv_button")
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Parcourir les fichiers CSV")
                            }
                        }
                    }
                }
            }

            // Tab 1: Text Area
            if (inputMode == 1) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Données CSV brutes (séparateur virgule ou point-virgule) :",
                            style = MaterialTheme.typography.titleSmall
                        )
                        OutlinedTextField(
                            value = csvText,
                            onValueChange = { csvText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("batch_csv_text_input"),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            placeholder = { Text("title;destination_url;is_dynamic;tags;frame_text\nTable 1;https://example.com/1;true;Resto;TABLE 1") }
                        )
                    }
                }
            }

            // Quick Samples Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Exemples prêts :", fontSize = 11.sp, color = Color.Gray)
                    OutlinedButton(
                        onClick = {
                            csvText = CsvQrParser.SAMPLE_RESTAURANT_CSV
                            selectedFileName = "restaurant_tables.csv"
                            batchLogoPreset = LogoPreset.RESTAURANT.name
                            batchEyeColor = "#D97706"
                            batchFrameStyle = FrameStyle.MENU.name
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("10 Tables Restaurant", fontSize = 10.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            csvText = CsvQrParser.SAMPLE_PRODUCTS_CSV
                            selectedFileName = "produits_packaging.csv"
                            batchLogoPreset = LogoPreset.STORE.name
                            batchEyeColor = "#0284C7"
                            batchFrameStyle = FrameStyle.SCAN_ME.name
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("4 Packaging Produits", fontSize = 10.sp)
                    }
                }
            }

            // Validation & Detected Rows Summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PlaylistAddCheck,
                            contentDescription = null,
                            tint = if (validCount > 0) Color(0xFF16A34A) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$validCount QR code(s) détecté(s)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (validCount > 0) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (invalidCount > 0) {
                        Surface(
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$invalidCount ligne(s) ignorée(s)",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB91C1C),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Preview Table of Parsed CSV Rows
            if (parsedItems.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Aperçu des entrées du lot :",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            parsedItems.take(6).forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = if (item.isDynamic) Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (item.isDynamic) "⚡ DYN" else "STAT",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.isDynamic) Color(0xFF2563EB) else Color(0xFF475569),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = item.content,
                                            fontSize = 10.sp,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Surface(
                                        color = Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = item.frameText,
                                            fontSize = 9.sp,
                                            color = Color(0xFF334155),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            if (parsedItems.size > 6) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "... et ${parsedItems.size - 6} autres codes dans le fichier CSV",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // Design Batch Customizer Section
            item {
                Text(
                    text = "Charte Graphique du Lot :",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ColorPickerRow(
                            label = "Couleur des modules du lot :",
                            selectedHex = batchFgColor,
                            onColorSelected = { batchFgColor = it }
                        )

                        ColorPickerRow(
                            label = "Couleur des repères (Yeux) :",
                            selectedHex = batchEyeColor,
                            onColorSelected = { batchEyeColor = it }
                        )

                        // Module Shape
                        Column {
                            Text("Forme des points :", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ModuleShape.values().take(3).forEach { shape ->
                                    FilterChip(
                                        selected = batchModuleShape == shape.name,
                                        onClick = { batchModuleShape = shape.name },
                                        label = { Text(shape.displayName, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        // Logo Preset
                        Column {
                            Text("Symbole central du lot :", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(listOf(LogoPreset.BRIEFCASE, LogoPreset.RESTAURANT, LogoPreset.STORE, LogoPreset.STAR, LogoPreset.GLOBE)) { preset ->
                                    FilterChip(
                                        selected = batchLogoPreset == preset.name,
                                        onClick = { batchLogoPreset = preset.name },
                                        label = { Text(preset.displayName, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Primary Generation Action
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (validCount > 0) {
                            isProcessing = true
                            coroutineScope.launch {
                                val entities = CsvQrParser.toEntities(
                                    batchItems = parsedItems,
                                    authorName = currentAuthorName,
                                    defaultFgColor = batchFgColor,
                                    defaultBgColor = batchBgColor,
                                    defaultEyeColor = batchEyeColor,
                                    moduleShape = batchModuleShape,
                                    eyeShape = batchEyeShape,
                                    logoPreset = batchLogoPreset,
                                    frameStyle = batchFrameStyle
                                )
                                onBatchSaved(entities)
                                generatedList = entities
                                isProcessing = false
                            }
                        } else {
                            Toast.makeText(context, "Aucune entrée valide dans le CSV", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = validCount > 0 && !isProcessing,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("launch_batch_generation_button")
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Génération en cours...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.FlashOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Générer les $validCount QR Codes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            // Result / Bulk Export ZIP Card
            generatedList?.let { list ->
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(28.dp))
                                Column {
                                    Text(
                                        text = "Lot généré avec succès !",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF14532D),
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "${list.size} QR codes enregistrés dans la base d'entreprise.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF166534)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    isExportingZip = true
                                    coroutineScope.launch {
                                        BatchZipExporter.createAndShareZip(
                                            context = context,
                                            qrCodes = list,
                                            zipNamePrefix = "Pack_Impression_${selectedFileName?.substringBefore(".") ?: "Lot"}"
                                        )
                                        isExportingZip = false
                                    }
                                },
                                enabled = !isExportingZip,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF16A34A),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("export_zip_button")
                            ) {
                                if (isExportingZip) {
                                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Création de l'archive ZIP vectorielle...")
                                } else {
                                    Icon(Icons.Default.Archive, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Télécharger le Pack ZIP (SVG + PNG HD)", fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Voir les codes sur l'accueil")
                            }
                        }
                    }
                }
            }
        }
    }
}
