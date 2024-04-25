package com.example.blooddonationapp.ui.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.blooddonationapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class SignupViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference.child("users")

    fun signup(user: User, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener { task ->
                val currentUser = auth.currentUser
                val userId = currentUser?.uid ?: ""
                user.userId = userId
                if (task.isSuccessful) {
                    database.child(userId).setValue(user)
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.d(
                                "User Created", e.toString() + e.message
                            )
                            onError("Error storing user data: ${e.message}")
                        }

                } else {
                    onError("Sign up failed: ${task.exception?.message}")
                }
            }
    }

    fun getFCMToken(
        onError: (String) -> Unit
    ): String {
        var token = ""
        FirebaseMessaging.getInstance().token.addOnCompleteListener { fcmTokenTask ->
            if (fcmTokenTask.isSuccessful) {
                token = fcmTokenTask.result
            } else {
                onError("Error fetching FCM token")
            }
        }
        return token;
    }
}
