package com.example.workoutlogger.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "exercises")
data class  Exercise(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name : String,
    val sets : Int,
    val reps : Int,
    val weight : Int,
    val date : Long
)