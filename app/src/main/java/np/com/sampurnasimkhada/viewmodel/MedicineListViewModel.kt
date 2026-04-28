package np.com.sampurnasimkhada.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import np.com.sampurnasimkhada.data.local.entity.MedicineEntity
import np.com.sampurnasimkhada.data.repository.MedicineRepository

data class MedicineListUiState(
    val medicines: List<MedicineEntity> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
)

class MedicineListViewModel(private val repo: MedicineRepository) : ViewModel() {

    private val _query = MutableStateFlow("")

    val uiState: StateFlow<MedicineListUiState> = _query
        .debounce(200)
        .flatMapLatest { q ->
            if (q.isBlank()) repo.observeAllMedicines()
            else repo.searchMedicines(q)
        }
        .combine(_query) { meds, q ->
            MedicineListUiState(medicines = meds, query = q, isLoading = false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MedicineListUiState())

    fun onQueryChange(q: String) { _query.value = q }

    fun delete(medicine: MedicineEntity) {
        viewModelScope.launch {
            repo.deleteDosesForMedicine(medicine.id)
            repo.deleteMedicine(medicine.id)
        }
    }
}
