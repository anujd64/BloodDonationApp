package com.example.blooddonationapp.utils

import android.icu.text.SimpleDateFormat
import android.util.Log
import com.example.blooddonationapp.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.Locale

object FirebaseUtil {

    private fun currentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    private fun allUserCollectionReference(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("users")
    }

    fun getChatroomReference(chatroomId: String): DocumentReference {
        Log.d("FirebaseUtil", "Chatroom reference: $chatroomId")
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId)
    }
    fun getPostsCollectionReference(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("posts")
    }
    fun getChatroomCollectionReference(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("chatrooms")
    }

    fun getChatroomMessageReference(chatroomId: String): CollectionReference {
        Log.d("FirebaseUtil", "Chatroom message reference: $chatroomId")
        return getChatroomReference(chatroomId).collection("chats")
    }

    fun allChatroomCollectionReference(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("chatrooms")
    }

    fun getOtherUserFromChatroom(userIds: List<String>): DocumentReference {
        return if (userIds[0] == currentUserId()) {
            allUserCollectionReference().document(userIds[1])
        } else {
            allUserCollectionReference().document(userIds[0])
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    fun getCurrentProfilePicStorageRef(): StorageReference {
        return FirebaseStorage.getInstance().reference.child("profile_pic")
            .child(currentUserId()!!)
    }

    fun getOtherProfilePicStorageRef(otherUserId: String): StorageReference {
        return FirebaseStorage.getInstance().reference.child("profile_pic")
            .child(otherUserId)
    }

    suspend fun getUserDetails(userId: String): User? {
        val userSnapshot = FirebaseDatabase.getInstance().reference.child("users").child(userId).get().await()
        return userSnapshot.getValue(User::class.java)
    }

    fun timestampToString(timestamp: Timestamp): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(timestamp.toDate())
    }


}
