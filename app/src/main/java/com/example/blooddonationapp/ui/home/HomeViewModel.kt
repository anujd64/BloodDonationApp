package com.example.blooddonationapp.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.paging.PagingConfig
import com.example.blooddonationapp.model.Post
import com.example.blooddonationapp.ui.home.posts.PaginationHelper
import com.example.blooddonationapp.ui.messages.Resource
import com.example.blooddonationapp.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    private val storage = FirebaseStorage.getInstance()

    fun deletePost(postId: String, imageName: String) {
        FirebaseUtil.getPostsCollectionReference().document(postId).delete().addOnCompleteListener {
            Log.d("HomeViewModel", "Post deleted successfully")
        }.addOnFailureListener { e ->
            Log.e("HomeViewModel", "Failed to delete post", e)
        }

        storage.reference.child("images").child(imageName).delete()
            .addOnSuccessListener {
                Log.d("HomeViewModel", "Image deleted successfully")
            }
            .addOnFailureListener { e ->
                Log.e("HomeViewModel", "Failed to delete image", e)
            }

    }

    private val _posts = MutableStateFlow<Resource<List<Post>>>(Resource.Loading())
    val posts: StateFlow<Resource<List<Post>>> = _posts


    fun getPosts() {
         FirebaseUtil.getPostsCollectionReference().orderBy("timestamp", Query.Direction.DESCENDING).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val posts = task.result?.toObjects(Post::class.java)
                _posts.value = Resource.Success(posts!!)
            } else {
                _posts.value = Resource.Error(task.exception?.message ?: "An error occurred")
            }
        }
    }

}