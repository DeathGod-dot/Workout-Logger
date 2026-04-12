package com.example.workoutlogger.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreManager(
    private val dao: ExerciseDao
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun backupToCloud(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
        
        return try {
            val exercises = dao.getAllExercisesList()
            val batch = firestore.batch()
            
            val userExercisesRef = firestore.collection("users")
                .document(user.uid)
                .collection("exercises")

            // For a simple backup, we overwrite existing entries or update them
            exercises.forEach { exercise ->
                val docRef = userExercisesRef.document(exercise.id.toString())
                batch.set(docRef, exercise)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromCloud(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))

        return try {
            val snapshot = firestore.collection("users")
                .document(user.uid)
                .collection("exercises")
                .get()
                .await()

            val exercises = snapshot.toObjects(Exercise::class.java)
            exercises.forEach { exercise ->
                dao.insertExercise(exercise)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
