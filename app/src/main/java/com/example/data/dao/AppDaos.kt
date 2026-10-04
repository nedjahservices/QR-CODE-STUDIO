package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FolderEntity
import com.example.data.model.QrCodeEntity
import com.example.data.model.ScanEventEntity
import com.example.data.model.TeamMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QrCodeDao {
    @Query("SELECT * FROM qr_codes ORDER BY createdAt DESC")
    fun getAllQrCodes(): Flow<List<QrCodeEntity>>

    @Query("SELECT * FROM qr_codes WHERE isDynamic = 1 ORDER BY updatedAt DESC")
    fun getDynamicQrCodes(): Flow<List<QrCodeEntity>>

    @Query("SELECT * FROM qr_codes WHERE id = :id")
    suspend fun getQrCodeById(id: Long): QrCodeEntity?

    @Query("SELECT * FROM qr_codes WHERE dynamicShortCode = :shortCode LIMIT 1")
    suspend fun getByShortCode(shortCode: String): QrCodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQrCode(qrCode: QrCodeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQrCodes(qrCodes: List<QrCodeEntity>): List<Long>

    @Update
    suspend fun updateQrCode(qrCode: QrCodeEntity)

    @Delete
    suspend fun deleteQrCode(qrCode: QrCodeEntity)

    @Query("UPDATE qr_codes SET destinationUrl = :newUrl, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateDestinationUrl(id: Long, newUrl: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE qr_codes SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateActiveStatus(id: Long, isActive: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE qr_codes SET totalScans = totalScans + 1 WHERE id = :id")
    suspend fun incrementScanCount(id: Long)

    @Query("UPDATE qr_codes SET folderId = :folderId, folderName = :folderName, updatedAt = :updatedAt WHERE id = :qrId")
    suspend fun moveToFolder(qrId: Long, folderId: Long?, folderName: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE qr_codes SET folderId = NULL, folderName = 'Général', updatedAt = :updatedAt WHERE folderId = :folderId")
    suspend fun detachFromFolder(folderId: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM qr_codes WHERE folderId = :folderId ORDER BY createdAt DESC")
    fun getQrCodesByFolder(folderId: Long): Flow<List<QrCodeEntity>>

    @Query("SELECT COUNT(*) FROM qr_codes")
    fun getQrCodeCount(): Flow<Int>
}

@Dao
interface ScanEventDao {
    @Query("SELECT * FROM scan_events ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanEventEntity>>

    @Query("SELECT * FROM scan_events WHERE qrCodeId = :qrCodeId ORDER BY timestamp DESC")
    fun getScansForQr(qrCodeId: Long): Flow<List<ScanEventEntity>>

    @Query("SELECT * FROM scan_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentScans(limit: Int = 20): Flow<List<ScanEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordScan(scan: ScanEventEntity): Long

    @Query("SELECT COUNT(*) FROM scan_events")
    fun getTotalScansCount(): Flow<Int>
}

@Dao
interface TeamMemberDao {
    @Query("SELECT * FROM team_members ORDER BY id ASC")
    fun getAllMembers(): Flow<List<TeamMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: TeamMemberEntity): Long

    @Update
    suspend fun updateMember(member: TeamMemberEntity)

    @Delete
    suspend fun deleteMember(member: TeamMemberEntity)

    @Query("SELECT COUNT(*) FROM team_members")
    suspend fun getMemberCount(): Int
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: Long): FolderEntity?

    @Query("SELECT * FROM folders WHERE category = :category ORDER BY name ASC")
    fun getFoldersByCategory(category: String): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("SELECT COUNT(*) FROM folders")
    fun getFolderCount(): Flow<Int>
}

