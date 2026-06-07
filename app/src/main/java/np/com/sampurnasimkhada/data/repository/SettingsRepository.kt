package np.com.sampurnasimkhada.data.repository

import kotlinx.coroutines.flow.Flow
import np.com.sampurnasimkhada.data.preferences.AppPreferencesDataSource

typealias AppSettings = AppPreferencesDataSource.AppSettings

class SettingsRepository(private val dataSource: AppPreferencesDataSource) {

    val settings: Flow<AppSettings> = dataSource.settings

    suspend fun setDarkMode(enabled: Boolean) = dataSource.setDarkMode(enabled)

    suspend fun setNotificationsEnabled(enabled: Boolean) = dataSource.setNotificationsEnabled(enabled)

    suspend fun setSnoozeMinutes(minutes: Int) {
        require(minutes in 1..60) { "Snooze duration must be between 1 and 60 minutes" }
        dataSource.setSnoozeMinutes(minutes)
    }

    suspend fun setSoundEnabled(enabled: Boolean) = dataSource.setSoundEnabled(enabled)

    suspend fun setOnboardingDone(done: Boolean) = dataSource.setOnboardingDone(done)

    suspend fun setAlarmSoundUri(uri: String) = dataSource.setAlarmSoundUri(uri)
}
