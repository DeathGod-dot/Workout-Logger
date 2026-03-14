package com.example.workoutlogger.ux.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.workoutlogger.data.Exercise

@Composable
fun AddExerciseForm(
    exercise: Exercise? = null,
    onSave: (Exercise) -> Unit
) {

    var name by remember { mutableStateOf(exercise?.name ?: "") }

    var sets by remember { mutableStateOf(exercise?.sets?.toString() ?: "") }

    var reps by remember { mutableStateOf(exercise?.reps?.toString() ?: "") }

    var weight by remember { mutableStateOf(exercise?.weight?.toString() ?: "") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Exercise Name") }
            )

            OutlinedTextField(
                value = sets,
                onValueChange = { sets = it },
                label = { Text("Sets") }
            )

            OutlinedTextField(
                value = reps,
                onValueChange = { reps = it },
                label = { Text("Reps") }
            )

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val exercise = Exercise(
                        name = name,
                        sets = sets.toIntOrNull() ?: 0,
                        reps = reps.toIntOrNull() ?: 0,
                        weight = weight.toIntOrNull() ?: 0
                    )

                    onSave(exercise)

                    name = ""
                    sets = ""
                    reps = ""
                    weight = ""

                },
                enabled = name.isNotBlank() &&
                        sets.isNotBlank() &&
                        reps.isNotBlank() &&
                        weight.isNotBlank()
            ) {

                Text("Save")

            }

        }

    }

}