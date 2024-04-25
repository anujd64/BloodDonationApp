package com.example.blooddonationapp.ui.messages

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.blooddonationapp.model.ChatRoom
import com.example.blooddonationapp.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecentChatsViewModel : ViewModel() {

    fun getUser1Id(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    private fun getChatList() : Query {

        val currentUser = FirebaseAuth.getInstance().currentUser?.uid


        return FirebaseFirestore.getInstance()
            .collection("chatrooms")
            .where(
                Filter.or(
                    Filter.equalTo("user1", currentUser),
                    Filter.equalTo("user2", currentUser)
                )
            ).orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

    }

    private val _chats = MutableStateFlow<Resource<List<ChatRoom>>>(Resource.Loading())
    val chats = _chats.asStateFlow()
    fun getChatListResource(){
        _chats.value = Resource.Loading()
        getChatList().get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val chatList = mutableListOf<ChatRoom>()
                for (document in task.result!!) {
                    val chatRoom = document.toObject(ChatRoom::class.java)
                    chatList.add(chatRoom)
                }
                _chats.value = Resource.Success(chatList)
            }else {
                // Log error
                _chats.value = Resource.Error("Error getting documents: ", null)
                Log.d("RecentChatsFragment", "Error getting documents: ", task.exception)
            }
        }
    }

    fun deleteChatRoom(chatRoomId: String, onResult: (success: Boolean) -> Unit) {
        FirebaseUtil.getChatroomReference(chatRoomId).collection("chats").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    document.reference.delete()
                }
            }else {
                Log.d("RecentChatsViewModel", "Error deleting chatroom", task.exception)
                onResult(false)
            }
        }
        FirebaseUtil.getChatroomReference(chatRoomId).delete().addOnCompleteListener {task ->
            if (task.isSuccessful) {
                Log.d("RecentChatsViewModel", "Chatroom deleted successfully")
            }else {
                Log.d("RecentChatsViewModel", "Error deleting chatroom", task.exception)
            }
            onResult(task.isSuccessful)
        }
    }
}

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}