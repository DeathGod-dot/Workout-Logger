package com.example.workoutlogger.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.Calendar

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "workout_reminders"
        private const val CHANNEL_NAME = "Workout Reminders"
        private const val PR_CHANNEL_ID = "pr_notifications"
        private const val PR_CHANNEL_NAME = "Personal Records"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH // Increased to HIGH
            
            val reminderChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Daily workout reminders"
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                enableVibration(true)
            }
            
            val prChannel = NotificationChannel(PR_CHANNEL_ID, PR_CHANNEL_NAME, importance).apply {
                description = "Personal Record achievements"
                enableLights(true)
                lightColor = android.graphics.Color.YELLOW
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(prChannel)
        }
    }

    fun sendPRNotification(exerciseName: String, weight: Float, unit: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val weightValue = if (weight % 1.0f == 0f) weight.toInt().toString() else String.format(java.util.Locale.getDefault(), "%.1f", weight)
            val builder = NotificationCompat.Builder(context, PR_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("New Personal Record! 🏆")
                .setContentText("You just hit $weightValue $unit on $exerciseName!")
                .setPriority(NotificationCompat.PRIORITY_MAX) // Increased priority
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            try {
                with(NotificationManagerCompat.from(context)) {
                    notify(System.currentTimeMillis().toInt(), builder.build())
                }
            } catch (e: SecurityException) {
                Log.e("NotificationHelper", "Permission denied despite check", e)
            }
        }
    }

    fun scheduleWorkoutReminder(minutesFromStartOfDay: Int) {
        Log.d("NotificationHelper", "Scheduling reminder for $minutesFromStartOfDay minutes from start of day")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 
            1001, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutesFromStartOfDay / 60)
            set(Calendar.MINUTE, minutesFromStartOfDay % 60)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun cancelWorkoutReminder() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 
            1001, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
