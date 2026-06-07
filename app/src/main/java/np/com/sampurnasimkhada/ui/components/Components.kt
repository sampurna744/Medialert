package np.com.sampurnasimkhada.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.filter
import np.com.sampurnasimkhada.ui.theme.*

import np.com.sampurnasimkhada.util.DATE_FMT
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.abs

// ── Top bar ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediAlertTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor        = Color.Transparent,
            titleContentColor     = TextPrimary,
            navigationIconContentColor = TextMuted,
            actionIconContentColor     = TextMuted,
        ),
    )
}

// ── Cards ─────────────────────────────────────────────────

@Composable
fun MediCard(
    modifier: Modifier = Modifier,
    color: Color = BgSurface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        color     = color,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

// ── Buttons ───────────────────────────────────────────────

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: Brush = Brush.linearGradient(listOf(Primary, PrimaryDark)),
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled) gradient
                    else Brush.linearGradient(listOf(TextMuted, TextMuted)),
                    RoundedCornerShape(16.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(text, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ── Badges ────────────────────────────────────────────────

@Composable
fun StatusBadge(text: String, containerColor: Color, contentColor: Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = containerColor) {
        Text(
            text     = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style    = MaterialTheme.typography.labelSmall,
            color    = contentColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Progress ring ─────────────────────────────────────────

@Composable
fun ProgressRing(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier,
    ringColor: Color = Color.White,
    trackColor: Color = Color.White.copy(alpha = 0.15f),
    strokeWidth: Dp = 6.dp,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress    = { progress },
            modifier    = Modifier.fillMaxSize(),
            color       = ringColor,
            trackColor  = trackColor,
            strokeWidth = strokeWidth,
        )
        Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
    }
}

// ── Colour dot ────────────────────────────────────────────

@Composable
fun ColorDot(hex: String, size: Dp = 10.dp) {
    val c = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Primary)
    Box(modifier = Modifier.size(size).clip(CircleShape).background(c))
}

// ── Disclaimer ────────────────────────────────────────────

@Composable
fun MedicalDisclaimer(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color(0xFF1A100A)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("⚠️ Medical Disclaimer", style = MaterialTheme.typography.labelLarge, color = Warning, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "This app is not a substitute for professional medical advice. Always consult your doctor.",
                style     = MaterialTheme.typography.bodySmall,
                color     = Color(0xFFAA7A40),
                lineHeight = 18.sp,
            )
        }
    }
}

// ── Section label ─────────────────────────────────────────

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text     = text.uppercase(),
        modifier = modifier,
        style    = MaterialTheme.typography.labelSmall,
        color    = TextMuted,
        letterSpacing = 1.sp,
    )
}

// ── Toggle row ────────────────────────────────────────────

@Composable
fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor  = Color.White,
                checkedTrackColor  = Primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = TextMuted,
            ),
        )
    }
}

// ── Wheel / drum-roll picker ──────────────────────────────
//
// Uses phantom (empty) items at each end instead of contentPadding so that
// rememberSnapFlingBehavior always snaps cleanly and
// firstVisibleItemIndex == selectedDataIndex after every snap.

@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onIndexChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemHeight = 44.dp
    val visibleCount = 5
    val halfCount = visibleCount / 2   // 2

    // Padded list: [phantom, phantom, data..., phantom, phantom]
    // firstVisibleItemIndex 0 → data[0] centred; index 1 → data[1] centred, etc.
    val paddedItems = remember(items) {
        List(halfCount) { null } + items.map { it } + List(halfCount) { null }
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex.coerceIn(0, items.lastIndex),
    )
    val flingBehavior = rememberSnapFlingBehavior(listState)

    // Live centred index during scrolling (for colour/weight animation)
    val centredDataIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex.coerceIn(0, items.lastIndex) }
    }

    // Sync scroll when selectedIndex changes externally (loadForEdit)
    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress) {
            listState.animateScrollToItem(selectedIndex.coerceIn(0, items.lastIndex))
        }
    }

    // Report selected index each time the user's scroll settles
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { !it }
            .collect {
                val idx = listState.firstVisibleItemIndex.coerceIn(0, items.lastIndex)
                if (idx != selectedIndex) onIndexChanged(idx)
            }
    }

    Box(
        modifier = modifier.height(itemHeight * visibleCount),
        contentAlignment = Alignment.Center,
    ) {
        // Selection highlight bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(Primary.copy(alpha = 0.13f), RoundedCornerShape(10.dp))
                .border(1.dp, Primary.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
        )

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            itemsIndexed(paddedItems) { paddedIdx, item ->
                val distance = abs(paddedIdx - (centredDataIndex + halfCount))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    if (item != null) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (distance == 0) FontWeight.Bold else FontWeight.Normal,
                            color = when (distance) {
                                0    -> Primary
                                1    -> TextSecondary.copy(alpha = 0.6f)
                                else -> TextMuted.copy(alpha = 0.22f)
                            },
                        )
                    }
                }
            }
        }
    }
}

// ── Time picker dialog ────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int = 8,
    initialMinute: Int = 0,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("OK", color = Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        },
        text = { TimePicker(state = state) },
        containerColor = BgSurface,
    )
}

// ── Date picker dialog ────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediDatePickerDialog(
    initialDateStr: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val initialMillis = runCatching {
        LocalDate.parse(initialDateStr, DATE_FMT)
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    }.getOrDefault(System.currentTimeMillis())

    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis ?: return@TextButton
                val date = Instant.ofEpochMilli(millis)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
                    .format(DATE_FMT)
                onConfirm(date)
            }) {
                Text("OK", color = Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        },
    ) {
        DatePicker(
            state = state,
            colors = DatePickerDefaults.colors(
                containerColor = BgSurface,
                selectedDayContainerColor = Primary,
                todayDateBorderColor = Primary,
            ),
        )
    }
}
