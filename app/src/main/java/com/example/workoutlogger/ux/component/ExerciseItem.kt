package com.example.workoutlogger.ux.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.workoutlogger.data.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseItem(exercise: Exercise, onDelete : ()  -> Unit, onEdit : () -> Unit) {


    var showDialog by remember { mutableStateOf(false) }

    Card( modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,

        ) {
            Text(text = exercise.name ,
                style = MaterialTheme.typography.titleMedium)

            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "${exercise.weight} kg • ${exercise.sets} sets x ${exercise.reps} reps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
                IconButton(
                    onClick = onEdit
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit exercise"
                    )
                }
                IconButton(
                    onClick = { showDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete exercise"
                    )
                }

            }
        }

        if (showDialog) {

            AlertDialog(
                onDismissRequest = { showDialog = false },

                title = {
                    Text("Delete Exercise")
                },

                text = {
                    Text("Are you sure you want to delete this exercise?")
                },

                confirmButton = {
                    TextButton(
                        onClick = {
                            onDelete()
                            showDialog = false
                        }
                    ) {
                        Text("Delete")
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = { showDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}