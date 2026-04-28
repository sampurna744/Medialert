package np.com.sampurnasimkhada.data.repository

import kotlinx.coroutines.flow.Flow
import np.com.sampurnasimkhada.data.preferences.AppPreferencesDataSource

/** Re-export the snapshot type so callers don't import the data-source. */
typealias AppSettings = AppPreferencesDataSource.AppSettings

/**
 * Repository 3 — User Settings (backed by DataStore).
 *
 * Thin wrapper around [AppPreferencesDataSource] that keeps ViewModels
 * fully decoupled from the DataStore implementation.  If you ever swap
 * DataStore for SharedPreferences or a remote config, only this class
 * and [AppPreferencesDataSource] need to change.
 *
 * @param dataSource  The DataStore-backed preferences source.
 */
class SettingsRepository(
    private val dataSource: AppPreferencesDataSource,
) {

    // ── Read ──────────────────────────────────────────────

    /**
     * Observe all settings as a single snapshot.
     * Emits the current value immediately, then on every change.
     *
     * Typical usage in a ViewModel:
     * ```kotlin
     * val settings: StateFlow<AppSettings> = settingsRepository.settings
     *     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())
     * ```
     */
    val settings: Flow<AppSettings> = dataSource.settings

    // ── Write ─────────────────────────────────────────────

    /**
     * Toggle dark / light mode.
     * The [MainActivity] collects [settings] and passes [AppSettings.darkMode]
     * to [MediAlertTheme] so the change is reflected immediately.
     */
    suspend fun setDarkMode(enabled: Boolean) =
        dataSource.setDarkMode(enabled)

    /**
     * Enable or disable all push notifications.
     * The alarm scheduler checks this flag before firing a notification.
     */
    suspend fun setNotificationsEnabled(enabled: Boolean) =
        dataSource.setNotificationsEnabled(enabled)

    /**
     * Set the snooze duration in minutes (valid values: 5, 10, 15, 20).
     * The AlarmReceiver reads this when the user taps "Snooze".
     */
    suspend fun setSnoozeMinutes(minutes: Int) {
        require(minutes in 1..60) { "Snooze duration must be between 1 and 60 minutes" }
        dataSource.setSnoozeMinutes(minutes)
    }

    /** Enable or disable the notification sound. */
    suspend fun setSoundEnabled(enabled: Boolean) =
        dataSource.setSoundEnabled(enabled)

    /** Mark the onboarding flow as completed so it is not shown again. */
    suspend fun setOnboardingDone(done: Boolean) =
        dataSource.setOnboardingDone(done)
}
