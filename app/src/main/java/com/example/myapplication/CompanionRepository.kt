package com.example.myapplication

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class CompanionRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun uid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

    suspend fun saveCompanionState(
        selectedCompanion: String,
        level: Int = 1,
        xp: Int = 0,
        xpGoal: Int = 100,
        foodBasics: Int = 0
    ) {
        val data = hashMapOf(
            "selectedCompanion" to selectedCompanion,
            "level" to level,
            "xp" to xp,
            "xpGoal" to xpGoal,
            "inventory" to hashMapOf(
                "food_basics" to foodBasics
            ),
            "updatedAt" to FieldValue.serverTimestamp(),
            // only sets createdAt if it doesn't exist (merge keeps existing)
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("companion")
            .document(uid())
            .set(data, SetOptions.merge())
            .await()
    }
}
