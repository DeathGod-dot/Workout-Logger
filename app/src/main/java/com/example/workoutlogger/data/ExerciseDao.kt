package com.example.workoutlogger.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise) : Long


    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Query(" SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercisesList(): List<Exercise>

    @Query("DELETE FROM exercises")
    suspend fun clearAllExercises()
}