package np.com.sampurnasimkhada.ui
import android.app.Activity

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import np.com.sampurnasimkhada.data.local.entity.DoseLogEntity
import np.com.sampurnasimkhada.ui.components.*
import np.com.sampurnasimkhada.ui.theme.*
import np.com.sampurnasimkhada.util.*
import np.com.sampurnasimkhada.viewmodel.HomeViewModel
import np.com.sampurnasimkhada.viewmodel.ViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    onAddMedicine: () -> Unit,
    onOpenChat: () -> Unit,
    onMedicineClick: (Long) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    vm: HomeViewModel = viewModel(factory = ViewModelFactory((LocalContext.current as Activity).application)),
) {
    val state by vm.uiState.collectAsState()
    val progress by animateFloatAsState(
        targetValue = if (state.totalCount > 0) state.takenCount.toFloat() / state.totalCount else 0f,
        label = "progress"
    )
    val h   = java.time.LocalTime.now().hour
    val greeting  = when { h < 12 -> "Good morning"; h < 17 -> "Good afternoon"; else -> "Good evening" }
    val dateLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))
    val groups = state.todayDoses.groupBy { timeGroup(it.scheduledTime) }
    val groupOrder = listOf("Morning", "Afternoon", "Evening", "Night")

    Scaffold(
        containerColor = BgDeep,
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMedicine, containerColor = Primary, contentColor = Color.White, shape = CircleShape) {
                Icon(Icons.Default.Add, "Add medicine")
            }
        }
    ) { pad ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(bottom = 24.dp)) {

            // Header
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 20.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column {
                        Text("MediAlert", style = MaterialTheme.typography.labelLarge, color = Primary, letterSpacing = 1.5.sp)
                        Text("$greeting 👋", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                        Text(dateLabel, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    IconButton(onClick = onOpenSettings) { Icon(Icons.Default.Settings, null, tint = TextMuted) }
                }
            }

            // Progress card
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF1C1A4E), Color(0xFF2D2080), Color(0xFF1A3060))))
                    .padding(20.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Today's Progress", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(.55f), letterSpacing = .8.sp)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("${state.takenCount}", style = MaterialTheme.typography.displayLarge, color = Color.White, fontSize = 28.sp)
                                Text("/${state.totalCount} doses", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(.5f), modifier = Modifier.padding(bottom = 4.dp, start = 2.dp))
                            }
                            state.nextDose?.let { next ->
                                Spacer(Modifier.height(6.dp))
                                Text("Next: ${state.medicines.find { m -> m.id == next.medicineId }?.name ?: "—"} at ${fmtTime(next.scheduledTime)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA5F3EB))
                            }
                            if (state.missedCount > 0) {
                                Spacer(Modifier.height(6.dp))
                                Surface(shape = RoundedCornerShape(8.dp), color = Danger.copy(.2f)) {
                                    Text("⚠ ${state.missedCount} missed", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.bodySmall, color = Color(0xFFFFB3B3))
                                }
                            }
                        }
                        ProgressRing(progress = progress, label = "${(progress * 100).toInt()}%", modifier = Modifier.size(72.dp), ringColor = if (progress >= 1f) Secondary else Color.White)
                    }
                }
            }

            // Quick actions
            item {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAction("Add Med", Icons.Default.Add, Primary, onAddMedicine, Modifier.weight(1f))
                    QuickAction("Ask AI", Icons.Default.AutoAwesome, Secondary, onOpenChat, Modifier.weight(1f))
                    QuickAction("History", Icons.Default.History, Danger, onOpenHistory, Modifier.weight(1f))
                }
            }

            // Schedule header
            item { Text("Today's Schedule", style = MaterialTheme.typography.headlineSmall, color = TextPrimary, modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 12.dp)) }

            // Grouped doses
            groupOrder.forEach { grp ->
                val doses = groups[grp] ?: return@forEach
                item {
                    Row(modifier = Modifier.padding(start = 20.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(groupIcon(grp), fontSize = 15.sp)
                        SectionLabel(grp)
                    }
                }
                items(doses) { dose ->
                    val medName = state.medicines.find { it.id == dose.medicineId }?.name ?: "—"
                    val medColor = state.medicines.find { it.id == dose.medicineId }?.colorHex ?: "#6C63FF"
                    DoseCard(dose = dose, medicineName = medName, colorHex = medColor,
                        onToggle = { vm.markTaken(dose) },
                        onClick = { onMedicineClick(dose.medicineId) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            if (state.todayDoses.isEmpty() && !state.isLoading) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💊", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No medicines today", style = MaterialTheme.typography.titleMedium, color = TextMuted)
                        Spacer(Modifier.height(4.dp))
                        Text("Tap + to get started", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun DoseCard(dose: DoseLogEntity, medicineName: String, colorHex: String, onToggle: () -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val taken  = dose.status == "TAKEN"
    val missed = dose.status == "MISSED"
    val doseColor = runCatching { Color(android.graphics.Color.parseColor(colorHex)) }.getOrDefault(Primary)
    val cardBg    = when { taken -> Color(0xFF0D2420); missed -> Color(0xFF22100E); else -> BgSurface }
    val border    = when { taken -> Color(0xFF0D4A3A); missed -> Color(0xFF4A1A18);  else -> BorderFaint }

    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp), color = cardBg,
        border = BorderStroke(1.dp, border)) {
        Row(modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(if (taken) Secondary else doseColor))
            Column(modifier = Modifier.weight(1f)) {
                Text(medicineName, style = MaterialTheme.typography.titleMedium, color = when { taken -> Secondary; missed -> Color(0xFFFF8A80); else -> TextPrimary }, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(fmtTime(dose.scheduledTime), style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            if (missed) StatusBadge("MISSED", Danger.copy(.15f), Danger)
            IconButton(onClick = onToggle, modifier = Modifier.size(32.dp).clip(CircleShape)
                .background(if (taken) Secondary else Color.Transparent)
                .border(2.dp, if (taken) Secondary else BorderFaint, CircleShape)) {
                if (taken) Icon(Icons.Default.Check, null, tint = Color(0xFF0D2420), modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, bg: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = BgSurface, border = BorderStroke(1.dp, BorderFaint), onClick = onClick) {
        Column(modifier = Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(bg), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}