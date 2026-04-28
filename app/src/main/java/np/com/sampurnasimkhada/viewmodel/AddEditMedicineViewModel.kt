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
    val name: String      = "",
    val dosage: String    = "",
    val frequency: Frequency = Frequency.ONCE,
    val times: List<String>  = listOf("08:00"),
    val colorHex: String  = "#6C63FF",
    val notes: String     = "",
    val startDate: String = today(),
    val endDate: String   = "",
    val nameError: String?   = null,
    val dosageError: String? = null,
    val isSaved: Boolean  = false,
    val isEditMode: Boolean = false,
)

class AddEditMedicineViewModel(
    application: Application,
    private val repo: MedicineRepository
) : AndroidViewModel(application) {

    private val alarmScheduler = AlarmScheduler(application)

    var state by mutableStateOf(AddEditUiState())
        private set

    fun loadForEdit(id: Long) {
        viewModelScope.launch {
            val med = repo.getMedicineById(id) ?: return@launch
            val freq = med.frequency.toFrequency()
            state = state.copy(
                isEditMode = true,
                name       = med.name,
                dosage     = med.dosage,
                frequency  = freq,
                times      = med.timesList(),
                colorHex   = med.colorHex,
                notes      = med.notes,
                startDate  = med.startDate,
                endDate    = med.endDate ?: "",
            )
        }
    }

    fun onName(v: String)      { state = state.copy(name = v, nameError = null) }
    fun onDosage(v: String)    { state = state.copy(dosage = v, dosageError = null) }
    fun onNotes(v: String)     { state = state.copy(notes = v) }
    fun onColor(v: String)     { state = state.copy(colorHex = v) }
    fun onStartDate(v: String) { state = state.copy(startDate = v) }
    fun onEndDate(v: String)   { state = state.copy(endDate = v) }

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
        var hasError = false
        if (s.name.isBlank())   { state = state.copy(nameError   = "Name is required");   hasError = true }
        if (s.dosage.isBlank()) { state = state.copy(dosageError = "Dosage is required"); hasError = true }
        if (hasError) return

        viewModelScope.launch {
            val entity = MedicineEntity(
                id        = editingId ?: 0L,
                name      = s.name.trim(),
                dosage    = s.dosage.trim(),
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
            
            // Cancel existing alarms if editing
            if (editingId != null) {
                alarmScheduler.cancelAlarmsForMedicine(updatedEntity)
            }
            
            // Schedule new alarms
            alarmScheduler.scheduleAlarmsForMedicine(updatedEntity)

            // Seed today's dose slots for new medicines
            if (editingId == null) {
                repo.seedDoseSlotsForToday(savedId, s.times, today())
            }
            state = state.copy(isSaved = true)
        }
    }
}
