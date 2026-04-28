package np.com.sampurnasimkhada.util

import np.com.sampurnasimkhada.data.local.entity.MedicineEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ── Date / time helpers ───────────────────────────────────

val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun today(): String = LocalDate.now().format(DATE_FMT)
fun nowTime(): String = LocalTime.now().format(TIME_FMT)

/**
 * Format "HH:mm" → "8:00 AM"
 */
fun fmtTime(t: String): String {
    return try {
        val (h, m) = t.split(":").map(String::toInt)
        val ap = if (h >= 12) "PM" else "AM"
        val hr = if (h % 12 == 0) 12 else h % 12
        "$hr:${m.toString().padStart(2, '0')} $ap"
    } catch (_: Exception) { t }
}

/**
 * Return the period label for a "HH:mm" string.
 */
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

// ── Frequency helpers ─────────────────────────────────────

enum class Frequency(val label: String, val defaultTimes: List<String>) {
    ONCE  ("Once daily",    listOf("08:00")),
    TWICE ("Twice daily",   listOf("08:00", "20:00")),
    THRICE("3× daily",      listOf("08:00", "13:00", "20:00")),
    CUSTOM("Custom",        listOf("08:00")),
}

fun String.toFrequency(): Frequency =
    Frequency.entries.firstOrNull { it.name == this } ?: Frequency.ONCE

// ── Entity helpers ────────────────────────────────────────

/** Split the comma-separated times field into a List<String>. */
fun MedicineEntity.timesList(): List<String> =
    times.split(",").map { it.trim() }.filter { it.isNotBlank() }

/** Encode a list of times back to the comma-separated storage format. */
fun List<String>.toTimesString(): String = joinToString(",")
