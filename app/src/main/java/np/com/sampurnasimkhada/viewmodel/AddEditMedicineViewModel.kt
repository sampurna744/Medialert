package np.com.sampurnasimkhada.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import np.com.sampurnasimkhada.data.local.entity.MedicineEntity
import np.com.sampurnasimkhada.data.repository.MedicineRepository
import np.com.sampurnasimkhada.util.*

data class AddEditUiState(
    val name: String         = "",
    val dosageMgIndex: Int   = DOSAGE_VALUES.indexOf(500),
    val frequency: Frequency = Frequency.ONCE,
    val times: List<String>  = listOf("08:00"),
    val colorHex: String     = "#6C63FF",
    val notes: String        = "",
    val startDate: String    = today(),
    val endDate: String      = "",
    val nameError: String?   = null,
    val isSaved: Boolean     = false,
    val isEditMode: Boolean  = false,
)

class AddEditMedicineViewModel(
    application: Application,
    private val repo: MedicineRepository,
) : AndroidViewModel(application) {

    private val alarmScheduler = AlarmScheduler(application)

    var state by mutableStateOf(AddEditUiState())
        private set

    fun loadForEdit(id: Long) {
        viewModelScope.launch {
            val med = repo.getMedicineById(id) ?: return@launch
            state = state.copy(
                isEditMode    = true,
                name          = med.name,
                dosageMgIndex = dosageIndexFor(parseDosageAmount(med.dosage)),
                frequency     = med.frequency.toFrequency(),
                times         = med.timesList(),
                colorHex      = med.colorHex,
                notes         = med.notes,
                startDate     = med.startDate,
                endDate       = med.endDate ?: "",
            )
        }
    }

    fun onName(v: String)       { state = state.copy(name = v, nameError = null) }
    fun onDosageMgIndex(v: Int) { state = state.copy(dosageMgIndex = v.coerceIn(0, DOSAGE_VALUES.lastIndex)) }
    fun onNotes(v: String)      { state = state.copy(notes = v) }
    fun onColor(v: String)          { state = state.copy(colorHex = v) }
    fun onStartDate(v: String)      { state = state.copy(startDate = v) }
    fun onEndDate(v: String)        { state = state.copy(endDate = v) }

    fun onFrequency(f: Frequency) {
        state = state.copy(frequency = f, times = f.defaultTimes.toMutableList())
    }

    fun onTime(index: Int, value: String) {
        val t = state.times.toMutableList()
        if (index < t.size) t[index] = value else t.add(value)
        state = state.copy(times = t)
    }

    fun save(editingId: Long?) {
        val s = state
        if (s.name.isBlank()) {
            state = state.copy(nameError = "Name is required")
            return
        }

        val dosage = "${DOSAGE_VALUES[s.dosageMgIndex]} mg"

        viewModelScope.launch {
            val entity = MedicineEntity(
                id        = editingId ?: 0L,
                name      = s.name.trim(),
                dosage    = dosage,
                frequency = s.frequency.name,
                times     = s.times.toTimesString(),
                colorHex  = s.colorHex,
                notes     = s.notes.trim(),
                startDate = s.startDate,
                endDate   = s.endDate.ifBlank { null },
            )
            val savedId = if (editingId != null) {
                repo.updateMedicine(entity)
                editingId
            } else {
                repo.insertMedicine(entity)
            }
            val updatedEntity = entity.copy(id = savedId)
            if (editingId != null) alarmScheduler.cancelAlarmsForMedicine(updatedEntity)
            alarmScheduler.scheduleAlarmsForMedicine(updatedEntity)
            // Always reseed today's slots — for new medicines this seeds fresh rows,
            // for edits it removes stale UPCOMING slots and adds the new times.
            repo.reseedDoseSlotsForDate(savedId, s.times, today())
            state = state.copy(isSaved = true)
        }
    }
}
