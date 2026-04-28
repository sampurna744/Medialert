package np.com.sampurnasimkhada.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity — records every dose action (taken / missed / snoozed).
 * Has a foreign-key back to the parent MedicineEntity so deleting
 * a medicine automatically cascades and removes its dose logs.
 */
@Entity(
    tableName = "dose_logs",
    foreignKeys = [
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("medicineId")],
)
data class DoseLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** FK to medicines.id */
    val medicineId: Long,

    /** yyyy-MM-dd of the scheduled dose */
    val scheduledDate: String,

    /** HH:mm of the scheduled dose */
    val scheduledTime: String,

    /**
     * Outcome of this dose slot.
     * One of: "TAKEN", "MISSED", "UPCOMING", "SNOOZED"
     */
    val status: String = "UPCOMING",

    /** Epoch ms when the user acted (null = not yet acted) */
    val actionTimestamp: Long? = null,
)
