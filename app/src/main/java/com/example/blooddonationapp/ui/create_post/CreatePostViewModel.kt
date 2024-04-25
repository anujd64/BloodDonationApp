package com.example.blooddonationapp.ui.create_post

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.blooddonationapp.model.Post
import com.example.blooddonationapp.utils.FirebaseUtil
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreatePostViewModel : ViewModel() {

    private val storage = FirebaseStorage.getInstance()
    private val _createPostState = MutableStateFlow<CreatePostState>(CreatePostState.Loading)
    val createPostState: StateFlow<CreatePostState> = _createPostState.asStateFlow()

    fun uploadImageAndCreatePost(postData: Post) {
        _createPostState.value = CreatePostState.Loading  // Set loading state before upload

        val imageName = "image_${System.currentTimeMillis()}.jpg"
        val imageRef = storage.reference.child("images/$imageName")
        val uploadTask = imageRef.putFile(Uri.parse(postData.imageUrl))

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
                postData.imageUrl = downloadUri.toString()
                postData.imageName = imageRef.name

                FirebaseUtil.getPostsCollectionReference().document(postData.postId).set(postData)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            _createPostState.value =
                                CreatePostState.Success  // Update to success state
                            Log.d(
                                "CreatePostViewModel",
                                "Post added successfully with key: ${postData.postId}"
                            )
                        } else {
                            _createPostState.value =
                                CreatePostState.Error(it.exception.toString())  // Update to error state
                            Log.w("CreatePostViewModel", "Error adding post", it.exception)
                        }
                    }
            } else {
                _createPostState.value =
                    CreatePostState.Error("Unknown Error!")  // Update to error state
            }
        }
    }

}

sealed class CreatePostState {
    data object Loading : CreatePostState()
    data object Success : CreatePostState()
    data class Error(val message: String) : CreatePostState()
}
