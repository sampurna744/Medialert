package np.com.sampurnasimkhada

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.navigation.compose.*
import np.com.sampurnasimkhada.navigation.MediAlertNavGraph
import np.com.sampurnasimkhada.navigation.Route
import np.com.sampurnasimkhada.navigation.bottomNavRoutes
import np.com.sampurnasimkhada.ui.theme.*

private data class NavTab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    NavTab(Route.Home.path,         "Home",      Icons.Filled.Home),
    NavTab(Route.MedicineList.path, "Medicines", Icons.Filled.MedicalServices),
    NavTab(Route.Chat.path,         "Assistant", Icons.Filled.AutoAwesome),
)

class MainActivity : ComponentActivity() {

    // Launcher for POST_NOTIFICATIONS permission (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* result handled silently — user can enable later in Settings */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestRequiredPermissions()
        enableEdgeToEdge()
        setContent {
            MedialertTheme(darkTheme = true) {
                val navController = rememberNavController()
                val backStack by navController.currentBackStackEntryAsState()
                val currentRoute = backStack?.destination?.route

                Scaffold(
                    containerColor = BgDeep,
                    bottomBar = {
                        if (currentRoute in bottomNavRoutes) {
                            NavigationBar(containerColor = BgCard, contentColor = Primary) {
                                tabs.forEach { tab ->
                                    NavigationBarItem(
                                        selected  = currentRoute == tab.route,
                                        onClick   = {
                                            navController.navigate(tab.route) {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState    = true
                                            }
                                        },
                                        label  = { Text(tab.label) },
                                        icon   = { Icon(tab.icon, tab.label) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor   = Primary,
                                            selectedTextColor   = Primary,
                                            indicatorColor      = Primary.copy(.15f),
                                            unselectedIconColor = TextMuted,
                                            unselectedTextColor = TextMuted,
                                        ),
                                    )
                                }
                            }
                        }
                    },
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MediAlertNavGraph(navController)
                    }
                }
            }
        }
    }

    /**
     * Request all runtime permissions the app needs:
     *   - POST_NOTIFICATIONS  (Android 13+ / API 33)
     *   - SCHEDULE_EXACT_ALARM via the system settings page (Android 12+ / API 31)
     *     This permission cannot be requested via the normal launcher — the user must
     *     be sent to the Alarms & Reminders settings screen.
     */
    private fun requestRequiredPermissions() {
        // POST_NOTIFICATIONS — Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // SCHEDULE_EXACT_ALARM — Android 12+
        // canScheduleExactAlarms() returns false until the user grants it from settings.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }
}