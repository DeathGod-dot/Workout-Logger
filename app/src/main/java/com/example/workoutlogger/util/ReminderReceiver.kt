package com.example.workoutlogger.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat

import com.example.workoutlogger.data.preferences.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Alarm triggered! Attempting to send notification...")
        
        val settingsManager = SettingsManager(context)
        val notificationHelper = NotificationHelper(context)

        CoroutineScope(Dispatchers.IO).launch {
            val enabled = settingsManager.notificationsEnabledFlow.first()
            val time = settingsManager.reminderTimeFlow.first()

            if (enabled) {
                // Energetic Messages
                val messages = listOf(
                    "No Excuses! 🚀" to "Success starts outside your comfort zone. Let's go!",
                    "Beast Mode: ON 🦍" to "The only bad workout is the one that didn't happen.",
                    "The Gym is Calling... 📞" to "You're only one workout away from a good mood!",
                    "Iron Awaits! ⚔️" to "Discipline > Motivation. See you at the gym!",
                    "Time to Level Up! 🆙" to "Don't let your dreams be dreams. Lift that weight!",
                    "Chase the Pump! 💪" to "Yesterday you said tomorrow. Today is the day!"
                )
                val (title, content) = messages.random()

                // Send the notification
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    Log.d("ReminderReceiver", "Permission granted. Sending notification.")
                    val builder = NotificationCompat.Builder(context, "workout_reminders")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)

                    with(NotificationManagerCompat.from(context)) {
                        notify(1001, builder.build())
                    }
                }

                // Reschedule the next alarm for tomorrow
                notificationHelper.scheduleWorkoutReminder(time)
            }
        }
    }
}
