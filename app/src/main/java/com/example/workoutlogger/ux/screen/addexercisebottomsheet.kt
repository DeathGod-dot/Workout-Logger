package com.example.workoutlogger.ux.screen

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import com.example.workoutlogger.data.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseBottomSheet(
    exercise: Exercise? = null,
    onDismiss: () -> Unit,
    onSave: (Exercise) -> Unit
) {

    ModalBottomSheet(
        onDismissRequest = { onDismiss() }
    ) {

        AddExerciseForm(
            exercise = exercise,
            onSave = onSave
        )

    }

}