package np.com.sampurnasimkhada.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import np.com.sampurnasimkhada.data.local.entity.MedicineEntity

/**
 * DAO for CRUD operations on the "medicines" table.
 *
 * All read queries return [Flow] so the UI automatically re-composes
 * whenever the underlying data changes — no manual refresh needed.
 */
@Dao
interface MedicineDao {

    // ── INSERT / UPSERT ───────────────────────────────────

    /**
     * Insert a new medicine. If a row with the same primary key already
     * exists it is replaced (upsert behaviour).
     * @return the newly assigned row id.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity): Long

    // ── UPDATE ────────────────────────────────────────────

    /** Replace all columns of an existing row matched by primary key. */
    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    // ── DELETE ────────────────────────────────────────────

    /** Delete a single entity (matched by primary key). */
    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)

    /**
     * Delete by id — useful when you only have the id, not the full entity.
     * The cascade rule in [DoseLogEntity] will also remove all related logs.
     */
    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteMedicineById(id: Long)

    // ── QUERIES ───────────────────────────────────────────

    /** Observe the full list, sorted alphabetically. */
    @Query("SELECT * FROM medicines ORDER BY name ASC")
    fun observeAllMedicines(): Flow<List<MedicineEntity>>

    /** One-shot fetch of all medicines (e.g. for alarm rescheduling on boot). */
    @Query("SELECT * FROM medicines ORDER BY name ASC")
    suspend fun getAllMedicines(): List<MedicineEntity>

    /** Fetch a single medicine by id. Returns null if not found. */
    @Query("SELECT * FROM medicines WHERE id = :id LIMIT 1")
    suspend fun getMedicineById(id: Long): MedicineEntity?

    /** Search by name (case-insensitive partial match). */
    @Query("SELECT * FROM medicines WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchMedicines(query: String): Flow<List<MedicineEntity>>
}
