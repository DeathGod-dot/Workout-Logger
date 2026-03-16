package com.example.workoutlogger.ux.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutScreen() {

    val context = LocalContext.current

    val db = remember {
        Room.databaseBuilder(
            context,
            WorkoutDatabase::class.java,
            "workout_database"
        ).build()
    }

    val dao = db.exerciseDao()
    val viewModel = remember { WorkoutViewModel(dao) }
    val exercises by viewModel.exercises.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {

        val savedExercises = dao.getAllExercises()
    }

    val totalExercises = exercises.size
    val totalSets = exercises.sumOf { it.sets }
    val totalVolume = exercises.sumOf { it.sets * it.reps * it.weight }

    var showBottomSheet by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<Exercise?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },

        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Exercise")
            }
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
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

            /* ---------- EMPTY STATE  ---------- */

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

        /* ---------- BOTTOM SHEET ---------- */

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

                            val index = exercises.indexOfFirst { it.id == updatedExercise.id }

                            if (index != -1) {
                            }

                        } else {
                            val newId = dao.insertExercise(updatedExercise)
                        }
                    }
                    showBottomSheet = false
                }
            )
        }
    }
}