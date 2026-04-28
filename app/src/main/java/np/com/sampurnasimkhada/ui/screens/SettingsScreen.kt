package np.com.sampurnasimkhada.ui
import android.app.Activity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import np.com.sampurnasimkhada.ui.components.*
import np.com.sampurnasimkhada.ui.theme.*
import np.com.sampurnasimkhada.viewmodel.SettingsViewModel
import np.com.sampurnasimkhada.viewmodel.ViewModelFactory

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = viewModel(factory = ViewModelFactory((LocalContext.current as Activity).application)),
) {
    val settings by vm.settings.collectAsState()

    Scaffold(containerColor = BgDeep, topBar = { MediAlertTopBar("Settings", onBack = onBack) }) { pad ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {

            item { SectionLabel("Appearance") }
            item {
                Surface(shape = RoundedCornerShape(14.dp), color = BgSurface) {
                    ToggleRow("Dark Mode", settings.darkMode, vm::setDarkMode)
                }
            }

            item { SectionLabel("Notifications") }
            item {
                Surface(shape = RoundedCornerShape(14.dp), color = BgSurface) {
                    Column {
                        ToggleRow("Enable Reminders", settings.notificationsEnabled, vm::setNotifications)
                        HorizontalDivider(color = BorderFaint, thickness = 0.5.dp)
                        ToggleRow("Sound", settings.soundEnabled, vm::setSound)
                    }
                }
            }

            item { SectionLabel("Snooze Duration") }
            item {
                Surface(shape = RoundedCornerShape(14.dp), color = BgSurface) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5, 10, 15, 20).forEach { n ->
                                val sel = settings.snoozeMinutes == n
                                OutlinedButton(onClick = { vm.setSnooze(n) }, modifier = Modifier.weight(1f),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, if (sel) Primary else BorderFaint),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = if (sel) Primary else BgSurface, contentColor = if (sel) Color.White else TextMuted),
                                    contentPadding = PaddingValues(vertical = 8.dp)) {
                                    Text("${n}m", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }

            item { SectionLabel("About") }
            item {
                Surface(shape = RoundedCornerShape(14.dp), color = BgSurface) {
                    Column {
                        listOf("Version" to "1.0.0", "Developer" to "Sampurna Simkhada", "University" to "British College").forEachIndexed { i, (k, v) ->
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
                                Text(k, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                                Text(v, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                            }
                            if (i < 2) HorizontalDivider(color = BorderFaint, thickness = 0.5.dp)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item { MedicalDisclaimer() }
        }
    }
}