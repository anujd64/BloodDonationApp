package com.example.blooddonationapp.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference.child("users")

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                        onSuccess()
                    updateFcmToken(onSuccess, onError)
                } else {
                    Log.e("login error", task.exception.toString())
                    onError("Authentication failed")
                }
            }
    }

    private fun updateFcmToken(onSuccess: () -> Unit, onError: (String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val currentUser = auth.currentUser
                currentUser?.let { user ->
                    val userId = user.uid
                    val userMap = HashMap<String, Any>()
                    userMap["fcmToken"] = token
                    database.child(userId).updateChildren(userMap)
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            onError("Error updating FCM token: ${e.message}")
                            Log.e("error", task.exception.toString())
                        }
                }
            } else {
                Log.e("error", task.exception.toString())
                onError("Error fetching FCM token")
            }
        }
    }
}
