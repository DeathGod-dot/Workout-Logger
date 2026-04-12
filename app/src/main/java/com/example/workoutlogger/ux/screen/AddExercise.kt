package com.example.workoutlogger.ux.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workoutlogger.data.Exercise

@Composable
fun AddExerciseForm(
    exercise: Exercise? = null,
    weightUnit: String = "kg",
    onSave: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(exercise) { mutableStateOf(exercise?.name ?: "") }
    var sets by remember(exercise) { mutableStateOf(exercise?.sets?.toString() ?: "") }
    var reps by remember(exercise) { mutableStateOf(exercise?.reps?.toString() ?: "") }
    var weight by remember(exercise) { 
        val initialWeight = if (weightUnit == "lbs") (exercise?.weight?.let { it * 2.20462f }?.let { String.format(java.util.Locale.getDefault(), "%.1f", it) } ?: "")
                           else (exercise?.weight?.let { if (it % 1.0f == 0f) it.toInt().toString() else it.toString() } ?: "")
        mutableStateOf(initialWeight)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A4A)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (exercise == null) "Log Exercise" else "Edit Exercise",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            StyledTextField(
                value = name,
                onValueChange = { name = it },
                label = "Exercise Name (e.g. Bench Press)",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StyledTextField(
                    value = sets,
                    onValueChange = { sets = it },
                    label = "Sets",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
                StyledTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = "Reps",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            StyledTextField(
                value = weight,
                onValueChange = { weight = it },
                label = "Weight ($weightUnit)",
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Decimal
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = {
                        val setsInt = sets.toIntOrNull()
                        val repsInt = reps.toIntOrNull()
                        val weightFloat = weight.toFloatOrNull()

                        if (setsInt != null && repsInt != null && weightFloat != null &&
                            setsInt > 0 && repsInt > 0 && weightFloat > 0f) {

                            val savedWeight = if (weightUnit == "lbs") (weightFloat * 0.453592f) else weightFloat

                            val newExercise = Exercise(
                                id = exercise?.id ?: 0,
                                name = name,
                                sets = setsInt,
                                reps = repsInt,
                                weight = savedWeight,
                                date = System.currentTimeMillis()
                            )

                            onSave(newExercise)
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    enabled = name.isNotBlank() &&
                            (sets.toIntOrNull() ?: 0) > 0 &&
                            (reps.toIntOrNull() ?: 0) > 0 &&
                            (weight.toFloatOrNull() ?: 0f) > 0f
                ) {
                    Text("Save", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF3B82F6),
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color(0xFF3B82F6),
            unfocusedLabelColor = Color(0xFF64748B)
        ),
        singleLine = true
    )
}
