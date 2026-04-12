package com.example.workoutlogger.ux.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.workoutlogger.data.Exercise
import com.example.workoutlogger.data.WorkoutDatabase
import com.example.workoutlogger.auth.UserData
import com.example.workoutlogger.util.NotificationHelper
import com.example.workoutlogger.ux.component.ExerciseItem
import com.example.workoutlogger.ux.component.RestTimer
import com.example.workoutlogger.viewmodel.SettingsViewModel
import com.example.workoutlogger.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch

/* -------------------- MAIN SCREEN (Navigation) -------------------- */

@Composable
fun WorkoutScreen(
    userData: UserData?,
    onSignOut: () -> Unit = {},
    settingsViewModel: SettingsViewModel,
    notificationHelper: NotificationHelper,
    database: WorkoutDatabase
) {

    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Workout) }
    val weightUnit by settingsViewModel.weightUnit.collectAsStateWithLifecycle()

    val context = LocalContext.current

    /* -------- DATABASE -------- */

    val dao = database.exerciseDao()

    /* -------- SHARED VIEWMODEL -------- */

    val viewModel: WorkoutViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WorkoutViewModel(dao) as T
            }
        }
    )

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },

        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {

                NavigationBarItem(
                    selected = selectedScreen == Screen.Workout,
                    onClick = { selectedScreen = Screen.Workout },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Workout") }
                )

                NavigationBarItem(
                    selected = selectedScreen == Screen.Progress,
                    onClick = { selectedScreen = Screen.Progress },
                    icon = { Icon(Icons.Default.ShowChart, null) },
                    label = { Text("Progress") }
                )

                NavigationBarItem(
                    selected = selectedScreen == Screen.Settings,
                    onClick = { selectedScreen = Screen.Settings },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Settings") }
                )
            }
        }

    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            when (selectedScreen) {

                Screen.Workout -> WorkoutContent(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    weightUnit = weightUnit,
                    settingsViewModel = settingsViewModel,
                    notificationHelper = notificationHelper
                )

                Screen.Progress -> ProgressScreen(
                    viewModel = viewModel,
                    weightUnit = weightUnit
                )

                Screen.Settings -> SettingsScreen(
                    userData = userData,
                    onSignOut = onSignOut,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
/* -------------------- WORKOUT CONTENT -------------------- */

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WorkoutContent(
        viewModel: WorkoutViewModel,
        snackbarHostState: SnackbarHostState,
        weightUnit: String,
        settingsViewModel: SettingsViewModel,
        notificationHelper: NotificationHelper) {

    val exercises by viewModel.exercises.collectAsState(initial = emptyList())
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val restTimerEnabled by settingsViewModel.restTimerEnabled.collectAsStateWithLifecycle()

    val totalExercises = exercises.size
    val totalSets = exercises.sumOf { it.sets }
    val totalVolume = exercises.sumOf { (it.sets * it.reps * it.weight).toDouble() }.toFloat()

    var showBottomSheet by remember { mutableStateOf(false) }
    var showRestTimer by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<Exercise?>(null) }

    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Text(
                text = "Workout Logger",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(24.dp)
            )

            WorkoutStatsCard(
                totalExercises = totalExercises,
                totalSets = totalSets,
                totalVolume = totalVolume,
                weightUnit = weightUnit
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (exercises.isEmpty()) {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "💪 No exercises yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap the + button to add your first workout",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {

                    items(exercises) { exercise ->

                        ExerciseItem(
                            exercise = exercise,
                            modifier = Modifier.animateItemPlacement(),
                            weightUnit = weightUnit,

                            onDelete = {
                                scope.launch {
                                    viewModel.deleteExercise(exercise)

                                    val result = snackbarHostState.showSnackbar(
                                        message = "Exercise Deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )

                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.addExercise(exercise)
                                    }
                                }
                            },

                            onEdit = {
                                editingExercise = exercise
                                showBottomSheet = true
                            }
                        )
                    }
                }
            }
        }

        /* -------- Floating Button -------- */

        FloatingActionButton(
            onClick = { showBottomSheet = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Exercise",
                modifier = Modifier.size(32.dp)
            )
        }
    }

    /* -------- Bottom Sheet -------- */

    if (showRestTimer) {
        BasicAlertDialog(
            onDismissRequest = { showRestTimer = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            RestTimer(onDismiss = { showRestTimer = false })
        }
    }

    if (showBottomSheet) {
        BasicAlertDialog(
            onDismissRequest = {
                showBottomSheet = false
                editingExercise = null
            },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            AddExerciseForm(
                exercise = editingExercise,
                weightUnit = weightUnit,
                onDismiss = {
                    showBottomSheet = false
                    editingExercise = null
                },
                onSave = { updatedExercise ->
                    scope.launch {
                        if (updatedExercise.id != 0) {
                            viewModel.updateExercise(updatedExercise)
                        } else {
                            // Check for PR
                            val previousMax = exercises.filter { it.name == updatedExercise.name }.maxOfOrNull { it.weight } ?: 0f
                            if (updatedExercise.weight > previousMax && notificationsEnabled) {
                                notificationHelper.sendPRNotification(updatedExercise.name, updatedExercise.weight, weightUnit)
                            }

                            viewModel.addExercise(updatedExercise)
                            settingsViewModel.updateStreakAfterWorkout()
                            // Trigger rest timer only for NEW exercises
                            if (restTimerEnabled) {
                                showRestTimer = true
                            }
                        }
                    }

                    showBottomSheet = false
                    editingExercise = null
                }
            )
        }
    }
}
