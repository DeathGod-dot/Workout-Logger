package com.example.workoutlogger.ux.screen


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.workoutlogger.data.Exercise
import com.example.workoutlogger.ux.component.ExerciseItem

@Composable
fun WorkoutScreen() {

    val exercises = remember { mutableStateListOf<Exercise>() }

    val totalExercises = exercises.size
    val totalSets = exercises.sumOf { it.sets }
    val totalVolume = exercises.sumOf { it.sets * it.reps * it.weight }

    var showBottomSheet by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<Exercise?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showBottomSheet = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Exercise"
                )
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

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {

                items(exercises) { exercise ->

                    ExerciseItem(
                        exercise = exercise,
                        onDelete = { exercises.remove(exercise) },
                        onEdit = {
                            editingExercise = exercise
                            showBottomSheet = true
                        }
                    )

                }

            }

        }

        if (showBottomSheet) {

            AddExerciseBottomSheet(

                exercise = editingExercise,

                onDismiss = {
                    showBottomSheet = false
                    editingExercise = null
                },

                onSave = { updatedExercise ->

                    if (editingExercise != null) {

                        val index = exercises.indexOf(editingExercise)

                        if (index != -1) {
                            exercises[index] = updatedExercise
                        }

                    } else {

                        exercises.add(updatedExercise)

                    }

                    showBottomSheet = false
                    editingExercise = null

                }

            )

        }

    }

}