package com.example.workoutlogger.ux.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.workoutlogger.data.Exercise
import com.example.workoutlogger.data.WorkoutDatabase
import com.example.workoutlogger.ux.component.ExerciseItem
import com.example.workoutlogger.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch

/* -------------------- MAIN SCREEN (Navigation) -------------------- */

@Composable
fun WorkoutScreen() {

    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Workout) }

    val context = LocalContext.current

    /* -------- DATABASE -------- */

    val db = remember {
        Room.databaseBuilder(
            context,
            WorkoutDatabase::class.java,
            "workout_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    val dao = db.exerciseDao()

    /* -------- SHARED VIEWMODEL -------- */

    val viewModel = remember { WorkoutViewModel(dao) }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(

        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },

        bottomBar = {
            NavigationBar {

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
                    snackbarHostState = snackbarHostState
                )

                Screen.Progress -> ProgressScreen(viewModel)

                Screen.Settings -> SettingsScreen()
            }
        }
    }
}
/* -------------------- WORKOUT CONTENT -------------------- */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutContent(
        viewModel: WorkoutViewModel,
        snackbarHostState: SnackbarHostState) {

    val exercises by viewModel.exercises.collectAsState(initial = emptyList())

    val totalExercises = exercises.size
    val totalSets = exercises.sumOf { it.sets }
    val totalVolume = exercises.sumOf { it.sets * it.reps * it.weight }

    var showBottomSheet by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<Exercise?>(null) }

    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Text(
                text = "Workout Logger",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            WorkoutStatsCard(
                totalExercises = totalExercises,
                totalSets = totalSets,
                totalVolume = totalVolume
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap the + button to add your first workout",
                        style = MaterialTheme.typography.bodyMedium
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
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Exercise")
        }
    }

    /* -------- Bottom Sheet -------- */

    if (showBottomSheet) {

        AddExerciseBottomSheet(
            exercise = editingExercise,

            onDismiss = {
                showBottomSheet = false
                editingExercise = null
            },

            onSave = { updatedExercise ->

                scope.launch {

                    if (updatedExercise.id != 0) {
                        viewModel.updateExercise(updatedExercise)
                    } else {
                        viewModel.addExercise(updatedExercise)
                    }
                }

                showBottomSheet = false
                editingExercise = null
            }
        )
    }
}