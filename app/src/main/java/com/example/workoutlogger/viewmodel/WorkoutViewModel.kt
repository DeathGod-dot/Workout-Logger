package com.example.workoutlogger.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workoutlogger.data.Exercise
import com.example.workoutlogger.data.ExerciseDao
import kotlinx.coroutines.launch

class WorkoutViewModel(
    private val dao: ExerciseDao
) : ViewModel() {

    val exercises = dao.getAllExercises()



    fun addExercise(exercise: Exercise){
        viewModelScope.launch {
            dao.insertExercise(exercise)
        }
    }

    fun deleteExercise(exercise: Exercise){
        viewModelScope.launch {
            dao.deleteExercise(exercise)
        }
    }

    fun updateExercise(exercise: Exercise){
        viewModelScope.launch {
            dao.updateExercise(exercise)
        }
    }
}
