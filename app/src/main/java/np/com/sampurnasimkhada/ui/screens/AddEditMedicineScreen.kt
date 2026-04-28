package np.com.sampurnasimkhada.ui
import android.app.Activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import np.com.sampurnasimkhada.ui.components.*
import np.com.sampurnasimkhada.ui.theme.*
import np.com.sampurnasimkhada.util.Frequency
import np.com.sampurnasimkhada.viewmodel.AddEditMedicineViewModel
import np.com.sampurnasimkhada.viewmodel.ViewModelFactory

@Composable
fun AddEditMedicineScreen(
    medicineId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    vm: AddEditMedicineViewModel = viewModel(factory = ViewModelFactory((LocalContext.current as Activity).application)),
) {
    val s = vm.state
    LaunchedEffect(medicineId) { medicineId?.let { vm.loadForEdit(it) } }
    LaunchedEffect(s.isSaved) { if (s.isSaved) onSaved() }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor   = BgSurface, unfocusedContainerColor = BgSurface,
        focusedTextColor        = TextPrimary, unfocusedTextColor    = TextPrimary,
        focusedBorderColor      = Primary, unfocusedBorderColor      = BorderFaint,
        focusedLabelColor       = Primary, unfocusedLabelColor       = TextMuted,
        errorBorderColor        = Danger, errorLabelColor            = Danger,
        cursorColor             = Primary,
    )
    val btnColor = runCatching { Color(android.graphics.Color.parseColor(s.colorHex)) }.getOrDefault(Primary)

    Scaffold(
        containerColor = BgDeep,
        topBar = { MediAlertTopBar(title = if (s.isEditMode) "Edit Medicine" else "Add Medicine", onBack = onBack) },
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Colour picker
            item {
                MediCard {
                    SectionLabel("Pill Colour", modifier = Modifier.padding(0.dp))
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PillColors.forEach { c ->
                            val hex = "#%06X".format((c.value and 0xFFFFFFUL).toLong())
                            Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(c)
                                .then(if (s.colorHex.equals(hex, ignoreCase = true))
                                    Modifier.clip(CircleShape)
                                else Modifier)
                                .clickable { vm.onColor("#%06X".format((c.value and 0xFFFFFFUL).toLong()).uppercase()) }
                            ) {
                                if (s.colorHex.uppercase() == "#%06X".format((c.value and 0xFFFFFFUL).toLong()).uppercase()) {
                                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawCircle(color = Color.White.copy(.6f), radius = size.minDimension / 2f,
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Name
            item {
                OutlinedTextField(value = s.name, onValueChange = vm::onName, label = { Text("Medicine Name") },
                    placeholder = { Text("e.g. Paracetamol") }, singleLine = true,
                    isError = s.nameError != null,
                    supportingText = s.nameError?.let { { Text(it, color = Danger) } },
                    shape = RoundedCornerShape(12.dp), colors = fieldColors, modifier = Modifier.fillMaxWidth())
            }

            // Dosage
            item {
                OutlinedTextField(value = s.dosage, onValueChange = vm::onDosage, label = { Text("Dosage") },
                    placeholder = { Text("e.g. 500mg") }, singleLine = true,
                    isError = s.dosageError != null,
                    supportingText = s.dosageError?.let { { Text(it, color = Danger) } },
                    shape = RoundedCornerShape(12.dp), colors = fieldColors, modifier = Modifier.fillMaxWidth())
            }

            // Frequency
            item {
                Column {
                    SectionLabel("Frequency")
                    Spacer(Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(BgSurface).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        listOf(Frequency.ONCE to "Once", Frequency.TWICE to "Twice", Frequency.THRICE to "3×").forEach { (f, l) ->
                            val sel = s.frequency == f
                            Button(onClick = { vm.onFrequency(f) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(9.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (sel) btnColor else Color.Transparent, contentColor = if (sel) Color.White else TextMuted),
                                contentPadding = PaddingValues(vertical = 10.dp)) {
                                Text(l, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            // Times
            item {
                Column {
                    SectionLabel("Reminder Time${if (s.times.size > 1) "s" else ""}")
                    Spacer(Modifier.height(6.dp))
                    s.times.forEachIndexed { i, t ->
                        Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(listOf("1st","2nd","3rd").getOrElse(i) { "${i+1}th" }, style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.width(24.dp))
                            OutlinedTextField(value = t, onValueChange = { v -> vm.onTime(i, v) }, singleLine = true,
                                placeholder = { Text("HH:MM") }, shape = RoundedCornerShape(12.dp), colors = fieldColors, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Dates
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = s.startDate, onValueChange = vm::onStartDate, label = { Text("Start Date") },
                        placeholder = { Text("yyyy-MM-dd") }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = fieldColors, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = s.endDate, onValueChange = vm::onEndDate, label = { Text("End Date") },
                        placeholder = { Text("Optional") }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = fieldColors, modifier = Modifier.weight(1f))
                }
            }

            // Notes
            item {
                OutlinedTextField(value = s.notes, onValueChange = vm::onNotes, label = { Text("Notes (optional)") },
                    placeholder = { Text("e.g. Take with food") }, minLines = 3, maxLines = 5,
                    shape = RoundedCornerShape(12.dp), colors = fieldColors, modifier = Modifier.fillMaxWidth())
            }

            // Save
            item {
                Button(onClick = { vm.save(medicineId) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = btnColor)) {
                    Text(if (s.isEditMode) "💾 Save Changes" else "💊 Add Medicine", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            item { MedicalDisclaimer() }
        }
    }
}