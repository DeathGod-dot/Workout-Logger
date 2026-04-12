package com.example.workoutlogger.ux.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import com.example.workoutlogger.auth.UserData
import com.example.workoutlogger.util.FileUtil
import com.example.workoutlogger.viewmodel.SettingsViewModel
import com.example.workoutlogger.viewmodel.SyncStatus
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.app.TimePickerDialog
import com.example.workoutlogger.util.NotificationHelper
import java.util.Locale

// DESIGN SYSTEM
private val S_BgDark = Color(0xFF0D1526)
private val S_SurfaceDark = Color(0xFF1E2A4A)
private val S_BlueAccent = Color(0xFF3B82F6)
private val S_GoldAccent = Color(0xFFF59E0B)
private val S_GreenAccent = Color(0xFF10B981)
private val S_PurpleAccent = Color(0xFF8B5CF6)
private val S_TextMuted = Color(0xFF64748B)
private val S_TextPrimary = Color.White
private val S_DangerRed = Color(0xFFEF4444)

@Composable
fun SettingsScreen(
    userData: UserData?,
    onSignOut: () -> Unit = {},
    settingsViewModel: SettingsViewModel
) {
    // STATE
    val restTimer by settingsViewModel.restTimerEnabled.collectAsStateWithLifecycle()
    val notifications by settingsViewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val reminderTime by settingsViewModel.reminderTime.collectAsStateWithLifecycle()
    val keepScreenOn by settingsViewModel.keepScreenOn.collectAsStateWithLifecycle()
    val weightUnit by settingsViewModel.weightUnit.collectAsStateWithLifecycle()
    
    val customUsername by settingsViewModel.customUsername.collectAsStateWithLifecycle()
    val profilePictureUri by settingsViewModel.profilePictureUri.collectAsStateWithLifecycle()
    val streak by settingsViewModel.currentStreak.collectAsStateWithLifecycle()
    
    // Get real stats
    val exercises by settingsViewModel.exercises.collectAsState(initial = emptyList())
    val workoutCount = exercises.size
    val prCount = exercises.distinctBy { it.name }.size
    val totalVolume = exercises.sumOf { (it.sets * it.reps * it.weight).toDouble() }
    val volumeText = if (totalVolume >= 1000) String.format(Locale.getDefault(), "%.1fk", totalVolume / 1000.0) else if (totalVolume % 1.0 == 0.0) totalVolume.toInt().toString() else String.format(Locale.getDefault(), "%.1f", totalVolume)

    val backupStatus by settingsViewModel.backupStatus.collectAsStateWithLifecycle()
    val restoreStatus by settingsViewModel.restoreStatus.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    
    var screenAlpha by remember { mutableFloatStateOf(0f) }
    val animatedAlpha by animateFloatAsState(
        targetValue = screenAlpha,
        animationSpec = tween(400),
        label = "screen_alpha"
    )

    LaunchedEffect(Unit) {
        screenAlpha = 1f
    }

    LaunchedEffect(backupStatus) {
        when (backupStatus) {
            is SyncStatus.Success -> {
                Toast.makeText(context, (backupStatus as SyncStatus.Success).message, Toast.LENGTH_SHORT).show()
                settingsViewModel.resetBackupStatus()
            }
            is SyncStatus.Error -> {
                Toast.makeText(context, (backupStatus as SyncStatus.Error).message, Toast.LENGTH_SHORT).show()
                settingsViewModel.resetBackupStatus()
            }
            else -> {}
        }
    }

    LaunchedEffect(restoreStatus) {
        when (restoreStatus) {
            is SyncStatus.Success -> {
                Toast.makeText(context, (restoreStatus as SyncStatus.Success).message, Toast.LENGTH_SHORT).show()
                settingsViewModel.resetRestoreStatus()
            }
            is SyncStatus.Error -> {
                Toast.makeText(context, (restoreStatus as SyncStatus.Error).message, Toast.LENGTH_SHORT).show()
                settingsViewModel.resetRestoreStatus()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(S_BgDark)
            .alpha(animatedAlpha)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp)
        ) {
            // HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Settings",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = S_TextPrimary
                    )
                }
            }

            // PROFILE CARD
            item {
                ProfileCard(
                    userData = userData,
                    customUsername = customUsername,
                    profilePictureUri = profilePictureUri,
                    streak = streak,
                    workouts = workoutCount.toString(),
                    volume = volumeText,
                    prs = prCount.toString(),
                    onEditClick = { showEditProfileDialog = true }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // SECTION: WORKOUT
            item {
                SectionLabel("Workout")
                SectionCard {
                    SettingRow(
                        emoji = "⏱️",
                        label = "Rest Timer",
                        sublabel = "Auto-start between sets",
                        accentColor = S_BlueAccent,
                        trailingContent = {
                            SettingsSwitch(checked = restTimer, onCheckedChange = { settingsViewModel.setRestTimerEnabled(it) }, accentColor = S_BlueAccent)
                        }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    
                    val hours = reminderTime / 60
                    val minutes = reminderTime % 60
                    val timeString = String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)

                    SettingRow(
                        emoji = "🔔",
                        label = "Notifications",
                        sublabel = "Workout reminders at $timeString",
                        accentColor = S_GoldAccent,
                        trailingContent = {
                            SettingsSwitch(
                                checked = notifications, 
                                onCheckedChange = { enabled ->
                                    settingsViewModel.setNotificationsEnabled(enabled)
                                    if (enabled) {
                                        notificationHelper.scheduleWorkoutReminder(reminderTime)
                                    } else {
                                        notificationHelper.cancelWorkoutReminder()
                                    }
                                }, 
                                accentColor = S_GoldAccent
                            )
                        },
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    val newMinutes = h * 60 + m
                                    settingsViewModel.setReminderTime(newMinutes)
                                    if (notifications) {
                                        notificationHelper.scheduleWorkoutReminder(newMinutes)
                                    }
                                },
                                hours,
                                minutes,
                                true
                            ).show()
                        }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    SettingRow(
                        emoji = "📱",
                        label = "Keep Screen On",
                        sublabel = "During active workout",
                        accentColor = S_PurpleAccent,
                        trailingContent = {
                            SettingsSwitch(checked = keepScreenOn, onCheckedChange = { settingsViewModel.setKeepScreenOn(it) }, accentColor = S_PurpleAccent)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // SECTION: UNITS & DISPLAY
            item {
                SectionLabel("Units & Display")
                SectionCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingIconBox(emoji = "⚖️", accentColor = S_BlueAccent)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Weight Unit", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = S_TextPrimary)
                                Text("Choose your measurement", fontSize = 11.sp, color = Color(0xFF475569))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(10.dp))
                                .padding(2.dp)
                        ) {
                            UnitToggle(
                                label = "KG",
                                isSelected = weightUnit == "kg",
                                onClick = { settingsViewModel.setWeightUnit("kg") },
                                modifier = Modifier.weight(1f)
                            )
                            UnitToggle(
                                label = "LBS",
                                isSelected = weightUnit == "lbs",
                                onClick = { settingsViewModel.setWeightUnit("lbs") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // SECTION: DATA & BACKUP
            item {
                SectionLabel("Data & Backup")
                SectionCard {
                    SettingRow(
                        emoji = "☁️", 
                        label = "Backup to Cloud", 
                        sublabel = "Sync your workouts to Firestore", 
                        accentColor = S_BlueAccent, 
                        trailingContent = { 
                            if (backupStatus is SyncStatus.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = S_BlueAccent)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFF475569)) 
                            }
                        },
                        onClick = { settingsViewModel.backupToCloud() }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    SettingRow(
                        emoji = "📥", 
                        label = "Restore from Cloud", 
                        sublabel = "Download your saved data", 
                        accentColor = S_GoldAccent, 
                        trailingContent = { 
                            if (restoreStatus is SyncStatus.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = S_GoldAccent)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFF475569)) 
                            }
                        },
                        onClick = { settingsViewModel.restoreFromCloud() }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    SettingRow(emoji = "📤", label = "Export Data", sublabel = "Download as CSV or JSON", accentColor = S_GreenAccent, 
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFF475569)) })
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // SECTION: ABOUT
            item {
                SectionLabel("About")
                SectionCard {
                    SettingRow(
                        emoji = "⭐", 
                        label = "Rate the App", 
                        sublabel = "Enjoying it? Leave a review", 
                        accentColor = S_GoldAccent, 
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFF475569)) },
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")))
                            }
                        }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    SettingRow(
                        emoji = "🐛", 
                        label = "Report a Bug", 
                        sublabel = "Help us improve", 
                        accentColor = S_BlueAccent, 
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFF475569)) },
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@workoutlogger.com"))
                                putExtra(Intent.EXTRA_SUBJECT, "Bug Report: Workout Logger")
                                putExtra(Intent.EXTRA_TEXT, "Please describe the bug here...")
                            }
                            context.startActivity(Intent.createChooser(intent, "Send Email"))
                        }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    SettingRow(
                        emoji = "ℹ️", 
                        label = "App Version", 
                        sublabel = "v1.0.0 — Up to date ✓", 
                        accentColor = Color.Gray, 
                        trailingContent = null,
                        onClick = {
                            Toast.makeText(context, "You are using the latest version (v1.0.0)", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // SECTION: ACCOUNT
            item {
                SectionLabel("Account")
                SectionCard {
                    SettingRow(
                        emoji = "🗑️",
                        label = "Clear All Data",
                        sublabel = "Permanently delete workouts",
                        accentColor = S_DangerRed,
                        isDanger = true,
                        onClick = { showClearDialog = true }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    SettingRow(
                        emoji = "🚪",
                        label = "Log Out",
                        sublabel = "Sign out of your account",
                        accentColor = S_DangerRed,
                        isDanger = true,
                        onClick = { showLogoutDialog = true }
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Delete everything?", fontWeight = FontWeight.Bold) },
            text = { Text("This will remove all workouts and cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { 
                    settingsViewModel.clearAllData()
                    showClearDialog = false 
                }) {
                    Text("Delete", color = S_DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = S_TextMuted)
                }
            },
            containerColor = S_SurfaceDark,
            titleContentColor = S_TextPrimary,
            textContentColor = Color(0xFFCBD5E1)
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out of your account?") },
            confirmButton = {
                TextButton(onClick = { 
                    onSignOut()
                    showLogoutDialog = false 
                }) {
                    Text("Log Out", color = S_BlueAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = S_TextMuted)
                }
            },
            containerColor = S_SurfaceDark,
            titleContentColor = S_TextPrimary,
            textContentColor = Color(0xFFCBD5E1)
        )
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = customUsername ?: userData?.username ?: "",
            currentPhotoUri = profilePictureUri,
            onDismiss = { showEditProfileDialog = false },
            onSave = { newName, newPhotoUri ->
                settingsViewModel.updateCustomUsername(newName)
                settingsViewModel.updateProfilePictureUri(newPhotoUri)
                showEditProfileDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    currentName: String,
    currentPhotoUri: String?,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    val context = LocalContext.current
    var name by remember(currentName) { mutableStateOf(currentName) }
    var photoUri by remember(currentPhotoUri) { mutableStateOf(currentPhotoUri) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { 
                val savedUri = FileUtil.saveImageToInternalStorage(context, it)
                photoUri = savedUri
            }
        }
    )

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = S_SurfaceDark),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Enhanced Avatar Picker
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(S_BgDark)
                        .border(2.dp, S_BlueAccent.copy(alpha = 0.5f), CircleShape)
                        .clickable { 
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.CameraAlt, 
                            null, 
                            tint = S_BlueAccent, 
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    // Edit Overlay Label
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "CHANGE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Styled Input Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = S_BlueAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = S_BlueAccent,
                        unfocusedLabelColor = S_TextMuted
                    )
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Action Buttons with equal balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cancel", color = S_TextMuted, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = { onSave(name, photoUri) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = S_BlueAccent),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

// REUSABLE COMPONENTS
@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        color = S_TextMuted,
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(S_SurfaceDark, Color(0xFF101830))
                    )
                )
        ) {
            content()
        }
    }
}

