package com.example.workoutlogger.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Exercise::class],
    version = 2
)

abstract class  WorkoutDatabase : RoomDatabase( ){

    abstract fun exerciseDao() : ExerciseDao

}


