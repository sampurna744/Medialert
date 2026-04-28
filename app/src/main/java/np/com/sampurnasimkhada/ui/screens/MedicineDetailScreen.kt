package np.com.sampurnasimkhada.ui
import android.app.Activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import np.com.sampurnasimkhada.ui.components.*
import np.com.sampurnasimkhada.ui.theme.*
import np.com.sampurnasimkhada.util.*
import np.com.sampurnasimkhada.viewmodel.MedicineDetailViewModel
import np.com.sampurnasimkhada.viewmodel.ViewModelFactory

@Composable
fun MedicineDetailScreen(
    medicineId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAskChat: () -> Unit,
    vm: MedicineDetailViewModel = viewModel(factory = ViewModelFactory((LocalContext.current as Activity).application)),
) {
    val state by vm.uiState.collectAsState()
    LaunchedEffect(medicineId) { vm.load(medicineId) }

    val med = state.medicine
    val medColor = med?.let { runCatching { Color(android.graphics.Color.parseColor(it.colorHex)) }.getOrDefault(Primary) } ?: Primary
    val tabs = listOf("📋 Info", "⏰ Schedule", "🤖 AI Info")

    Scaffold(containerColor = BgDeep) { pad ->
        if (med == null && state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
            return@Scaffold
        }
        if (med == null) return@Scaffold

        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(bottom = 32.dp)) {

            // Hero
            item {
                Box(modifier = Modifier.fillMaxWidth()
                    .background(Brush.linearGradient(listOf(medColor.copy(.3f), BgDeep)))
                    .padding(20.dp)) {
                    Column {
                        OutlinedButton(onClick = onBack, shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.dp, Color.Transparent),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(.1f), contentColor = TextSecondary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                            Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Back", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(16.dp))
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(modifier = Modifier.size(64.dp), shape = RoundedCornerShape(20.dp), color = medColor.copy(.2f), border = BorderStroke(2.dp, medColor.copy(.4f))) {
                                Box(contentAlignment = Alignment.Center) { Text("💊", fontSize = 28.sp) }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(med.name, style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                            Text("${med.dosage} · ${med.frequency.toFrequency().label}", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                            Spacer(Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(onClick = onEdit, border = BorderStroke(1.dp, BorderFaint), colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("Edit")
                                }
                                OutlinedButton(onClick = { vm.delete(medicineId, onBack) }, border = BorderStroke(1.dp, Danger.copy(.3f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger)) {
                                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("Delete")
                                }
                            }
                        }
                    }
                }
            }

            // Tabs
            item {
                TabRow(selectedTabIndex = state.selectedTab, containerColor = BgDeep, contentColor = medColor,
                    indicator = { pos -> TabRowDefaults.SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(pos[state.selectedTab]), color = medColor) }) {
                    tabs.forEachIndexed { i, title ->
                        Tab(selected = state.selectedTab == i, onClick = { vm.selectTab(i) },
                            selectedContentColor = medColor, unselectedContentColor = TextMuted,
                            text = { Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold) })
                    }
                }
            }

            // Tab content
            item {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when (state.selectedTab) {
                        0 -> { // Info
                            if (med.notes.isNotBlank()) {
                                MediCard { SectionLabel("Notes"); Spacer(Modifier.height(6.dp)); Text(med.notes, style = MaterialTheme.typography.bodyMedium, color = TextSecondary) }
                            }
                            MediCard {
                                SectionLabel("Details"); Spacer(Modifier.height(8.dp))
                                listOf("Dosage" to med.dosage, "Frequency" to med.frequency.toFrequency().label, "Start" to med.startDate, "End" to (med.endDate ?: "Ongoing"))
                                    .forEach { (k, v) ->
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(k, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                            Text(v, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                        }
                                        HorizontalDivider(color = BorderFaint, thickness = 0.5.dp)
                                    }
                            }
                            Button(onClick = onAskChat, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884))) {
                                Text("🤖 Ask Assistant about this medicine", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        1 -> { // Schedule
                            med.timesList().forEachIndexed { i, t ->
                                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = BgSurface, border = BorderStroke(1.dp, BorderFaint)) {
                                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                        Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = medColor.copy(.15f)) {
                                            Box(contentAlignment = Alignment.Center) { Text(groupIcon(timeGroup(t)), fontSize = 18.sp) }
                                        }
                                        Column(Modifier.weight(1f)) {
                                            Text(fmtTime(t), style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                                            Text("${timeGroup(t)} dose${if (i > 0) " #${i+1}" else ""}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                        }
                                        StatusBadge("Dose ${i+1}", medColor.copy(.15f), medColor)
                                    }
                                }
                            }
                        }
                        2 -> { // AI Info
                            when {
                                state.aiLoading -> Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(color = Primary); Spacer(Modifier.height(12.dp)); Text("Loading AI info…", color = TextMuted, style = MaterialTheme.typography.bodySmall) }
                                }
                                state.aiError -> Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("⚠️", fontSize = 32.sp); Spacer(Modifier.height(8.dp)); Text("Could not load AI info", color = Danger)
                                    Spacer(Modifier.height(12.dp)); Button(onClick = { vm.loadAi() }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("Try again") }
                                }
                                state.aiInfo != null -> {
                                    val info = state.aiInfo!!
                                    listOf(Triple("USES", info.uses, Primary), Triple("SIDE EFFECTS", info.sideEffects, Warning), Triple("WARNINGS", info.warnings, Danger))
                                        .filter { (_, items, _) -> items.isNotEmpty() }
                                        .forEach { (title, items, color) ->
                                            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = BgSurface, border = BorderStroke(1.dp, BorderFaint)) {
                                                Row {
                                                    Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(color).clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)))
                                                    Column(modifier = Modifier.padding(14.dp)) {
                                                        SectionLabel(title); Spacer(Modifier.height(8.dp))
                                                        items.forEach { item -> Row(modifier = Modifier.padding(vertical = 3.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                            Box(modifier = Modifier.size(5.dp).offset(y = 5.dp).clip(RoundedCornerShape(3.dp)).background(color))
                                                            Text(item, style = MaterialTheme.typography.bodySmall, color = TextSecondary, lineHeight = 18.sp)
                                                        }}
                                                    }
                                                }
                                            }
                                        }
                                    if (info.tip.isNotBlank()) {
                                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color(0xFF0D1A2A)) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text("💡 TIP", style = MaterialTheme.typography.labelSmall, color = Secondary, fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp))
                                                Text(info.tip, style = MaterialTheme.typography.bodySmall, color = Color(0xFFAADDCC), lineHeight = 18.sp)
                                            }
                                        }
                                    }
                                    MedicalDisclaimer()
                                }
                                else -> Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                                    Button(onClick = { vm.loadAi() }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("Load AI Information") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}