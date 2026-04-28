package np.com.sampurnasimkhada.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import np.com.sampurnasimkhada.data.local.entity.DoseLogEntity
import np.com.sampurnasimkhada.data.local.entity.MedicineEntity
import np.com.sampurnasimkhada.data.repository.MedicineRepository
import np.com.sampurnasimkhada.util.*

data class HomeUiState(
    val todayDoses: List<DoseLogEntity> = emptyList(),
    val medicines: List<MedicineEntity> = emptyList(),
    val takenCount: Int = 0,
    val totalCount: Int = 0,
    val nextDose: DoseLogEntity? = null,
    val missedCount: Int = 0,
    val isLoading: Boolean = true,
)

class HomeViewModel(private val repo: MedicineRepository) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        observeToday()
        sweepMissed()
    }

    private fun observeToday() {
        viewModelScope.launch {
            combine(
                repo.observeDosesForDate(today()),
                repo.observeAllMedicines(),
            ) { doses, meds ->
                val taken    = doses.count { it.status == "TAKEN" }
                val missed   = doses.count { it.status == "MISSED" }
                val upcoming = doses.filter { it.status == "UPCOMING" }
                    .minByOrNull { it.scheduledTime }
                HomeUiState(
                    todayDoses  = doses.sortedBy { it.scheduledTime },
                    medicines   = meds,
                    takenCount  = taken,
                    totalCount  = doses.size,
                    nextDose    = upcoming,
                    missedCount = missed,
                    isLoading   = false,
                )
            }.collect { _state.value = it }
        }
    }

    private fun sweepMissed() {
        viewModelScope.launch {
            repo.sweepOverdueDoses(today(), nowTime())
        }
    }

    fun markTaken(dose: DoseLogEntity) {
        viewModelScope.launch {
            val newStatus = if (dose.status == "TAKEN") "UPCOMING" else "TAKEN"
            repo.updateDoseStatus(dose.medicineId, dose.scheduledDate, dose.scheduledTime, newStatus)
        }
    }
}
