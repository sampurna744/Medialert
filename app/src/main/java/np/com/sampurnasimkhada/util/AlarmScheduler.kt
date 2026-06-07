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

    fun scheduleAlarmsForMedicine(medicine: MedicineEntity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w(TAG, "Exact alarm permission not granted — skipping ${medicine.name}")
            return
        }

        val today = LocalDate.now()
        medicine.times.split(",").map { it.trim() }.forEachIndexed { index, timeStr ->
            try {
                val time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
                var scheduledAt = LocalDateTime.of(today, time)
                if (scheduledAt.isBefore(LocalDateTime.now())) scheduledAt = scheduledAt.plusDays(1)

                scheduleExact(createPendingIntent(medicine, index), scheduledAt)
                Log.d(TAG, "Scheduled ${medicine.name} at $scheduledAt (slot $index)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule ${medicine.name} slot $index", e)
            }
        }
    }

    fun scheduleNextDay(
        medicineId: Long,
        medicineName: String,
        medicineDosage: String,
        times: String,
        slotIndex: Int,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) return

        try {
            val timeStr = times.split(",").map { it.trim() }.getOrNull(slotIndex) ?: return
            val time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
            val tomorrow = LocalDateTime.of(LocalDate.now().plusDays(1), time)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (medicineId * 100 + slotIndex).toInt(),
                buildAlarmIntent(medicineId, medicineName, medicineDosage, times, slotIndex),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )!!

            scheduleExact(pendingIntent, tomorrow)
            Log.d(TAG, "Rescheduled $medicineName slot $slotIndex for $tomorrow")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reschedule next day for medicineId=$medicineId slot=$slotIndex", e)
        }
    }

    fun cancelAlarmsForMedicine(medicine: MedicineEntity) {
        medicine.times.split(",").forEachIndexed { index, _ ->
            findPendingIntent(medicine, index)?.let { alarmManager.cancel(it) }
        }
    }

    private fun scheduleExact(pendingIntent: PendingIntent, at: LocalDateTime) {
        val triggerMillis = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    private fun createPendingIntent(medicine: MedicineEntity, slotIndex: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            (medicine.id * 100 + slotIndex).toInt(),
            buildAlarmIntent(medicine.id, medicine.name, medicine.dosage, medicine.times, slotIndex),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )!!

    private fun findPendingIntent(medicine: MedicineEntity, slotIndex: Int): PendingIntent? =
        PendingIntent.getBroadcast(
            context,
            (medicine.id * 100 + slotIndex).toInt(),
            buildAlarmIntent(medicine.id, medicine.name, medicine.dosage, medicine.times, slotIndex),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

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
