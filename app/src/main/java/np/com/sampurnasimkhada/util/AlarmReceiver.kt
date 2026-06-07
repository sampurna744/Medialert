package np.com.sampurnasimkhada.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import np.com.sampurnasimkhada.MainActivity
import np.com.sampurnasimkhada.data.local.database.MediAlertDatabase
import np.com.sampurnasimkhada.data.preferences.AppPreferencesDataSource
import np.com.sampurnasimkhada.data.repository.SettingsRepository

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_MEDICINE_ID     = "MEDICINE_ID"
        const val EXTRA_MEDICINE_NAME   = "MEDICINE_NAME"
        const val EXTRA_MEDICINE_DOSAGE = "MEDICINE_DOSAGE"
        const val EXTRA_MEDICINE_TIMES  = "MEDICINE_TIMES"
        const val EXTRA_SLOT_INDEX      = "SLOT_INDEX"
        const val ACTION_STOP_ALARM     = "np.com.sampurnasimkhada.STOP_ALARM"
        const val ACTION_MARK_TAKEN     = "np.com.sampurnasimkhada.MARK_TAKEN"
        private const val EXTRA_NOTIF_ID       = "NOTIF_ID"
        private const val EXTRA_SCHEDULED_TIME = "SCHEDULED_TIME"
        private const val EXTRA_SCHEDULED_DATE = "SCHEDULED_DATE"
        private const val CHANNEL_ID           = "medialert_alarms"
        private const val TAG                  = "AlarmReceiver"

        @Volatile private var activeRingtone: Ringtone? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                Log.d(TAG, "Boot completed — rescheduling all alarms")
                rescheduleAllAlarms(context)
                return
            }

            ACTION_STOP_ALARM -> {
                stopAlarm()
                cancelNotification(context, intent.getIntExtra(EXTRA_NOTIF_ID, -1))
                Log.d(TAG, "Alarm stopped by user")
                return
            }

            ACTION_MARK_TAKEN -> {
                stopAlarm()
                val medicineId = intent.getLongExtra(EXTRA_MEDICINE_ID, -1L)
                val date       = intent.getStringExtra(EXTRA_SCHEDULED_DATE) ?: today()
                val time       = intent.getStringExtra(EXTRA_SCHEDULED_TIME) ?: ""
                if (medicineId != -1L && time.isNotBlank()) {
                    runBlocking {
                        MediAlertDatabase.getInstance(context)
                            .doseLogDao()
                            .updateStatus(medicineId, date, time, "TAKEN", System.currentTimeMillis())
                    }
                    Log.d(TAG, "Marked dose taken: medicineId=$medicineId time=$time")
                }
                cancelNotification(context, intent.getIntExtra(EXTRA_NOTIF_ID, -1))
                return
            }
        }

        val medicineName   = intent.getStringExtra(EXTRA_MEDICINE_NAME)   ?: "Medicine"
        val medicineDosage = intent.getStringExtra(EXTRA_MEDICINE_DOSAGE) ?: ""
        val medicineId     = intent.getLongExtra(EXTRA_MEDICINE_ID, -1L)
        val medicineTimes  = intent.getStringExtra(EXTRA_MEDICINE_TIMES)  ?: ""
        val slotIndex      = intent.getIntExtra(EXTRA_SLOT_INDEX, 0)
        val scheduledTime  = medicineTimes.split(",").getOrElse(slotIndex) { "" }.trim()

        Log.d(TAG, "Alarm fired for $medicineName (id=$medicineId slot=$slotIndex time=$scheduledTime)")

        val settings = runBlocking {
            SettingsRepository(AppPreferencesDataSource(context)).settings.first()
        }

        if (settings.notificationsEnabled) {
            if (settings.soundEnabled) {
                playAlarmSound(context, settings.alarmSoundUri, medicineId)
            }
            showNotification(
                context        = context,
                name           = medicineName,
                dosage         = medicineDosage,
                medicineId     = medicineId,
                scheduledTime  = scheduledTime,
                scheduledDate  = today(),
            )
        }

        if (medicineId != -1L && medicineTimes.isNotBlank()) {
            AlarmScheduler(context).scheduleNextDay(
                medicineId, medicineName, medicineDosage, medicineTimes, slotIndex
            )
        }
    }

    // ── Sound ─────────────────────────────────────────────

    private fun playAlarmSound(context: Context, storedUri: String, medicineId: Long) {
        stopAlarm()
        val soundUri: Uri = if (storedUri.isNotBlank()) Uri.parse(storedUri)
            else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        try {
            val ringtone = RingtoneManager.getRingtone(context, soundUri) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ringtone.isLooping = false
            activeRingtone = ringtone
            ringtone.play()
            // Auto-stop after 30 s if the user doesn't act
            Thread {
                Thread.sleep(30_000)
                if (activeRingtone === ringtone) {
                    stopAlarm()
                    cancelNotification(context, medicineId.toInt())
                }
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play alarm sound", e)
        }
    }

    private fun stopAlarm() {
        activeRingtone?.stop()
        activeRingtone = null
    }

    private fun cancelNotification(context: Context, notifId: Int) {
        if (notifId != -1) {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(notifId)
        }
    }

    // ── Notification ──────────────────────────────────────

    private fun showNotification(
        context: Context,
        name: String,
        dosage: String,
        medicineId: Long,
        scheduledTime: String,
        scheduledDate: String,
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS not granted — skipping notification for $name")
                return
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Medicine Alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Alarm notifications for scheduled medicines"
                    enableVibration(true)
                    enableLights(true)
                    setSound(null, null)
                }
            )
        }

        val notifId = medicineId.toInt()

        // Tap → open app
        val tapPendingIntent = PendingIntent.getActivity(
            context, notifId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Stop Alarm action
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            notifId * 100 + 1,
            Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_STOP_ALARM
                putExtra(EXTRA_NOTIF_ID, notifId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Mark as Taken action
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            notifId * 100 + 2,
            Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_MARK_TAKEN
                putExtra(EXTRA_MEDICINE_ID, medicineId)
                putExtra(EXTRA_SCHEDULED_DATE, scheduledDate)
                putExtra(EXTRA_SCHEDULED_TIME, scheduledTime)
                putExtra(EXTRA_NOTIF_ID, notifId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Time for $name")
            .setContentText("Dosage: $dosage")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(tapPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .addAction(android.R.drawable.ic_media_pause,  "Stop Alarm",     stopPendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, "Mark as Taken", takenPendingIntent)
            .build()

        notificationManager.notify(notifId, notification)
        Log.d(TAG, "Notification shown for $name (time=$scheduledTime)")
    }

    // ── Boot ──────────────────────────────────────────────

    private fun rescheduleAllAlarms(context: Context) {
        val scheduler = AlarmScheduler(context)
        Thread {
            try {
                val db = MediAlertDatabase.getInstance(context)
                val medicines = runBlocking { db.medicineDao().getAllMedicines() }
                medicines.forEach { scheduler.scheduleAlarmsForMedicine(it) }
                Log.d(TAG, "Rescheduled alarms for ${medicines.size} medicines after boot")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reschedule alarms on boot", e)
            }
        }.start()
    }
}
