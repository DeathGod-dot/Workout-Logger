package com.example.workoutlogger.ux.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.workoutlogger.viewmodel.WorkoutViewModel
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun ProgressScreen(
    viewModel: WorkoutViewModel
) {

    val exercises by viewModel.exercises.collectAsState(initial = emptyList())


    val exerciseNames =
        exercises.map { it.name }.distinct()       /* -------- Get unique exercise names -------- */


    var selectedExercise by remember {
        mutableStateOf(
            exerciseNames.firstOrNull() ?: "")
    }

    LaunchedEffect(exerciseNames) {
        if (exerciseNames.isNotEmpty() && selectedExercise.isEmpty()) {
            selectedExercise = exerciseNames.first()
        }
    }

    var expanded by remember { mutableStateOf(false) }
    var isVolumeMode by remember { mutableStateOf(false) }


    val filteredExercises = exercises
        .filter { it.name == selectedExercise }              /* -------- Filter + sort data -------- */
        .sortedBy { it.date }

    val chartData = if (isVolumeMode) {
        filteredExercises.map {
            (it.sets * it.reps * it.weight).toFloat()
        }
    } else {
        filteredExercises.map {
            it.weight.toFloat()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Progress 📈",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        /* -------- Dropdown -------- */

        Box {

            Button(onClick = { expanded = true }) {
                Text(selectedExercise.ifEmpty { "Select Exercise" })
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                exerciseNames.forEach { name ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            selectedExercise = name
                            expanded = false
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(text = "Weight")

            Switch(
                checked = isVolumeMode,
                onCheckedChange = { isVolumeMode = it }
            )

            Text(text = "Volume")
        }

        Spacer(modifier = Modifier.height(24.dp))

        /* -------- Chart -------- */

        if (chartData.isNotEmpty()) {

            val chartEntryModel = entryModelOf(*chartData.toTypedArray())

            Text("Selected: $selectedExercise")
            Text("Total: ${exercises.size}")
            Text("Filtered: ${filteredExercises.size}")

            Chart(
                chart = lineChart(),
                model = chartEntryModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )

        } else {

            Text("No data for selected exercise")
        }
    }
}