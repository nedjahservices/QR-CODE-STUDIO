package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FolderEntity
import com.example.data.model.QrCodeEntity
import com.example.ui.MainViewModel
import com.example.ui.components.CreateEditFolderDialog
import com.example.ui.components.EditDynamicDestinationDialog
import com.example.ui.components.ExportDialog
import com.example.ui.components.MoveToFolderDialog
import com.example.ui.screens.AnalyticsDashboardScreen
import com.example.ui.screens.BatchQrScreen
import com.example.ui.screens.CreateQrScreen
import com.example.ui.screens.DynamicManagerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.TeamScreen
import com.example.ui.screens.TemplatesScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val filteredQrs by viewModel.filteredQrCodes.collectAsStateWithLifecycle()
    val allQrs by viewModel.allQrCodes.collectAsStateWithLifecycle()
    val dynamicQrs by viewModel.dynamicQrCodes.collectAsStateWithLifecycle()
    val allScans by viewModel.allScans.collectAsStateWithLifecycle()
    val recentScans by viewModel.recentScans.collectAsStateWithLifecycle()
    val summary by viewModel.analyticsSummary.collectAsStateWithLifecycle()
    val teamMembers by viewModel.teamMembers.collectAsStateWithLifecycle()
    val currentMember by viewModel.currentMember.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    val allFolders by viewModel.allFolders.collectAsStateWithLifecycle()
    val selectedFolderId by viewModel.selectedFolderId.collectAsStateWithLifecycle()
    val selectedFolderCategory by viewModel.selectedFolderCategory.collectAsStateWithLifecycle()
    val movingQr by viewModel.movingQr.collectAsStateWithLifecycle()
    val folderBeingEdited by viewModel.folderBeingEdited.collectAsStateWithLifecycle()
    val isCreatingFolder by viewModel.isCreatingFolder.collectAsStateWithLifecycle()

    val editingDynamicQr by viewModel.editingDynamicQr.collectAsStateWithLifecycle()
    val exportingQr by viewModel.exportingQr.collectAsStateWithLifecycle()
    val notificationMessage by viewModel.notificationMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Screen state: if not null, we are in the Create/Edit screen
    var editingEntity by remember { mutableStateOf<QrCodeEntity?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var isBatchGenerating by remember { mutableStateOf(false) }

    LaunchedEffect(notificationMessage) {
        notificationMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    if (isBatchGenerating) {
        BackHandler {
            isBatchGenerating = false
        }

        BatchQrScreen(
            currentAuthorName = currentMember?.name ?: "Alexandre V.",
            onBack = { isBatchGenerating = false },
            onBatchSaved = { entities ->
                viewModel.saveBatchQrCodes(entities) {
                    // Stay on result card or user can click Back
                }
            }
        )
    } else if (isCreatingNew || editingEntity != null) {
        BackHandler {
            isCreatingNew = false
            editingEntity = null
        }

        CreateQrScreen(
            initialQr = editingEntity,
            availableFolders = allFolders,
            onBack = {
                isCreatingNew = false
                editingEntity = null
            },
            onSaveAndExport = { qr ->
                viewModel.saveQr(qr) {
                    viewModel.setExportingQr(qr)
                    isCreatingNew = false
                    editingEntity = null
                }
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { viewModel.setActiveTab(0) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Accueil") },
                        label = { Text("Accueil", fontSize = 11.sp, fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { viewModel.setActiveTab(1) },
                        icon = { Icon(Icons.Default.Bolt, contentDescription = "Dynamiques") },
                        label = { Text("Dynamiques", fontSize = 11.sp, fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("nav_dynamics")
                    )
                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { viewModel.setActiveTab(2) },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") },
                        label = { Text("Analytics", fontSize = 11.sp, fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("nav_analytics")
                    )
                    NavigationBarItem(
                        selected = activeTab == 3,
                        onClick = { viewModel.setActiveTab(3) },
                        icon = { Icon(Icons.Default.FlashOn, contentDescription = "Modèles") },
                        label = { Text("Modèles", fontSize = 11.sp, fontWeight = if (activeTab == 3) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("nav_templates")
                    )
                    NavigationBarItem(
                        selected = activeTab == 4,
                        onClick = { viewModel.setActiveTab(4) },
                        icon = { Icon(Icons.Default.Group, contentDescription = "Équipe") },
                        label = { Text("Équipe", fontSize = 11.sp, fontWeight = if (activeTab == 4) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("nav_team")
                    )
                }
            }
        ) { innerPadding ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (activeTab) {
                    0 -> HomeScreen(
                        qrCodes = filteredQrs,
                        allQrCodes = allQrs,
                        folders = allFolders,
                        selectedFolderId = selectedFolderId,
                        selectedFolderCategory = selectedFolderCategory,
                        analytics = summary,
                        searchQuery = searchQuery,
                        selectedFilter = selectedFilter,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onFilterChange = { viewModel.setSelectedFilter(it) },
                        onSelectFolderId = { viewModel.setSelectedFolderId(it) },
                        onSelectFolderCategory = { viewModel.setSelectedFolderCategory(it) },
                        onCreateFolderClick = { viewModel.setIsCreatingFolder(true) },
                        onEditFolderClick = { viewModel.setFolderBeingEdited(it) },
                        onDeleteFolderClick = { viewModel.deleteFolder(it) },
                        onMoveQrToFolderClick = { viewModel.setMovingQr(it) },
                        onCreateQrClick = { isCreatingNew = true },
                        onBatchClick = { isBatchGenerating = true },
                        onTemplatesClick = { viewModel.setActiveTab(3) },
                        onEditDynamicDestination = { viewModel.setEditingDynamicQr(it) },
                        onExportQr = { viewModel.setExportingQr(it) },
                        onSimulateScan = { viewModel.simulateScan(it) },
                        onToggleActive = { viewModel.toggleQrActiveStatus(it) },
                        onDeleteQr = { viewModel.deleteQr(it) }
                    )
                    1 -> DynamicManagerScreen(
                        dynamicQrs = dynamicQrs,
                        onEditDestination = { viewModel.setEditingDynamicQr(it) },
                        onToggleActive = { viewModel.toggleQrActiveStatus(it) },
                        onSimulateScan = { viewModel.simulateScan(it) },
                        onExport = { viewModel.setExportingQr(it) },
                        onCreateNewDynamic = { isCreatingNew = true }
                    )
                    2 -> AnalyticsDashboardScreen(
                        summary = summary,
                        allScans = allScans,
                        recentScans = recentScans,
                        allQrs = allQrs,
                        onSimulateScanClick = { viewModel.simulateScan(it) }
                    )
                    3 -> TemplatesScreen(
                        onSelectTemplate = { templateEntity ->
                            editingEntity = templateEntity
                        },
                        onExportTemplateDirect = { templateEntity ->
                            viewModel.setExportingQr(templateEntity)
                        }
                    )
                    4 -> TeamScreen(
                        members = teamMembers,
                        currentMember = currentMember,
                        onSwitchMember = { viewModel.setCurrentMember(it) },
                        onAddMember = { name, role, email, dept ->
                            viewModel.addTeamMember(name, role, email, dept)
                        }
                    )
                }
            }
        }
    }

    // Dynamic destination editing dialog
    editingDynamicQr?.let { qr ->
        EditDynamicDestinationDialog(
            qr = qr,
            onDismiss = { viewModel.setEditingDynamicQr(null) },
            onConfirmNewDestination = { newUrl ->
                viewModel.updateDynamicDestination(qr.id, newUrl)
            }
        )
    }

    // Vector SVG and Print PDF export modal sheet
    exportingQr?.let { qr ->
        ExportDialog(
            qr = qr,
            onDismiss = { viewModel.setExportingQr(null) }
        )
    }

    // Move to folder dialog
    movingQr?.let { qr ->
        MoveToFolderDialog(
            qrCode = qr,
            folders = allFolders,
            onDismiss = { viewModel.setMovingQr(null) },
            onMoveToFolder = { targetFolder ->
                viewModel.moveQrToFolder(qr, targetFolder)
            }
        )
    }

    // Create or Edit folder dialog
    if (isCreatingFolder || folderBeingEdited != null) {
        CreateEditFolderDialog(
            initialFolder = folderBeingEdited,
            onDismiss = {
                viewModel.setIsCreatingFolder(false)
                viewModel.setFolderBeingEdited(null)
            },
            onSave = { folder ->
                viewModel.saveFolder(folder)
            }
        )
    }
}
