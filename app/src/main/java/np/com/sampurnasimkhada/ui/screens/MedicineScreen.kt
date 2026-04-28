package np.com.sampurnasimkhada.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import np.com.sampurnasimkhada.data.local.entity.MedicineEntity
import np.com.sampurnasimkhada.ui.components.*
import np.com.sampurnasimkhada.ui.theme.*
import np.com.sampurnasimkhada.util.*
import np.com.sampurnasimkhada.viewmodel.MedicineListViewModel
import np.com.sampurnasimkhada.viewmodel.ViewModelFactory

@Composable
fun MedicineListScreen(
    onAddMedicine: () -> Unit,
    onMedicineClick: (Long) -> Unit,
    onEditMedicine: (Long) -> Unit,
    vm: MedicineListViewModel = viewModel(factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)),
) {
    val state by vm.uiState.collectAsState()
    var deleteTarget by remember { mutableStateOf<MedicineEntity?>(null) }

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            MediAlertTopBar(title = "My Medicines", actions = {
                IconButton(onClick = onAddMedicine) { Icon(Icons.Default.Add, null, tint = Primary) }
            })
        },
    ) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad)) {
            OutlinedTextField(
                value = state.query, onValueChange = vm::onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search medicines…", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = BgSurface, unfocusedContainerColor = BgSurface,
                    focusedTextColor        = TextPrimary, unfocusedTextColor    = TextPrimary,
                    focusedBorderColor      = Primary, unfocusedBorderColor      = BorderFaint,
                    cursorColor             = Primary,
                ),
            )
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.medicines, key = { it.id }) { med ->
                    MedRowCard(med = med, onClick = { onMedicineClick(med.id) }, onEdit = { onEditMedicine(med.id) }, onDelete = { deleteTarget = med })
                }
                if (state.medicines.isEmpty() && !state.isLoading) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(8.dp))
                            Text("No medicines found", style = MaterialTheme.typography.titleMedium, color = TextMuted)
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { med ->
        AlertDialog(onDismissRequest = { deleteTarget = null }, containerColor = BgSurface,
            title = { Text("Delete ${med.name}?", color = TextPrimary) },
            text  = { Text("This removes all reminders for this medicine.", color = TextMuted) },
            confirmButton = { Button(onClick = { vm.delete(med); deleteTarget = null }, colors = ButtonDefaults.buttonColors(containerColor = Danger)) { Text("Delete") } },
            dismissButton = { OutlinedButton(onClick = { deleteTarget = null }, colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)) { Text("Cancel") } },
        )
    }
}

@Composable
private fun MedRowCard(med: MedicineEntity, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val c = runCatching { Color(android.graphics.Color.parseColor(med.colorHex)) }.getOrDefault(Primary)
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = BgSurface, border = BorderStroke(1.dp, BorderFaint), onClick = onClick) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(modifier = Modifier.size(42.dp), shape = RoundedCornerShape(12.dp), color = c.copy(.15f), border = BorderStroke(1.dp, c.copy(.3f))) {
                Box(contentAlignment = Alignment.Center) { Text("💊", style = MaterialTheme.typography.headlineSmall) }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(med.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${med.dosage} · ${med.frequency.toFrequency().label}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    med.timesList().forEach { t ->
                        Surface(shape = RoundedCornerShape(6.dp), color = BorderFaint) {
                            Text(fmtTime(t), modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, null, tint = Primary, modifier = Modifier.size(16.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, tint = Danger, modifier = Modifier.size(16.dp)) }
            }
        }
    }
}