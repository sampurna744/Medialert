package np.com.sampurnasimkhada.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity — represents one row in the "medicines" table.
 * Times are stored as a comma-separated string (e.g. "08:00,20:00")
 * because Room does not natively support List<String> columns.
 */
@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Display name, e.g. "Paracetamol" */
    val name: String,

    /** Dosage string, e.g. "500mg" */
    val dosage: String,

    /** One of: "ONCE", "TWICE", "THRICE", "CUSTOM" */
    val frequency: String,

    /** Comma-separated HH:mm times, e.g. "08:00,20:00" */
    val times: String,

    /** Hex colour chosen by the user, e.g. "#6C63FF" */
    val colorHex: String = "#6C63FF",

    /** Optional free-text notes */
    val notes: String = "",

    /** yyyy-MM-dd */
    val startDate: String,

    /** yyyy-MM-dd, nullable = ongoing */
    val endDate: String? = null,

    /** Timestamp of record creation */
    val createdAt: Long = System.currentTimeMillis(),
)
