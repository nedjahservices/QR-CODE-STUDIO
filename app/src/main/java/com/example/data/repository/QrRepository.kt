package com.example.data.repository

import com.example.data.dao.FolderDao
import com.example.data.dao.QrCodeDao
import com.example.data.dao.ScanEventDao
import com.example.data.dao.TeamMemberDao
import com.example.data.model.FolderEntity
import com.example.data.model.QrCodeEntity
import com.example.data.model.ScanEventEntity
import com.example.data.model.TeamMemberEntity
import kotlinx.coroutines.flow.Flow

class QrRepository(
    private val qrCodeDao: QrCodeDao,
    private val scanEventDao: ScanEventDao,
    private val teamMemberDao: TeamMemberDao,
    private val folderDao: FolderDao
) {
    val allQrCodes: Flow<List<QrCodeEntity>> = qrCodeDao.getAllQrCodes()
    val dynamicQrCodes: Flow<List<QrCodeEntity>> = qrCodeDao.getDynamicQrCodes()
    val allScans: Flow<List<ScanEventEntity>> = scanEventDao.getAllScans()
    val recentScans: Flow<List<ScanEventEntity>> = scanEventDao.getRecentScans(30)
    val teamMembers: Flow<List<TeamMemberEntity>> = teamMemberDao.getAllMembers()
    val allFolders: Flow<List<FolderEntity>> = folderDao.getAllFolders()

    suspend fun getQrById(id: Long): QrCodeEntity? = qrCodeDao.getQrCodeById(id)

    suspend fun saveQrCode(qrCode: QrCodeEntity): Long {
        return if (qrCode.id == 0L) {
            qrCodeDao.insertQrCode(qrCode)
        } else {
            qrCodeDao.updateQrCode(qrCode)
            qrCode.id
        }
    }

    suspend fun saveBatchQrCodes(qrCodes: List<QrCodeEntity>): List<Long> {
        return qrCodeDao.insertQrCodes(qrCodes)
    }

    suspend fun deleteQrCode(qrCode: QrCodeEntity) {
        qrCodeDao.deleteQrCode(qrCode)
    }

    suspend fun updateDynamicDestination(id: Long, newUrl: String) {
        qrCodeDao.updateDestinationUrl(id, newUrl)
    }

    suspend fun toggleActiveStatus(id: Long, isActive: Boolean) {
        qrCodeDao.updateActiveStatus(id, isActive)
    }

    suspend fun moveQrToFolder(qrId: Long, folderId: Long?, folderName: String) {
        qrCodeDao.moveToFolder(qrId, folderId, folderName)
    }

    suspend fun saveFolder(folder: FolderEntity): Long {
        return if (folder.id == 0L) {
            folderDao.insertFolder(folder)
        } else {
            folderDao.updateFolder(folder)
            folder.id
        }
    }

    suspend fun deleteFolder(folder: FolderEntity) {
        qrCodeDao.detachFromFolder(folder.id)
        folderDao.deleteFolder(folder)
    }

    suspend fun simulateOrRecordScan(
        qrCodeId: Long,
        qrTitle: String,
        destinationUrl: String,
        channel: String = "Test Simulateur",
        city: String = "Paris",
        device: String = "Android"
    ) {
        qrCodeDao.incrementScanCount(qrCodeId)
        scanEventDao.recordScan(
            ScanEventEntity(
                qrCodeId = qrCodeId,
                qrTitle = qrTitle,
                timestamp = System.currentTimeMillis(),
                deviceType = device,
                city = city,
                country = "France",
                channel = channel,
                destinationReached = destinationUrl
            )
        )
    }

    suspend fun addTeamMember(member: TeamMemberEntity) = teamMemberDao.insertMember(member)
    suspend fun updateTeamMember(member: TeamMemberEntity) = teamMemberDao.updateMember(member)
    suspend fun deleteTeamMember(member: TeamMemberEntity) = teamMemberDao.deleteMember(member)
}
