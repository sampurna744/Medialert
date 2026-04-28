package np.com.sampurnasimkhada.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import np.com.sampurnasimkhada.data.local.entity.MedicineEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val TAG = "AlarmScheduler"
    }

    /**
     * Schedule one exact alarm per time slot for this medicine.
     * Each alarm is one-shot; AlarmReceiver reschedules it for the
     * following day after firing — this is the only reliable way to
     * get both exact timing and daily repetition on modern Android.
     */
    fun scheduleAlarmsForMedicine(medicine: MedicineEntity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Exact alarm permission not granted — skipping schedule for ${medicine.name}")
                return
            }
        }

        val times = medicine.times.split(",").map { it.trim() }
        val today = LocalDate.now()

        times.forEachIndexed { index, timeStr ->
            try {
                val time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
                var scheduledDateTime = LocalDateTime.of(today, time)

                if (scheduledDateTime.isBefore(LocalDateTime.now())) {
                    scheduledDateTime = scheduledDateTime.plusDays(1)
                }

                // createPendingIntent returns non-null (FLAG_UPDATE_CURRENT)
                val pendingIntent: PendingIntent = createPendingIntent(medicine, index)

                val triggerAtMillis = scheduledDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }

                Log.d(TAG, "Scheduled alarm for ${medicine.name} at $scheduledDateTime (slot $index)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule alarm for ${medicine.name} slot $index", e)
            }
        }
    }

    /**
     * Called by AlarmReceiver after each alarm fires to push the
     * trigger forward by exactly one day.
     */
    fun scheduleNextDay(
        medicineId: Long,
        medicineName: String,
        medicineDosage: String,
        times: String,
        slotIndex: Int,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) return
        }

        try {
            val timeStr = times.split(",").map { it.trim() }.getOrNull(slotIndex) ?: return
            val time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
            val tomorrow = LocalDateTime.of(LocalDate.now().plusDays(1), time)

            // Build a fresh non-null PendingIntent directly (no medicine entity here)
            val intent = buildAlarmIntent(medicineId, medicineName, medicineDosage, times, slotIndex)
            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
                context,
                (medicineId * 100 + slotIndex).toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )!!

            val triggerAtMillis = tomorrow
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }

            Log.d(TAG, "Rescheduled $medicineName slot $slotIndex for tomorrow at $tomorrow")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reschedule next day for medicineId=$medicineId slot=$slotIndex", e)
        }
    }

    fun cancelAlarmsForMedicine(medicine: MedicineEntity) {
        val times = medicine.times.split(",")
        times.forEachIndexed { index, _ ->
            // findPendingIntent uses FLAG_NO_CREATE — returns null if no alarm registered
            val pendingIntent: PendingIntent? = findPendingIntent(medicine, index)
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                Log.d(TAG, "Cancelled alarm for ${medicine.name} slot $index")
            }
        }
    }

    // ── PendingIntent helpers ─────────────────────────────────────────────────

    /**
     * Create (or update) a PendingIntent for scheduling.
     * FLAG_UPDATE_CURRENT guarantees a non-null return — the !! is safe.
     */
    private fun createPendingIntent(medicine: MedicineEntity, slotIndex: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            (medicine.id * 100 + slotIndex).toInt(),
            buildAlarmIntent(medicine.id, medicine.name, medicine.dosage, medicine.times, slotIndex),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )!!

    /**
     * Look up an existing PendingIntent without creating one.
     * FLAG_NO_CREATE returns null if the alarm was never registered.
     */
    private fun findPendingIntent(medicine: MedicineEntity, slotIndex: Int): PendingIntent? =
        PendingIntent.getBroadcast(
            context,
            (medicine.id * 100 + slotIndex).toInt(),
            buildAlarmIntent(medicine.id, medicine.name, medicine.dosage, medicine.times, slotIndex),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

    /** Shared intent builder so schedule / cancel / reschedule all produce identical intents. */
    private fun buildAlarmIntent(
        medicineId: Long,
        medicineName: String,
        medicineDosage: String,
        medicineTimes: String,
        slotIndex: Int,
    ): Intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra(AlarmReceiver.EXTRA_MEDICINE_ID,     medicineId)
        putExtra(AlarmReceiver.EXTRA_MEDICINE_NAME,   medicineName)
        putExtra(AlarmReceiver.EXTRA_MEDICINE_DOSAGE, medicineDosage)
        putExtra(AlarmReceiver.EXTRA_MEDICINE_TIMES,  medicineTimes)
        putExtra(AlarmReceiver.EXTRA_SLOT_INDEX,      slotIndex)
    }
}