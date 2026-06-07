package np.com.sampurnasimkhada.ui
import android.app.Activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
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
import np.com.sampurnasimkhada.util.DOSAGE_VALUES
import np.com.sampurnasimkhada.util.Frequency
import np.com.sampurnasimkhada.util.fmtTime

import np.com.sampurnasimkhada.viewmodel.AddEditMedicineViewModel
import np.com.sampurnasimkhada.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
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

    // Dialog visibility state
    var timePickerSlot by remember { mutableIntStateOf(-1) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor    = BgSurface, unfocusedContainerColor = BgSurface,
        focusedTextColor         = TextPrimary, unfocusedTextColor    = TextPrimary,
        focusedBorderColor       = Primary, unfocusedBorderColor      = BorderFaint,
        focusedLabelColor        = Primary, unfocusedLabelColor       = TextMuted,
        errorBorderColor         = Danger, errorLabelColor            = Danger,
        cursorColor              = Primary,
    )
    val btnColor = runCatching {
        Color(android.graphics.Color.parseColor(s.colorHex))
    }.getOrDefault(Primary)

    // ── Time picker dialog ────────────────────────────────
    if (timePickerSlot >= 0) {
        val existing = s.times.getOrElse(timePickerSlot) { "08:00" }
        val (initH, initM) = existing.split(":").map { it.toIntOrNull() ?: 0 }
            .let { (it.getOrElse(0) { 8 }) to (it.getOrElse(1) { 0 }) }

        TimePickerDialog(
            initialHour   = initH,
            initialMinute = initM,
            onDismiss     = { timePickerSlot = -1 },
            onConfirm     = { h, m ->
                vm.onTime(timePickerSlot, "%02d:%02d".format(h, m))
                timePickerSlot = -1
            },
        )
    }

    // ── Date picker dialogs ───────────────────────────────
    if (showStartDatePicker) {
        MediDatePickerDialog(
            initialDateStr = s.startDate,
            onDismiss      = { showStartDatePicker = false },
            onConfirm      = { vm.onStartDate(it); showStartDatePicker = false },
        )
    }
    if (showEndDatePicker) {
        MediDatePickerDialog(
            initialDateStr = s.endDate.ifBlank { s.startDate },
            onDismiss      = { showEndDatePicker = false },
            onConfirm      = { vm.onEndDate(it); showEndDatePicker = false },
        )
    }

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            MediAlertTopBar(
                title  = if (s.isEditMode) "Edit Medicine" else "Add Medicine",
                onBack = onBack,
            )
        },
    ) { pad ->
        LazyColumn(
            modifier        = Modifier.fillMaxSize().padding(pad),
            contentPadding  = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {

            // ── Pill colour ───────────────────────────────
            item {
                MediCard {
                    SectionLabel("Pill Colour", modifier = Modifier.padding(0.dp))
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PillColors.forEach { c ->
                            val hex = "#%06X".format((c.value and 0xFFFFFFUL).toLong()).uppercase()
                            val selected = s.colorHex.uppercase() == hex
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .clickable { vm.onColor(hex) },
                            ) {
                                if (selected) {
                                    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                                        drawCircle(
                                            color  = Color.White.copy(.6f),
                                            radius = size.minDimension / 2f,
                                            style  = androidx.compose.ui.graphics.drawscope.Stroke(3f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Medicine name ─────────────────────────────
            item {
                OutlinedTextField(
                    value         = s.name,
                    onValueChange = vm::onName,
                    label         = { Text("Medicine Name") },
                    placeholder   = { Text("e.g. Paracetamol") },
                    singleLine    = true,
                    isError       = s.nameError != null,
                    supportingText = s.nameError?.let { { Text(it, color = Danger) } },
                    shape         = RoundedCornerShape(12.dp),
                    colors        = fieldColors,
                    modifier      = Modifier.fillMaxWidth(),
                )
            }

            // ── Dosage wheel picker ───────────────────────
            item {
                MediCard {
                    SectionLabel("Dosage (mg)", modifier = Modifier.padding(0.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "${DOSAGE_VALUES[s.dosageMgIndex]} mg",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                    Spacer(Modifier.height(4.dp))
                    WheelPicker(
                        items          = DOSAGE_VALUES.map { it.toString() },
                        selectedIndex  = s.dosageMgIndex,
                        onIndexChanged = vm::onDosageMgIndex,
                        modifier       = Modifier.fillMaxWidth(),
                    )
                }
            }

            // ── Frequency ─────────────────────────────────
            item {
                Column {
                    SectionLabel("Frequency")
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgSurface)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        listOf(
                            Frequency.ONCE   to "Once",
                            Frequency.TWICE  to "Twice",
                            Frequency.THRICE to "3×",
                        ).forEach { (f, l) ->
                            val sel = s.frequency == f
                            Button(
                                onClick = { vm.onFrequency(f) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(9.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (sel) btnColor else Color.Transparent,
                                    contentColor   = if (sel) Color.White else TextMuted,
                                ),
                                contentPadding = PaddingValues(vertical = 10.dp),
                            ) {
                                Text(l, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            // ── Reminder times (tap to open clock picker) ─
            item {
                Column {
                    SectionLabel("Reminder Time${if (s.times.size > 1) "s" else ""}")
                    Spacer(Modifier.height(6.dp))
                    s.times.forEachIndexed { i, t ->
                        val label = listOf("1st", "2nd", "3rd").getOrElse(i) { "${i + 1}th" }
                        Row(
                            modifier = Modifier.padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                label,
                                style    = MaterialTheme.typography.labelSmall,
                                color    = TextMuted,
                                modifier = Modifier.width(24.dp),
                            )
                            Surface(
                                shape    = RoundedCornerShape(12.dp),
                                color    = BgSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { timePickerSlot = i },
                            ) {
                                Row(
                                    modifier             = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment    = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        fmtTime(t),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = "Pick time",
                                        tint   = Primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Dates (tap to open calendar picker) ───────
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Start date
                    Surface(
                        shape    = RoundedCornerShape(12.dp),
                        color    = BgSurface,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showStartDatePicker = true },
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                            Text(
                                "Start Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary,
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    s.startDate.ifBlank { "Pick date" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (s.startDate.isBlank()) TextMuted else TextPrimary,
                                )
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }

                    // End date
                    Surface(
                        shape    = RoundedCornerShape(12.dp),
                        color    = BgSurface,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showEndDatePicker = true },
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                            Text(
                                "End Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (s.endDate.isBlank()) TextMuted else Primary,
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    s.endDate.ifBlank { "Optional" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (s.endDate.isBlank()) TextMuted else TextPrimary,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (s.endDate.isNotBlank()) {
                                        Text(
                                            "×",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextMuted,
                                            modifier = Modifier.clickable { vm.onEndDate("") },
                                        )
                                    }
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = if (s.endDate.isBlank()) TextMuted else Primary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Notes ─────────────────────────────────────
            item {
                OutlinedTextField(
                    value         = s.notes,
                    onValueChange = vm::onNotes,
                    label         = { Text("Notes (optional)") },
                    placeholder   = { Text("e.g. Take with food") },
                    minLines      = 3,
                    maxLines      = 5,
                    shape         = RoundedCornerShape(12.dp),
                    colors        = fieldColors,
                    modifier      = Modifier.fillMaxWidth(),
                )
            }

            // ── Save ──────────────────────────────────────
            item {
                Button(
                    onClick  = { vm.save(medicineId) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = btnColor),
                ) {
                    Text(
                        if (s.isEditMode) "Save Changes" else "Add Medicine",
                        style      = MaterialTheme.typography.titleMedium,
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            item { MedicalDisclaimer() }
        }
    }
}
