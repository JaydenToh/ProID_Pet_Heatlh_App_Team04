package com.example.myapplication

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseHelper{
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private fun toEmail(username:String): String{
        return if(username.contains("@")){
            username
        }else{
            "$username@navigationUI.com"//fake email
        }
    }

    suspend fun signIn(username: String,password:String): Boolean{
        return try{
            val email = toEmail(username)
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user!=null
        }catch(e: Exception){
            Log.e("FirebaseHelper","Login Failed",e)
            false
        }
    }

    suspend fun signUp(username: String, password: String, role: String): Boolean {
        return try {
            val email = toEmail(username)
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid

            if (userId != null) {
                val userMap = mapOf("email" to email, "role" to role)
                FirebaseFirestore.getInstance().collection("users")
                    .document(userId).set(userMap).await()
                true
            } else false
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Sign Up Failed", e)
            false
        }
    }

    suspend fun saveCompanionState(userId: String, selectedCompanion: String, level: Int, xp: Int, xpGoal: Int, foodBasics: Int) {
        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("companion")
                .document(selectedCompanion)  // Use companion name as the document ID
                .set(
                    mapOf(
                        "level" to level,
                        "xp" to xp,
                        "xpGoal" to xpGoal,
                        "foodBasics" to foodBasics
                    )
                )
                .await()  // Ensure the data is saved before moving to the next screen
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error saving companion data", e)
        }
    }
}

