package np.com.sampurnasimkhada.util

import np.com.sampurnasimkhada.data.local.entity.MedicineEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun today(): String = LocalDate.now().format(DATE_FMT)
fun nowTime(): String = LocalTime.now().format(TIME_FMT)

fun fmtTime(t: String): String {
    return try {
        val (h, m) = t.split(":").map(String::toInt)
        val ap = if (h >= 12) "PM" else "AM"
        val hr = if (h % 12 == 0) 12 else h % 12
        "$hr:${m.toString().padStart(2, '0')} $ap"
    } catch (_: Exception) { t }
}

fun timeGroup(t: String): String {
    val h = t.split(":").firstOrNull()?.toIntOrNull() ?: 0
    return when {
        h < 12 -> "Morning"
        h < 17 -> "Afternoon"
        h < 20 -> "Evening"
        else   -> "Night"
    }
}

fun groupIcon(group: String): String = when (group) {
    "Morning"   -> "☀️"
    "Afternoon" -> "🌤"
    "Evening"   -> "🌇"
    else        -> "🌙"
}

// ── Frequency ─────────────────────────────────────────────

enum class Frequency(val label: String, val defaultTimes: List<String>) {
    ONCE  ("Once daily",    listOf("08:00")),
    TWICE ("Twice daily",   listOf("08:00", "20:00")),
    THRICE("3× daily",      listOf("08:00", "13:00", "20:00")),
    CUSTOM("Custom",        listOf("08:00")),
}

fun String.toFrequency(): Frequency =
    Frequency.entries.firstOrNull { it.name == this } ?: Frequency.ONCE

// ── Dosage picker values ───────────────────────────────────

val DOSAGE_VALUES = listOf(
    5, 10, 15, 20, 25, 30, 40, 50, 60, 75, 80, 100,
    120, 125, 150, 175, 200, 225, 250, 300, 350, 400,
    450, 500, 550, 600, 650, 700, 750, 800, 900,
    1000, 1200, 1500, 2000,
)

fun dosageIndexFor(amount: Int): Int {
    val idx = DOSAGE_VALUES.indexOfFirst { it >= amount }
    return if (idx == -1) DOSAGE_VALUES.lastIndex else idx
}

fun parseDosageAmount(raw: String): Int =
    Regex("""\d+""").find(raw)?.value?.toIntOrNull() ?: 500

// ── Entity helpers ────────────────────────────────────────

fun MedicineEntity.timesList(): List<String> =
    times.split(",").map { it.trim() }.filter { it.isNotBlank() }

fun List<String>.toTimesString(): String = joinToString(",")
