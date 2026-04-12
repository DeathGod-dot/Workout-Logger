package com.example.workoutlogger.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val REST_TIMER_ENABLED = booleanPreferencesKey("rest_timer_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SOUND_FEEDBACK_ENABLED = booleanPreferencesKey("sound_feedback_enabled")
        val REMINDER_TIME = intPreferencesKey("reminder_time") // Minutes from start of day
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LAST_WORKOUT_DATE = longPreferencesKey("last_workout_date")
        val CUSTOM_USERNAME = stringPreferencesKey("custom_username")
        val PROFILE_PICTURE_URI = stringPreferencesKey("profile_picture_uri")
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE] ?: true }
    val weightUnitFlow: Flow<String> = context.dataStore.data.map { it[WEIGHT_UNIT] ?: "kg" }
    val keepScreenOnFlow: Flow<Boolean> = context.dataStore.data.map { it[KEEP_SCREEN_ON] ?: true }
    val restTimerEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[REST_TIMER_ENABLED] ?: true }
    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }
    val soundFeedbackEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[SOUND_FEEDBACK_ENABLED] ?: false }
    val reminderTimeFlow: Flow<Int> = context.dataStore.data.map { it[REMINDER_TIME] ?: (18 * 60) } // Default 6 PM
    val currentStreakFlow: Flow<Int> = context.dataStore.data.map { it[CURRENT_STREAK] ?: 0 }
    val lastWorkoutDateFlow: Flow<Long> = context.dataStore.data.map { it[LAST_WORKOUT_DATE] ?: 0L }
    val customUsernameFlow: Flow<String?> = context.dataStore.data.map { it[CUSTOM_USERNAME] }
    val profilePictureUriFlow: Flow<String?> = context.dataStore.data.map { it[PROFILE_PICTURE_URI] }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun setWeightUnit(unit: String) {
        context.dataStore.edit { it[WEIGHT_UNIT] = unit }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { it[KEEP_SCREEN_ON] = enabled }
    }

    suspend fun setRestTimerEnabled(enabled: Boolean) {
        context.dataStore.edit { it[REST_TIMER_ENABLED] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setSoundFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SOUND_FEEDBACK_ENABLED] = enabled }
    }

    suspend fun setReminderTime(minutes: Int) {
        context.dataStore.edit { it[REMINDER_TIME] = minutes }
    }

    suspend fun setCustomUsername(username: String?) {
        context.dataStore.edit { preferences ->
            if (username == null) preferences.remove(CUSTOM_USERNAME)
            else preferences[CUSTOM_USERNAME] = username
        }
    }

    suspend fun setProfilePictureUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri == null) preferences.remove(PROFILE_PICTURE_URI)
            else preferences[PROFILE_PICTURE_URI] = uri
        }
    }

    suspend fun updateStreak(newStreak: Int, lastDate: Long) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_STREAK] = newStreak
            preferences[LAST_WORKOUT_DATE] = lastDate
        }
    }
}
