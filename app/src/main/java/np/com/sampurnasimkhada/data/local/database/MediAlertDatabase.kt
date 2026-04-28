package np.com.sampurnasimkhada.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import np.com.sampurnasimkhada.data.local.dao.DoseLogDao
import np.com.sampurnasimkhada.data.local.dao.MedicineDao
import np.com.sampurnasimkhada.data.local.entity.DoseLogEntity
import np.com.sampurnasimkhada.data.local.entity.MedicineEntity

/**
 * Single Room database for MediAlert.
 *
 * Contains two tables:
 *  - [MedicineEntity]  — the medicine catalogue
 *  - [DoseLogEntity]   — per-day dose tracking log
 *
 * Singleton is managed here via the companion object.
 * In a Hilt project you would instead provide this via an @Module;
 * the companion-object singleton is kept here so the class compiles
 * without Hilt being wired up yet.
 */
@Database(
    entities = [MedicineEntity::class, DoseLogEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class MediAlertDatabase : RoomDatabase() {

    abstract fun medicineDao(): MedicineDao
    abstract fun doseLogDao(): DoseLogDao

    companion object {
        private const val DB_NAME = "medialert.db"

        @Volatile
        private var INSTANCE: MediAlertDatabase? = null

        /**
         * Returns the singleton database instance, creating it if necessary.
         *
         * Usage:
         * ```kotlin
         * val db = MediAlertDatabase.getInstance(applicationContext)
         * val dao = db.medicineDao()
         * ```
         */
        fun getInstance(context: Context): MediAlertDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): MediAlertDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                MediAlertDatabase::class.java,
                DB_NAME,
            )
                // Wipe and rebuild on version mismatch instead of crashing.
                // Replace with proper migrations before shipping to production.
                .fallbackToDestructiveMigration()
                .build()
    }
}
