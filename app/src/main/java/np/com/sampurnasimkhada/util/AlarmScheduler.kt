package np.com.sampurnasimkhada.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import np.com.sampurnasimkhada.data.local.entity.MedicineEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarmsForMedicine(medicine: MedicineEntity) {
        val times = medicine.times.split(",").map { it.trim() }
        val today = LocalDate.now()
        
        times.forEachIndexed { index, timeStr ->
            try {
                val time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
                var scheduledDateTime = LocalDateTime.of(today, time)
                
                // If the time has already passed today, schedule for tomorrow
                if (scheduledDateTime.isBefore(LocalDateTime.now())) {
                    scheduledDateTime = scheduledDateTime.plusDays(1)
                }

                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("MEDICINE_ID", medicine.id)
                    putExtra("MEDICINE_NAME", medicine.name)
                    putExtra("MEDICINE_DOSAGE", medicine.dosage)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (medicine.id * 100 + index).toInt(), // Unique ID for each alarm
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val triggerAtMillis = scheduledDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cancelAlarmsForMedicine(medicine: MedicineEntity) {
        val times = medicine.times.split(",")
        times.forEachIndexed { index, _ ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (medicine.id * 100 + index).toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }
    }
}
