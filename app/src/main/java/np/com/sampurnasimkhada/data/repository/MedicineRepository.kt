package np.com.sampurnasimkhada.data.repository

import kotlinx.coroutines.flow.Flow
import np.com.sampurnasimkhada.data.local.dao.DoseLogDao
import np.com.sampurnasimkhada.data.local.dao.MedicineDao
import np.com.sampurnasimkhada.data.local.entity.DoseLogEntity
import np.com.sampurnasimkhada.data.local.entity.MedicineEntity

/**
 * Repository 1 — Medicine & Dose-Log data.
 *
 * This is the single source of truth for all medicine and dose
 * information.  The ViewModel layer talks **only** to this class
 * and never touches the DAOs directly.
 *
 * Mapping between the entity layer and any domain/UI models happens
 * here so that the rest of the app stays decoupled from Room.
 *
 * @param medicineDao  DAO injected (or passed directly in tests).
 * @param doseLogDao   DAO injected (or passed directly in tests).
 */
class MedicineRepository(
    private val medicineDao: MedicineDao,
    private val doseLogDao: DoseLogDao,
) {

    // ─────────────────────────────────────────────────────
    // Medicine CRUD
    // ─────────────────────────────────────────────────────

    /**
     * Live list of all medicines ordered alphabetically.
     * Compose UI observes this and recomposes on every DB change.
     */
    fun observeAllMedicines(): Flow<List<MedicineEntity>> =
        medicineDao.observeAllMedicines()

    /**
     * One-shot fetch — used when you need the list synchronously,
     * e.g. when rescheduling alarms after a device reboot.
     */
    suspend fun getAllMedicines(): List<MedicineEntity> =
        medicineDao.getAllMedicines()

    /** Fetch a single medicine by id, or null if not found. */
    suspend fun getMedicineById(id: Long): MedicineEntity? =
        medicineDao.getMedicineById(id)

    /**
     * Persist a new medicine.
     * @return the auto-generated row id, which you should pass to
     *         [AlarmScheduler] to schedule the first alarm.
     */
    suspend fun insertMedicine(medicine: MedicineEntity): Long =
        medicineDao.insertMedicine(medicine)

    /** Update all fields of an existing medicine row. */
    suspend fun updateMedicine(medicine: MedicineEntity) =
        medicineDao.updateMedicine(medicine)

    /**
     * Delete a medicine by id.
     * The FK cascade in [DoseLogEntity] automatically removes all
     * associated dose-log rows — no extra cleanup needed here.
     */
    suspend fun deleteMedicine(id: Long) =
        medicineDao.deleteMedicineById(id)

    /** Live search — emits a new list whenever the query or DB changes. */
    fun searchMedicines(query: String): Flow<List<MedicineEntity>> =
        medicineDao.searchMedicines(query)

    // ─────────────────────────────────────────────────────
    // Dose-log operations
    // ─────────────────────────────────────────────────────

    /**
     * Observe all dose slots for a given date (e.g. today).
     * Used by the Home screen to build the schedule list.
     */
    fun observeDosesForDate(date: String): Flow<List<DoseLogEntity>> =
        doseLogDao.observeLogsForDate(date)

    /**
     * Observe the full history for a single medicine.
     * Used by the Medicine Detail / History tab.
     */
    fun observeDoseHistoryForMedicine(medicineId: Long): Flow<List<DoseLogEntity>> =
        doseLogDao.observeLogsForMedicine(medicineId)

    /** Observe every dose log across all medicines (History screen). */
    fun observeAllDoseLogs(): Flow<List<DoseLogEntity>> =
        doseLogDao.observeAllLogs()

    /** Reactive count of TAKEN doses for a given date — drives the progress ring. */
    fun observeTakenCountForDate(date: String): Flow<Int> =
        doseLogDao.observeTakenCountForDate(date)

    /**
     * Seed today's dose slots for a medicine.
     * Call this right after [insertMedicine] and whenever a schedule changes.
     * Uses IGNORE conflict strategy so existing rows are not overwritten.
     */
    suspend fun seedDoseSlotsForToday(
        medicineId: Long,
        times: List<String>,
        date: String,
    ) {
        val logs = times.map { time ->
            DoseLogEntity(
                medicineId    = medicineId,
                scheduledDate = date,
                scheduledTime = time,
                status        = "UPCOMING",
            )
        }
        doseLogDao.insertDoseLogs(logs)
    }

    /**
     * Mark one dose slot as TAKEN.
     * Passing [actionTimestamp] = null records the action at the current time.
     */
    suspend fun markDoseTaken(
        medicineId: Long,
        date: String,
        time: String,
        timestamp: Long = System.currentTimeMillis(),
    ) = doseLogDao.updateStatus(medicineId, date, time, "TAKEN", timestamp)

    /** Mark one dose slot as MISSED. */
    suspend fun markDoseMissed(medicineId: Long, date: String, time: String) =
        doseLogDao.updateStatus(medicineId, date, time, "MISSED", null)

    /** Mark one dose slot as SNOOZED. */
    suspend fun markDoseSnoozed(medicineId: Long, date: String, time: String) =
        doseLogDao.updateStatus(medicineId, date, time, "SNOOZED", null)

    /**
     * Sweep all overdue UPCOMING slots and mark them MISSED.
     * Call this whenever the app resumes or a WorkManager job fires.
     */
    suspend fun sweepOverdueDoses(date: String, currentTime: String) {
        val overdue = doseLogDao.getOverdueLogs(date, currentTime)
        overdue.forEach { log ->
            doseLogDao.updateStatus(log.medicineId, log.scheduledDate, log.scheduledTime, "MISSED", null)
        }
    }

    /** Remove logs older than [beforeDate] (periodic housekeeping). */
    suspend fun deleteOldLogs(beforeDate: String) =
        doseLogDao.deleteOldLogs(beforeDate)

    /**
     * Delete all doses for a specific medicine.
     */
    suspend fun deleteDosesForMedicine(medicineId: Long) =
        doseLogDao.deleteLogsForMedicine(medicineId)

    /** Alias used by ViewModels */
    suspend fun updateDoseStatus(medicineId: Long, date: String, time: String, status: String) =
        doseLogDao.updateStatus(medicineId, date, time, status, null)
}