@Composable
fun SettingRow(
    emoji: String,
    label: String,
    sublabel: String? = null,
    accentColor: Color,
    trailingContent: @Composable (() -> Unit)? = null,
    isDanger: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIconBox(emoji = emoji, accentColor = if (isDanger) S_DangerRed else accentColor)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDanger) S_DangerRed else S_TextPrimary
            )
            if (sublabel != null) {
                Text(
                    text = sublabel,
                    fontSize = 11.sp,
                    color = Color(0xFF475569)
                )
            }
        }
        if (trailingContent != null) {
            trailingContent()
        }
    }
}

@Composable
fun SettingIconBox(emoji: String, accentColor: Color) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 18.sp)
    }
}

@Composable
fun SettingsSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, accentColor: Color) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = accentColor,
            uncheckedTrackColor = Color.White.copy(alpha = 0.1f),
            uncheckedBorderColor = Color.Transparent,
            uncheckedThumbColor = Color.Gray
        )
    )
}

@Composable
fun UnitToggle(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val bgColor by animateColorAsState(if (isSelected) S_BlueAccent else Color.Transparent, label = "bg")
    val textColor by animateColorAsState(if (isSelected) Color.White else Color(0xFF64748B), label = "text")
    
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        shadowElevation = if (isSelected) 8.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileCard(
    userData: UserData?, 
    customUsername: String?,
    profilePictureUri: String?,
    streak: Int,
    workouts: String,
    volume: String,
    prs: String,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, S_BlueAccent.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(S_BlueAccent.copy(alpha = 0.15f), S_BgDark)
                    )
                )
                .padding(20.dp)
        ) {
            // Decorative Glow
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
                    .blur(40.dp)
                    .background(S_BlueAccent.copy(alpha = 0.1f), CircleShape)
            )

            Column {
                // TOP SECTION: AVATAR + INFO + EDIT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Box
                    Box(modifier = Modifier.size(72.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Brush.linearGradient(listOf(S_BlueAccent, S_PurpleAccent)))
                                .border(2.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profilePictureUri != null) {
                                AsyncImage(
                                    model = profilePictureUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = (customUsername ?: userData?.username)?.firstOrNull()?.toString() ?: "U",
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        // Online Dot
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 2.dp, y = 2.dp)
                                .background(S_BgDark, CircleShape)
                                .padding(2.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().background(S_GreenAccent, CircleShape))
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val displayName = customUsername ?: userData?.username ?: "User"
                        Text(
                            text = displayName,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val handle = displayName.lowercase().replace(" ", "")
                        Text(
                            text = "@$handle",
                            color = Color(0xFF63B3ED),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Professional Edit Button
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = S_BlueAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // STATS SECTION: Balanced and Spaced
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileStat(workouts, "Workouts")
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.1f)))
                    ProfileStat(volume, "Vol kg")
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.1f)))
                    ProfileStat(prs, "PRs")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // STREAK BADGE: Modernized
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(S_GoldAccent.copy(alpha = 0.15f), Color.Transparent)
                            ), 
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, S_GoldAccent.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔥", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (streak > 0) "$streak-day streak!" else "No active streak",
                        color = S_GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (streak > 0) "Keep it going" else "Start training today!",
                        color = S_TextMuted,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (streak > 0) {
                        Surface(
                            color = S_GoldAccent,
                            shape = CircleShape
                        ) {
                            Text(
                                "ACTIVE", 
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = Color.Black, 
                                fontSize = 9.sp, 
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value, 
            color = Color.White, 
            fontWeight = FontWeight.ExtraBold, 
            fontSize = 16.sp
        )
        Text(
            text = label, 
            color = S_TextMuted, 
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
