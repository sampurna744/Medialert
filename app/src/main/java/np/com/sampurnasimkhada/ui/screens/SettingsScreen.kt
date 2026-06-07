package np.com.sampurnasimkhada.ui
import android.app.Activity

import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    val context = LocalContext.current

    val ringtoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            @Suppress("DEPRECATION")
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            vm.setAlarmSoundUri(uri?.toString() ?: "")
        }
    }

    fun currentSoundName(): String {
        val uri = settings.alarmSoundUri
        if (uri.isBlank()) return "Default Alarm"
        return try {
            RingtoneManager.getRingtone(context, Uri.parse(uri))?.getTitle(context) ?: "Custom Sound"
        } catch (e: Exception) {
            "Custom Sound"
        }
    }

    fun launchRingtonePicker() {
        val intent = android.content.Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound")
            if (settings.alarmSoundUri.isNotBlank()) {
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(settings.alarmSoundUri))
            }
        }
        ringtoneLauncher.launch(intent)
    }

    Scaffold(containerColor = BgDeep, topBar = { MediAlertTopBar("Settings", onBack = onBack) }) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {

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

            item { SectionLabel("Alarm Sound") }
            item {
                Surface(shape = RoundedCornerShape(14.dp), color = BgSurface) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(
                                    Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Column {
                                    Text(
                                        "Ringtone",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextSecondary,
                                    )
                                    Text(
                                        currentSoundName(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                    )
                                }
                            }
                            TextButton(
                                onClick = { launchRingtonePicker() },
                                enabled = settings.soundEnabled,
                            ) {
                                Text(
                                    "Change",
                                    color = if (settings.soundEnabled) Primary else TextMuted,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        if (settings.alarmSoundUri.isNotBlank()) {
                            HorizontalDivider(color = BorderFaint, thickness = 0.5.dp)
                            TextButton(
                                onClick = { vm.setAlarmSoundUri("") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                            ) {
                                Text("Reset to Default", color = TextMuted)
                            }
                        }
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
                                OutlinedButton(
                                    onClick = { vm.setSnooze(n) },
                                    modifier = Modifier.weight(1f),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, if (sel) Primary else BorderFaint),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (sel) Primary else BgSurface,
                                        contentColor = if (sel) Color.White else TextMuted,
                                    ),
                                    contentPadding = PaddingValues(vertical = 8.dp),
                                ) {
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
                        listOf(
                            "Version" to "1.0.0",
                            "Developer" to "Sampurna Simkhada",
                            "University" to "British College",
                        ).forEachIndexed { i, (k, v) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
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
