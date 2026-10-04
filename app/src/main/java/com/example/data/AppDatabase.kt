package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.FolderDao
import com.example.data.dao.QrCodeDao
import com.example.data.dao.ScanEventDao
import com.example.data.dao.TeamMemberDao
import com.example.data.model.EyeShape
import com.example.data.model.FolderCategory
import com.example.data.model.FolderEntity
import com.example.data.model.FrameStyle
import com.example.data.model.LogoPreset
import com.example.data.model.ModuleShape
import com.example.data.model.QrCodeEntity
import com.example.data.model.QrType
import com.example.data.model.ScanEventEntity
import com.example.data.model.TeamMemberEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [QrCodeEntity::class, ScanEventEntity::class, TeamMemberEntity::class, FolderEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun qrCodeDao(): QrCodeDao
    abstract fun scanEventDao(): ScanEventDao
    abstract fun teamMemberDao(): TeamMemberDao
    abstract fun folderDao(): FolderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "qr_studio_pro.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialEnterpriseData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialEnterpriseData(db: AppDatabase) {
            val qrDao = db.qrCodeDao()
            val scanDao = db.scanEventDao()
            val teamDao = db.teamMemberDao()
            val folderDao = db.folderDao()

            // 1. Folders for categorization (Campaign, Client, Department)
            val folderCampaign = FolderEntity(
                name = "Campagnes Promo & Vitrines",
                category = FolderCategory.CAMPAIGN.name,
                colorHex = "#4F46E5",
                description = "Campagnes marketing grand public, affichage urbain et promos"
            )
            val folderClient = FolderEntity(
                name = "Client - Le Bistrot Parisien",
                category = FolderCategory.CLIENT.name,
                colorHex = "#D97706",
                description = "Comptes et établissements clients sous contrat d'accompagnement"
            )
            val folderDept = FolderEntity(
                name = "Département Commercial & Direction",
                category = FolderCategory.DEPARTMENT.name,
                colorHex = "#2563EB",
                description = "Supports institutionnels, vCards de l'équipe et relations partenaires"
            )
            val folderReviews = FolderEntity(
                name = "Avis Clients & E-Réputation",
                category = FolderCategory.CAMPAIGN.name,
                colorHex = "#0284C7",
                description = "QR codes de comptoir pour booster les avis Google 5 étoiles"
            )

            val fIdCampaign = folderDao.insertFolder(folderCampaign)
            val fIdClient = folderDao.insertFolder(folderClient)
            val fIdDept = folderDao.insertFolder(folderDept)
            val fIdReviews = folderDao.insertFolder(folderReviews)

            // 2. Team members
            val alex = TeamMemberEntity(
                name = "Alexandre Valois",
                role = "Directeur Marketing",
                email = "a.valois@techcorp-group.com",
                avatarColorHex = "#4F46E5",
                department = "Marketing Global",
                qrCount = 3
            )
            val sophie = TeamMemberEntity(
                name = "Sophie Martin",
                role = "Lead Designer & Print",
                email = "sophie.m@techcorp-group.com",
                avatarColorHex = "#EC4899",
                department = "Design Studio",
                qrCount = 2
            )
            val lucas = TeamMemberEntity(
                name = "Lucas Moreau",
                role = "Responsable Retail & Événements",
                email = "lucas.moreau@techcorp-group.com",
                avatarColorHex = "#10B981",
                department = "Opérations Retail",
                qrCount = 1
            )
            teamDao.insertMember(alex)
            teamDao.insertMember(sophie)
            teamDao.insertMember(lucas)

            // 2. Initial Enterprise QR Codes
            val now = System.currentTimeMillis()
            val day = 86_400_000L

            val qr1 = QrCodeEntity(
                title = "Menu Digital & Carte des Vins",
                type = QrType.URL.name,
                isDynamic = true,
                dynamicShortCode = "menu-gourmet-24",
                rawContent = "https://qr.biz/d/menu-gourmet-24",
                destinationUrl = "https://le-bistrot-parisien.fr/carte-saison-ete",
                isActive = true,
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
                createdByUser = "Lucas Moreau",
                createdAt = now - (14 * day),
                updatedAt = now - (2 * day),
                totalScans = 184,
                tags = "Restaurant, Table, Menu",
                folderId = fIdClient,
                folderName = folderClient.name
            )

            val qr2 = QrCodeEntity(
                title = "Campagne Affiches Métro & Vitrine",
                type = QrType.URL.name,
                isDynamic = true,
                dynamicShortCode = "promo-autumn-50",
                rawContent = "https://qr.biz/d/promo-autumn-50",
                destinationUrl = "https://techcorp-group.com/fr/offres-speciales",
                isActive = true,
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
                createdByUser = "Alexandre Valois",
                createdAt = now - (21 * day),
                updatedAt = now - (5 * day),
                totalScans = 428,
                tags = "Vitrine, Affichage, Promo",
                folderId = fIdCampaign,
                folderName = folderCampaign.name
            )

            val qr3 = QrCodeEntity(
                title = "Carte de Visite vCard PDG",
                type = QrType.VCARD.name,
                isDynamic = false,
                dynamicShortCode = "",
                rawContent = "BEGIN:VCARD\nVERSION:3.0\nN:Valois;Alexandre;;;\nFN:Alexandre Valois\nORG:TechCorp Group\nTITLE:Directeur Marketing\nTEL:+33140506070\nEMAIL:a.valois@techcorp-group.com\nURL:https://techcorp-group.com\nEND:VCARD",
                destinationUrl = "",
                isActive = true,
                fgColorHex = "#18181B",
                bgColorHex = "#FAFAFA",
                eyeColorHex = "#2563EB",
                eyeInnerColorHex = "#1D4ED8",
                moduleShape = ModuleShape.SQUARE.name,
                eyeShape = EyeShape.SQUARE.name,
                logoPreset = LogoPreset.BRIEFCASE.name,
                frameStyle = FrameStyle.SCAN_ME.name,
                frameText = "CONTACT PRO",
                frameColorHex = "#2563EB",
                createdByUser = "Sophie Martin",
                createdAt = now - (7 * day),
                updatedAt = now - (7 * day),
                totalScans = 56,
                tags = "vCard, Impression Carte",
                folderId = fIdDept,
                folderName = folderDept.name
            )

            val qr4 = QrCodeEntity(
                title = "Avis Google & Réputation Vitrine",
                type = QrType.URL.name,
                isDynamic = true,
                dynamicShortCode = "avis-google-tech",
                rawContent = "https://qr.biz/d/avis-google-tech",
                destinationUrl = "https://g.page/r/techcorp-reviews",
                isActive = true,
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
                createdByUser = "Alexandre Valois",
                createdAt = now - (10 * day),
                updatedAt = now - (1 * day),
                totalScans = 312,
                tags = "Avis, Google, Comptoir",
                folderId = fIdReviews,
                folderName = folderReviews.name
            )

            val id1 = qrDao.insertQrCode(qr1)
            val id2 = qrDao.insertQrCode(qr2)
            val id3 = qrDao.insertQrCode(qr3)
            val id4 = qrDao.insertQrCode(qr4)

            // 3. Scan events history for realistic dashboard
            val cities = listOf("Paris", "Lyon", "Marseille", "Toulouse", "Nice", "Nantes", "Bruxelles", "Bordeaux")
            val devices = listOf("iOS", "Android", "Android", "iOS", "Android", "iOS", "Android")
            val channels = listOf("Affiche Vitrine", "Table Restaurant", "Packaging", "Flyer Promo", "Comptoir Caisse")

            var scanTime = now - (14 * day)
            val step = (14 * day) / 60
            for (i in 0 until 60) {
                scanTime += step + ((i * 1337) % 50000)
                val targetQrId = when {
                    i % 3 == 0 -> id2
                    i % 2 == 0 -> id1
                    else -> id4
                }
                val qrTitle = when (targetQrId) {
                    id1 -> qr1.title
                    id2 -> qr2.title
                    else -> qr4.title
                }
                scanDao.recordScan(
                    ScanEventEntity(
                        qrCodeId = targetQrId,
                        qrTitle = qrTitle,
                        timestamp = scanTime,
                        deviceType = devices[i % devices.size],
                        city = cities[i % cities.size],
                        country = "France",
                        channel = channels[i % channels.size],
                        destinationReached = if (targetQrId == id1) qr1.destinationUrl else qr2.destinationUrl
                    )
                )
            }
        }
    }
}
