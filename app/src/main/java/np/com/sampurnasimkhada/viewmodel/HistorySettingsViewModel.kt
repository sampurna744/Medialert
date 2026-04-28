package np.com.sampurnasimkhada.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import np.com.sampurnasimkhada.data.local.entity.DoseLogEntity
import np.com.sampurnasimkhada.data.repository.MedicineRepository
import np.com.sampurnasimkhada.data.repository.SettingsRepository
import np.com.sampurnasimkhada.data.repository.AppSettings
import np.com.sampurnasimkhada.util.today

// ── HistoryViewModel ─────────────────────────────────────

data class HistoryUiState(
    val doses: List<DoseLogEntity> = emptyList(),
    val takenCount: Int = 0,
    val missedCount: Int = 0,
    val upcomingCount: Int = 0,
)

class HistoryViewModel(repo: MedicineRepository) : ViewModel() {
    val uiState: StateFlow<HistoryUiState> = repo.observeDosesForDate(today())
        .map { doses ->
            HistoryUiState(
                doses         = doses.sortedBy { it.scheduledTime },
                takenCount    = doses.count { it.status == "TAKEN" },
                missedCount   = doses.count { it.status == "MISSED" },
                upcomingCount = doses.count { it.status == "UPCOMING" },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())
}

// ── SettingsViewModel ─────────────────────────────────────

class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() {
    val settings = repo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setDarkMode(v: Boolean)         { viewModelScope.launch { repo.setDarkMode(v) } }
    fun setNotifications(v: Boolean)    { viewModelScope.launch { repo.setNotificationsEnabled(v) } }
    fun setSnooze(minutes: Int)         { viewModelScope.launch { repo.setSnoozeMinutes(minutes) } }
    fun setSound(v: Boolean)            { viewModelScope.launch { repo.setSoundEnabled(v) } }
}
