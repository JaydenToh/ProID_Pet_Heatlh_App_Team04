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
}