package com.example.workoutlogger.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.workoutlogger.data.preferences.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val settingsManager = SettingsManager(context)
            val notificationHelper = NotificationHelper(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                val enabled = settingsManager.notificationsEnabledFlow.first()
                val time = settingsManager.reminderTimeFlow.first()
                
                if (enabled) {
                    notificationHelper.scheduleWorkoutReminder(time)
                }
            }
        }
    }
}
