package np.com.sampurnasimkhada.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import np.com.sampurnasimkhada.MainActivity

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_MEDICINE_ID     = "MEDICINE_ID"
        const val EXTRA_MEDICINE_NAME   = "MEDICINE_NAME"
        const val EXTRA_MEDICINE_DOSAGE = "MEDICINE_DOSAGE"
        const val EXTRA_MEDICINE_TIMES  = "MEDICINE_TIMES"
        const val EXTRA_SLOT_INDEX      = "SLOT_INDEX"
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        // ── Boot completed: reschedule all alarms ─────────
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed — rescheduling all alarms")
            rescheduleAllAlarms(context)
            return
        }

        // ── Normal alarm fire ─────────────────────────────
        val medicineName   = intent.getStringExtra(EXTRA_MEDICINE_NAME)   ?: "Medicine"
        val medicineDosage = intent.getStringExtra(EXTRA_MEDICINE_DOSAGE) ?: ""
        val medicineId     = intent.getLongExtra(EXTRA_MEDICINE_ID, -1L)
        val medicineTimes  = intent.getStringExtra(EXTRA_MEDICINE_TIMES)  ?: ""
        val slotIndex      = intent.getIntExtra(EXTRA_SLOT_INDEX, 0)

        Log.d(TAG, "Alarm fired for $medicineName (id=$medicineId slot=$slotIndex)")

        // Show the notification
        showNotification(context, medicineName, medicineDosage, medicineId)

        // Reschedule this exact slot for tomorrow — this is how daily repetition works
        // without using setInexactRepeating (which would overwrite exact alarms).
        if (medicineId != -1L && medicineTimes.isNotBlank()) {
            AlarmScheduler(context).scheduleNextDay(
                medicineId, medicineName, medicineDosage, medicineTimes, slotIndex
            )
        }
    }

    private fun showNotification(context: Context, name: String, dosage: String, medicineId: Long) {
        val channelId = "medialert_notifications"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // ── Android 13+: check POST_NOTIFICATIONS permission ──
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                Log.w(TAG, "POST_NOTIFICATIONS not granted — cannot show notification for $name")
                return
            }
        }

        // ── Create notification channel (no-op if already exists) ──
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for your scheduled medicines"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            medicineId.toInt(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("💊 Time for $name")
            .setContentText("Please take your dosage: $dosage")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        // Use medicineId as the notification ID so each medicine gets its own notification
        // (rather than all colliding on the same ID)
        notificationManager.notify(medicineId.toInt(), notification)
        Log.d(TAG, "Notification shown for $name")
    }

    /**
     * Called on BOOT_COMPLETED. Reads all medicines from the DB and
     * reschedules their alarms since all AlarmManager alarms are wiped on reboot.
     */
    private fun rescheduleAllAlarms(context: Context) {
        val scheduler = AlarmScheduler(context)
        // Run on a background thread — BroadcastReceiver.onReceive must return quickly
        // but we use goAsync() pattern via a plain thread here to avoid StrictMode
        Thread {
            try {
                val db = np.com.sampurnasimkhada.data.local.database.MediAlertDatabase
                    .getInstance(context)
                val medicines = kotlinx.coroutines.runBlocking {
                    db.medicineDao().getAllMedicines()
                }
                medicines.forEach { scheduler.scheduleAlarmsForMedicine(it) }
                Log.d(TAG, "Rescheduled alarms for ${medicines.size} medicines after boot")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reschedule alarms on boot", e)
            }
        }.start()
    }
}