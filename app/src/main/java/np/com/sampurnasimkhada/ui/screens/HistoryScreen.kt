package np.com.sampurnasimkhada.ui
import android.app.Activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import np.com.sampurnasimkhada.data.local.entity.DoseLogEntity
import np.com.sampurnasimkhada.ui.components.*
import np.com.sampurnasimkhada.ui.theme.*
import np.com.sampurnasimkhada.util.*
import np.com.sampurnasimkhada.viewmodel.HistoryViewModel
import np.com.sampurnasimkhada.viewmodel.ViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    vm: HistoryViewModel = viewModel(factory = ViewModelFactory((LocalContext.current as Activity).application)),
) {
    val state by vm.uiState.collectAsState()
    val dateLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault()))

    Scaffold(containerColor = BgDeep, topBar = { MediAlertTopBar("Reminder History", onBack = onBack) }) { pad ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Stats
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        Triple("✅", "Taken",    state.takenCount to Secondary to Color(0xFF0D2A20)),
                        Triple("⚠️", "Missed",   state.missedCount to Danger   to Color(0xFF2A1010)),
                        Triple("🔔", "Upcoming", state.upcomingCount to Primary to Color(0xFF1A1035)),
                    ).forEach { (icon, label, data) ->
                        val (countColor, bg) = data
                        val (count, color)   = countColor
                        Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), color = bg, border = BorderStroke(1.dp, color.copy(.3f))) {
                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(icon, fontSize = 20.sp)
                                Text("$count", style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.ExtraBold)
                                Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(.7f))
                            }
                        }
                    }
                }
            }

            item { SectionLabel("Today — $dateLabel") }

            items(state.doses) { dose -> DoseHistoryRow(dose) }

            if (state.doses.isEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 40.sp); Spacer(Modifier.height(8.dp)); Text("No history yet", color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun DoseHistoryRow(dose: DoseLogEntity) {
    val (statusColor, statusBg, statusLabel) = when (dose.status) {
        "TAKEN"    -> Triple(Secondary, Color(0xFF0D2A20), "Taken")
        "MISSED"   -> Triple(Danger,    Color(0xFF2A1010), "Missed")
        else       -> Triple(Primary,   Color(0xFF1A1035), "Upcoming")
    }
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = BgSurface, border = BorderStroke(1.dp, BorderFaint)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ColorDot(hex = "#%06X".format((statusColor.value and 0xFFFFFFUL).toLong()), size = 8.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(dose.medicineId.toString(), style = MaterialTheme.typography.titleMedium, color = statusColor)
                Text(fmtTime(dose.scheduledTime), style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            StatusBadge(statusLabel, statusBg, statusColor)
        }
    }
}