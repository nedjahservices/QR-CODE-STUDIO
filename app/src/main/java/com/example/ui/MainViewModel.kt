package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.FolderEntity
import com.example.data.model.QrCodeEntity
import com.example.data.model.ScanEventEntity
import com.example.data.model.TeamMemberEntity
import com.example.data.repository.QrRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AnalyticsSummary(
    val totalScans: Int,
    val uniqueDaysCount: Int,
    val dynamicQrCount: Int,
    val scansToday: Int,
    val topCity: String,
    val topChannel: String,
    val androidSharePercent: Int,
    val iosSharePercent: Int
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QrRepository

    val allQrCodes: StateFlow<List<QrCodeEntity>>
    val dynamicQrCodes: StateFlow<List<QrCodeEntity>>
    val allScans: StateFlow<List<ScanEventEntity>>
    val recentScans: StateFlow<List<ScanEventEntity>>
    val teamMembers: StateFlow<List<TeamMemberEntity>>
    val allFolders: StateFlow<List<FolderEntity>>

    private val _currentMember = MutableStateFlow<TeamMemberEntity?>(null)
    val currentMember = _currentMember.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("ALL") // "ALL", "DYNAMIC", "STATIC"
    val selectedFilter = _selectedFilter.asStateFlow()

    // Folder states
    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    val selectedFolderId = _selectedFolderId.asStateFlow()

    private val _selectedFolderCategory = MutableStateFlow("ALL")
    val selectedFolderCategory = _selectedFolderCategory.asStateFlow()

    private val _movingQr = MutableStateFlow<QrCodeEntity?>(null)
    val movingQr = _movingQr.asStateFlow()

    private val _folderBeingEdited = MutableStateFlow<FolderEntity?>(null)
    val folderBeingEdited = _folderBeingEdited.asStateFlow()

    private val _isCreatingFolder = MutableStateFlow(false)
    val isCreatingFolder = _isCreatingFolder.asStateFlow()

    private val _activeTab = MutableStateFlow(0) // 0: Home, 1: Create, 2: Dynamics, 3: Analytics, 4: Team, 5: Templates
    val activeTab = _activeTab.asStateFlow()

    private val _editingDynamicQr = MutableStateFlow<QrCodeEntity?>(null)
    val editingDynamicQr = _editingDynamicQr.asStateFlow()

    private val _exportingQr = MutableStateFlow<QrCodeEntity?>(null)
    val exportingQr = _exportingQr.asStateFlow()

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage = _notificationMessage.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = QrRepository(db.qrCodeDao(), db.scanEventDao(), db.teamMemberDao(), db.folderDao())

        allQrCodes = repository.allQrCodes.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        dynamicQrCodes = repository.dynamicQrCodes.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allScans = repository.allScans.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        recentScans = repository.recentScans.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        teamMembers = repository.teamMembers.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allFolders = repository.allFolders.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Set initial user
        viewModelScope.launch {
            teamMembers.collect { members ->
                if (_currentMember.value == null && members.isNotEmpty()) {
                    _currentMember.value = members.first()
                }
            }
        }
    }

    val filteredQrCodes: StateFlow<List<QrCodeEntity>> = combine(
        allQrCodes,
        searchQuery,
        selectedFilter,
        selectedFolderId
    ) { list, query, filter, folderId ->
        list.filter { item ->
            val matchesFolder = (folderId == null) || (item.folderId == folderId)
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.tags.contains(query, ignoreCase = true) ||
                    item.folderName.contains(query, ignoreCase = true) ||
                    item.createdByUser.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                "DYNAMIC" -> item.isDynamic
                "STATIC" -> !item.isDynamic
                else -> true
            }
            matchesFolder && matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val analyticsSummary: StateFlow<AnalyticsSummary> = combine(
        allScans,
        dynamicQrCodes
    ) { scans, dynamics ->
        val total = scans.size
        val now = System.currentTimeMillis()
        val oneDayAgo = now - 86_400_000L
        val todayCount = scans.count { it.timestamp >= oneDayAgo }

        val cities = scans.groupingBy { it.city }.eachCount()
        val topCity = cities.maxByOrNull { it.value }?.key ?: "Paris"

        val channels = scans.groupingBy { it.channel }.eachCount()
        val topChannel = channels.maxByOrNull { it.value }?.key ?: "Affiche Vitrine"

        val androidCount = scans.count { it.deviceType.contains("Android", ignoreCase = true) }
        val iosCount = scans.count { it.deviceType.contains("iOS", ignoreCase = true) }
        val totalMobile = (androidCount + iosCount).coerceAtLeast(1)
        val androidPercent = (androidCount * 100) / totalMobile
        val iosPercent = 100 - androidPercent

        AnalyticsSummary(
            totalScans = total,
            uniqueDaysCount = 14,
            dynamicQrCount = dynamics.size,
            scansToday = todayCount,
            topCity = topCity,
            topChannel = topChannel,
            androidSharePercent = androidPercent,
            iosSharePercent = iosPercent
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AnalyticsSummary(0, 0, 0, 0, "-", "-", 50, 50)
    )

    fun setActiveTab(tabIndex: Int) {
        _activeTab.value = tabIndex
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setEditingDynamicQr(qr: QrCodeEntity?) {
        _editingDynamicQr.value = qr
    }

    fun setExportingQr(qr: QrCodeEntity?) {
        _exportingQr.value = qr
    }

    fun setCurrentMember(member: TeamMemberEntity) {
        _currentMember.value = member
        showNotification("Profil actif : ${member.name} (${member.role})")
    }

    fun showNotification(msg: String) {
        _notificationMessage.value = msg
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }

    fun saveQr(qr: QrCodeEntity, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            val author = _currentMember.value?.name ?: "Alexandre V."
            val entityToSave = qr.copy(
                createdByUser = if (qr.id == 0L) author else qr.createdByUser,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveQrCode(entityToSave)
            showNotification("QR Code « ${qr.title} » enregistré avec succès !")
            onFinished()
        }
    }

    fun deleteQr(qr: QrCodeEntity) {
        viewModelScope.launch {
            repository.deleteQrCode(qr)
            showNotification("QR Code « ${qr.title} » supprimé")
        }
    }

    fun updateDynamicDestination(qrId: Long, newUrl: String) {
        viewModelScope.launch {
            repository.updateDynamicDestination(qrId, newUrl)
            _editingDynamicQr.value = null
            showNotification("Redirection mise à jour en direct ! Inutile de réimprimer le QR.")
        }
    }

    fun toggleQrActiveStatus(qr: QrCodeEntity) {
        viewModelScope.launch {
            val newStatus = !qr.isActive
            repository.toggleActiveStatus(qr.id, newStatus)
            val statusText = if (newStatus) "activé" else "mis en pause"
            showNotification("QR « ${qr.title} » $statusText")
        }
    }

    fun simulateScan(qr: QrCodeEntity, channel: String = "Test Impression") {
        viewModelScope.launch {
            val cities = listOf("Paris", "Lyon", "Marseille", "Bordeaux", "Bruxelles", "Genève", "Montréal")
            val devices = listOf("Android", "iOS")
            val targetUrl = if (qr.isDynamic) qr.destinationUrl.ifBlank { qr.rawContent } else qr.rawContent
            repository.simulateOrRecordScan(
                qrCodeId = qr.id,
                qrTitle = qr.title,
                destinationUrl = targetUrl,
                channel = channel,
                city = cities.random(),
                device = devices.random()
            )
            showNotification("Scan enregistré avec succès pour « ${qr.title} » !")
        }
    }

    fun addTeamMember(name: String, role: String, email: String, department: String) {
        viewModelScope.launch {
            val colors = listOf("#4F46E5", "#0284C7", "#10B981", "#EC4899", "#8B5CF6", "#F59E0B")
            val member = TeamMemberEntity(
                name = name,
                role = role,
                email = email,
                avatarColorHex = colors.random(),
                department = department,
                qrCount = 0
            )
            repository.addTeamMember(member)
            showNotification("Membre $name ajouté à l'équipe !")
        }
    }

    fun saveBatchQrCodes(
        qrCodes: List<QrCodeEntity>,
        onFinished: (List<QrCodeEntity>) -> Unit
    ) {
        viewModelScope.launch {
            val author = _currentMember.value?.name ?: "Alexandre V."
            val now = System.currentTimeMillis()
            val preparedList = qrCodes.mapIndexed { i, qr ->
                qr.copy(
                    createdByUser = author,
                    createdAt = now + i * 20,
                    updatedAt = now + i * 20
                )
            }
            repository.saveBatchQrCodes(preparedList)
            showNotification("${preparedList.size} codes QR générés et enregistrés par lot !")
            onFinished(preparedList)
        }
    }

    fun setSelectedFolderId(folderId: Long?) {
        _selectedFolderId.value = folderId
    }

    fun setSelectedFolderCategory(category: String) {
        _selectedFolderCategory.value = category
    }

    fun setMovingQr(qr: QrCodeEntity?) {
        _movingQr.value = qr
    }

    fun setFolderBeingEdited(folder: FolderEntity?) {
        _folderBeingEdited.value = folder
    }

    fun setIsCreatingFolder(isCreating: Boolean) {
        _isCreatingFolder.value = isCreating
    }

    fun moveQrToFolder(qr: QrCodeEntity, folder: FolderEntity?) {
        viewModelScope.launch {
            val fId = folder?.id
            val fName = folder?.name ?: "Général"
            repository.moveQrToFolder(qr.id, fId, fName)
            showNotification("« ${qr.title} » déplacé dans « $fName »")
            _movingQr.value = null
        }
    }

    fun saveFolder(folder: FolderEntity, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveFolder(folder)
            showNotification(if (folder.id == 0L) "Dossier « ${folder.name} » créé !" else "Dossier « ${folder.name} » mis à jour !")
            _folderBeingEdited.value = null
            _isCreatingFolder.value = false
            onFinished()
        }
    }

    fun deleteFolder(folder: FolderEntity, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            if (_selectedFolderId.value == folder.id) {
                _selectedFolderId.value = null
            }
            repository.deleteFolder(folder)
            showNotification("Dossier « ${folder.name} » supprimé. Les QR codes ont été replacés dans Général.")
            onFinished()
        }
    }
}
