package com.example.workoutlogger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workoutlogger.data.FirestoreManager
import com.example.workoutlogger.data.preferences.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsManager: SettingsManager,
    private val firestoreManager: com.example.workoutlogger.data.FirestoreManager,
    private val exerciseDao: com.example.workoutlogger.data.ExerciseDao
) : ViewModel() {

    val darkMode = settingsManager.darkModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val weightUnit = settingsManager.weightUnitFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "kg")

    val keepScreenOn = settingsManager.keepScreenOnFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val restTimerEnabled = settingsManager.restTimerEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notificationsEnabled = settingsManager.notificationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val soundFeedbackEnabled = settingsManager.soundFeedbackEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val reminderTime = settingsManager.reminderTimeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 18 * 60)

    val currentStreak = settingsManager.currentStreakFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val customUsername = settingsManager.customUsernameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val profilePictureUri = settingsManager.profilePictureUriFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val exercises = exerciseDao.getAllExercises()

    private val _backupStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val backupStatus = _backupStatus.asStateFlow()

    private val _restoreStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val restoreStatus = _restoreStatus.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setDarkMode(enabled) }
    }

    fun setWeightUnit(unit: String) {
        viewModelScope.launch { settingsManager.setWeightUnit(unit) }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setKeepScreenOn(enabled) }
    }

    fun setRestTimerEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setRestTimerEnabled(enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setNotificationsEnabled(enabled) }
    }

    fun setSoundFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setSoundFeedbackEnabled(enabled) }
    }

    fun setReminderTime(minutes: Int) {
        viewModelScope.launch { settingsManager.setReminderTime(minutes) }
    }

    fun updateCustomUsername(username: String?) {
        viewModelScope.launch { settingsManager.setCustomUsername(username) }
    }

    fun updateProfilePictureUri(uri: String?) {
        viewModelScope.launch { settingsManager.setProfilePictureUri(uri) }
    }

    fun clearAllData() {
        viewModelScope.launch {
            exerciseDao.clearAllExercises()
            settingsManager.setReminderTime(18 * 60) // Reset to default
            settingsManager.updateStreak(0, 0L)
            settingsManager.setCustomUsername(null)
            settingsManager.setProfilePictureUri(null)
        }
    }

    fun updateStreakAfterWorkout() {
        viewModelScope.launch {
            val lastWorkoutDate = settingsManager.lastWorkoutDateFlow.first()
            val streak = settingsManager.currentStreakFlow.first()
            val now = System.currentTimeMillis()

            val lastCal = java.util.Calendar.getInstance().apply { timeInMillis = lastWorkoutDate }
            val nowCal = java.util.Calendar.getInstance().apply { timeInMillis = now }

            // Check if it's the same day
            val isSameDay = lastCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
                           lastCal.get(java.util.Calendar.DAY_OF_YEAR) == nowCal.get(java.util.Calendar.DAY_OF_YEAR)

            if (isSameDay && streak > 0) return@launch // Already logged today

            // Check if it's the next day
            lastCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            val isNextDay = lastCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
                            lastCal.get(java.util.Calendar.DAY_OF_YEAR) == nowCal.get(java.util.Calendar.DAY_OF_YEAR)

            if (isNextDay || streak == 0 || lastWorkoutDate == 0L) {
                settingsManager.updateStreak(streak + 1, now)
            } else {
                // More than 1 day missed
                settingsManager.updateStreak(1, now)
            }
        }
    }

    fun checkAndResetStreak() {
        viewModelScope.launch {
            val lastWorkoutDate = settingsManager.lastWorkoutDateFlow.first()
            if (lastWorkoutDate == 0L) return@launch

            val lastCal = java.util.Calendar.getInstance().apply { timeInMillis = lastWorkoutDate }
            val nowCal = java.util.Calendar.getInstance()

            // Reset if more than 1 day has passed
            lastCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            if (nowCal.after(lastCal)) {
                // If it's NOT the next day (meaning it's even later)
                val isNextDay = lastCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
                                lastCal.get(java.util.Calendar.DAY_OF_YEAR) == nowCal.get(java.util.Calendar.DAY_OF_YEAR)
                if (!isNextDay) {
                    settingsManager.updateStreak(0, lastWorkoutDate) // Keep date but reset count
                }
            }
        }
    }

    fun backupToCloud() {
        viewModelScope.launch {
            _backupStatus.value = SyncStatus.Loading
            firestoreManager.backupToCloud()
                .onSuccess { _backupStatus.value = SyncStatus.Success("Backup successful") }
                .onFailure { _backupStatus.value = SyncStatus.Error(it.message ?: "Backup failed") }
        }
    }

    fun restoreFromCloud() {
        viewModelScope.launch {
            _restoreStatus.value = SyncStatus.Loading
            firestoreManager.restoreFromCloud()
                .onSuccess { _restoreStatus.value = SyncStatus.Success("Restore successful") }
                .onFailure { _restoreStatus.value = SyncStatus.Error(it.message ?: "Restore failed") }
        }
    }

    fun resetBackupStatus() {
        _backupStatus.value = SyncStatus.Idle
    }

    fun resetRestoreStatus() {
        _restoreStatus.value = SyncStatus.Idle
    }
}

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Loading : SyncStatus()
    data class Success(val message: String) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}
