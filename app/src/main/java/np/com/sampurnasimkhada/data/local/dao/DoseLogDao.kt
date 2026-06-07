package np.com.sampurnasimkhada.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import np.com.sampurnasimkhada.data.local.entity.DoseLogEntity

/**
 * DAO for reading and writing dose-log rows.
 *
 * Dose logs are created when a medicine is saved (one row per
 * scheduled time slot per day) and updated when the user marks a
 * dose as taken, snoozes it, or the alarm fires and marks it missed.
 */
@Dao
interface DoseLogDao {

    // ── INSERT ────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLog(log: DoseLogEntity): Long

    /** Bulk-insert for seeding today's schedule. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDoseLogs(logs: List<DoseLogEntity>)

    // ── UPDATE ────────────────────────────────────────────

    /**
     * Update the status (and optional action timestamp) of one specific slot.
     * This is the primary write path after a user taps "Taken" or "Snooze".
     */
    @Query("""
        UPDATE dose_logs
        SET    status = :status,
               actionTimestamp = :actionTimestamp
        WHERE  medicineId    = :medicineId
          AND  scheduledDate = :date
          AND  scheduledTime = :time
    """)
    suspend fun updateStatus(
        medicineId: Long,
        date: String,
        time: String,
        status: String,
        actionTimestamp: Long? = System.currentTimeMillis(),
    )

    // ── DELETE ────────────────────────────────────────────

    /** Remove all logs for a medicine (called before deleting the medicine). */
    @Query("DELETE FROM dose_logs WHERE medicineId = :medicineId")
    suspend fun deleteLogsForMedicine(medicineId: Long)

    /** Remove only UPCOMING (not yet acted-on) slots for a medicine on a given date.
     *  Called before reseeding when the user edits a medicine's schedule. */
    @Query("DELETE FROM dose_logs WHERE medicineId = :medicineId AND scheduledDate = :date AND status = 'UPCOMING'")
    suspend fun deleteUpcomingLogsForDate(medicineId: Long, date: String)

    /** Remove logs older than a given date (for periodic cleanup). */
    @Query("DELETE FROM dose_logs WHERE scheduledDate < :beforeDate")
    suspend fun deleteOldLogs(beforeDate: String)

    // ── QUERIES ───────────────────────────────────────────

    /** Observe all logs for a specific date, ordered chronologically. */
    @Query("""
        SELECT * FROM dose_logs
        WHERE  scheduledDate = :date
        ORDER  BY scheduledTime ASC
    """)
    fun observeLogsForDate(date: String): Flow<List<DoseLogEntity>>

    /** Observe all logs for a specific medicine (history view). */
    @Query("""
        SELECT * FROM dose_logs
        WHERE  medicineId = :medicineId
        ORDER  BY scheduledDate DESC, scheduledTime DESC
    """)
    fun observeLogsForMedicine(medicineId: Long): Flow<List<DoseLogEntity>>

    /** All logs, newest first — used by the history screen. */
    @Query("SELECT * FROM dose_logs ORDER BY scheduledDate DESC, scheduledTime DESC")
    fun observeAllLogs(): Flow<List<DoseLogEntity>>

    /** Count of TAKEN doses for a given date — used in the progress ring. */
    @Query("SELECT COUNT(*) FROM dose_logs WHERE scheduledDate = :date AND status = 'TAKEN'")
    fun observeTakenCountForDate(date: String): Flow<Int>

    /** All UPCOMING logs whose time has already passed (for missed-dose sweep). */
    @Query("""
        SELECT * FROM dose_logs
        WHERE  status        = 'UPCOMING'
          AND  scheduledDate = :date
          AND  scheduledTime < :currentTime
    """)
    suspend fun getOverdueLogs(date: String, currentTime: String): List<DoseLogEntity>
}
