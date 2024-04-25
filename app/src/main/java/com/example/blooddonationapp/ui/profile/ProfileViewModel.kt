package com.example.blooddonationapp.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.blooddonationapp.model.Post
import com.example.blooddonationapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance()

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    fun getUserProfile() {
        _profileState.value = ProfileState.Loading
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            database.child("users").child(userId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val userX = snapshot.getValue(User::class.java)!!
                    _profileState.value = ProfileState.Success(userX)
                } else {
                    _profileState.value = ProfileState.Error("No such document")
                }
            }.addOnFailureListener { e ->
                _profileState.value = ProfileState.Error("Error getting user data: ${e.message}")
            }
        }
    }

    fun logout() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val currentUser = auth.currentUser
                currentUser?.let { user ->
                    val userId = user.uid
                    val userMap = HashMap<String, Any>()
                    userMap["fcmToken"] = ""
                    database.child("users").child(userId).updateChildren(userMap)
                        .addOnSuccessListener {
                            auth.signOut()
                        }.addOnFailureListener { e ->
                            _profileState.value =
                                ProfileState.Error("Error updating FCM token: ${e.message}")
                        }
                }
            } else {
                _profileState.value = ProfileState.Error("Error fetching FCM token")
            }
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        _profileState.value = ProfileState.Loading
        val imageName = "image_${auth.currentUser?.uid}.jpg"
        val imageRef = storage.reference.child("images/$imageName")
        val uploadTask = imageRef.putFile(imageUri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val userMap = HashMap<String, Any>()
                userMap["imageUrl"] = downloadUri.toString()
                val currentUser = auth.currentUser
                currentUser?.let { user ->
                    val userId = user.uid
                    database.child("users").child(userId).updateChildren(userMap)
                        .addOnSuccessListener {
                            _profileState.value =
                                ProfileState.Success(User(imageUrl = downloadUri.toString()))
                        }.addOnFailureListener { e ->
                            _profileState.value =
                                ProfileState.Error("Error updating user data: ${e.message}")
                        }
                }
            } else {
                _profileState.value = ProfileState.Error("Error uploading image")
            }
        }
    }
}

sealed class ProfileState {
    data object Loading : ProfileState()
    data class Success(val user: User) : ProfileState()
    data class Error(val message: String) : ProfileState()
}