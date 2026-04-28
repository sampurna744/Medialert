package np.com.sampurnasimkhada.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import np.com.sampurnasimkhada.data.local.entity.MedicineEntity
import np.com.sampurnasimkhada.data.remote.dto.MedicineInfoDto
import np.com.sampurnasimkhada.data.repository.GroqRepository
import np.com.sampurnasimkhada.data.repository.MedicineRepository

data class DetailUiState(
    val medicine: MedicineEntity? = null,
    val selectedTab: Int = 0,
    val aiInfo: MedicineInfoDto? = null,
    val aiLoading: Boolean = false,
    val aiError: Boolean = false,
    val isLoading: Boolean = true,
)

class MedicineDetailViewModel(
    private val medicineRepo: MedicineRepository,
    private val aiRepo: GroqRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _state.asStateFlow()

    fun load(id: Long) {
        viewModelScope.launch {
            val med = medicineRepo.getMedicineById(id)
            _state.update { it.copy(medicine = med, isLoading = false) }
        }
    }

    fun selectTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
        if (index == 2 && _state.value.aiInfo == null && !_state.value.aiLoading) loadAi()
    }

    fun loadAi() {
        val med = _state.value.medicine ?: return
        _state.update { it.copy(aiLoading = true, aiError = false) }
        viewModelScope.launch {
            aiRepo.getMedicineInfo(med.name, med.dosage)
                .onSuccess { info -> _state.update { it.copy(aiInfo = info, aiLoading = false) } }
                .onFailure { _state.update { it.copy(aiError = true, aiLoading = false) } }
        }
    }

    fun delete(id: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            medicineRepo.deleteDosesForMedicine(id)
            medicineRepo.deleteMedicine(id)
            onDone()
        }
    }
}
