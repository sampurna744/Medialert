package np.com.sampurnasimkhada.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "medialert_settings")

class AppPreferencesDataSource(private val context: Context) {

    private object Keys {
        val DARK_MODE             = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SNOOZE_MINUTES        = intPreferencesKey("snooze_minutes")
        val SOUND_ENABLED         = booleanPreferencesKey("sound_enabled")
        val ONBOARDING_DONE       = booleanPreferencesKey("onboarding_done")
        val ALARM_SOUND_URI       = stringPreferencesKey("alarm_sound_uri")
    }

    data class AppSettings(
        val darkMode: Boolean             = true,
        val notificationsEnabled: Boolean = true,
        val snoozeMinutes: Int            = 5,
        val soundEnabled: Boolean         = true,
        val onboardingDone: Boolean       = false,
        val alarmSoundUri: String         = "",
    )

    val settings: Flow<AppSettings> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            AppSettings(
                darkMode             = prefs[Keys.DARK_MODE]             ?: true,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
                snoozeMinutes        = prefs[Keys.SNOOZE_MINUTES]        ?: 5,
                soundEnabled         = prefs[Keys.SOUND_ENABLED]         ?: true,
                onboardingDone       = prefs[Keys.ONBOARDING_DONE]       ?: false,
                alarmSoundUri        = prefs[Keys.ALARM_SOUND_URI]       ?: "",
            )
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setSnoozeMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.SNOOZE_MINUTES] = minutes }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    suspend fun setAlarmSoundUri(uri: String) {
        context.dataStore.edit { it[Keys.ALARM_SOUND_URI] = uri }
    }
}
