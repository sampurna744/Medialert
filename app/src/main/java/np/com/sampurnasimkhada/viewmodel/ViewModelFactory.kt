package np.com.sampurnasimkhada.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import np.com.sampurnasimkhada.BuildConfig
import np.com.sampurnasimkhada.data.local.database.MediAlertDatabase
import np.com.sampurnasimkhada.data.preferences.AppPreferencesDataSource
import np.com.sampurnasimkhada.data.remote.api.GroqRetrofitFactory
import np.com.sampurnasimkhada.data.repository.GroqRepository
import np.com.sampurnasimkhada.data.repository.MedicineRepository
import np.com.sampurnasimkhada.data.repository.SettingsRepository


/**
 * Manual ViewModelProvider.Factory that wires repositories into ViewModels.
 */
class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    private val appContext: Context get() = application.applicationContext

    private val db by lazy { MediAlertDatabase.getInstance(appContext) }

    private val medicineRepo by lazy {
        MedicineRepository(db.medicineDao(), db.doseLogDao())
    }



    private val groqRepo by lazy {
        GroqRepository(
            api = GroqRetrofitFactory.create(BuildConfig.GROQ_API_KEY),
        )
    }

    private val settingsRepo by lazy {
        SettingsRepository(AppPreferencesDataSource(appContext))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) ->
            HomeViewModel(medicineRepo) as T

        modelClass.isAssignableFrom(MedicineListViewModel::class.java) ->
            MedicineListViewModel(medicineRepo) as T

        modelClass.isAssignableFrom(AddEditMedicineViewModel::class.java) ->
            AddEditMedicineViewModel(application, medicineRepo) as T

        modelClass.isAssignableFrom(MedicineDetailViewModel::class.java) ->
            MedicineDetailViewModel(medicineRepo, groqRepo) as T

        modelClass.isAssignableFrom(ChatViewModel::class.java) ->
            ChatViewModel(groqRepo, medicineRepo) as T

        modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
            HistoryViewModel(medicineRepo) as T

        modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
            SettingsViewModel(settingsRepo) as T

        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}