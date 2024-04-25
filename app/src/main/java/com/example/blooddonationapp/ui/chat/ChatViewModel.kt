package com.example.blooddonationapp.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blooddonationapp.model.ChatMessage
import com.example.blooddonationapp.model.ChatRoom
import com.example.blooddonationapp.model.User
import com.example.blooddonationapp.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID


class ChatViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private var chatRoomId: String = ""
    private val _otherUser = MutableStateFlow<User>(User())
    val otherUser: StateFlow<User> = _otherUser.asStateFlow()

    private val _currentUser = MutableStateFlow<User>(User())
    private val currentUser: StateFlow<User> = _currentUser.asStateFlow()


    private val _chatRoomState = MutableStateFlow<ChatRoomState>(ChatRoomState.Loading)
    val chatRoomState: StateFlow<ChatRoomState> = _chatRoomState.asStateFlow()

    fun getOtherUserDetails(userId: String) {
        viewModelScope.launch {
            val user = FirebaseUtil.getUserDetails(userId)
            _otherUser.value = user!!
            val user2 = FirebaseUtil.getUserDetails(auth.currentUser?.uid!!)
            _currentUser.value = user2!!
        }
    }

    fun getOrCreateChatRoom(user1: String, user2: String) {
        if (chatRoomId.isEmpty()) {
            getChatRoomId(user1, user2)
        }

        viewModelScope.launch {
            val user1Profile = FirebaseUtil.getUserDetails(user1)!!
            val user2Profile = FirebaseUtil.getUserDetails(user2)!!

            FirebaseUtil.getChatroomReference(chatRoomId).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    val chatRoomModel = result.toObject(ChatRoom::class.java)
                    if (chatRoomModel == null) {
                        val newChatRoom = ChatRoom(
                            chatRoomId = chatRoomId,
                            user1 = user1Profile.userId,
                            user2 = user2Profile.userId,
                            lastMessageTimestamp = null
                        )
                        FirebaseUtil.getChatroomReference(chatRoomId).set(newChatRoom)
                        _chatRoomState.value = ChatRoomState.Success(newChatRoom)
                    } else {
                        _chatRoomState.value = ChatRoomState.Success(chatRoomModel)
                    }
                } else {
                    _chatRoomState.value =
                        ChatRoomState.Error("Error getting chat room: ${task.exception?.message}")
                }
            }
        }
    }

    fun getChatRoomId(user1: String, user2: String) {
        chatRoomId = if (user1.hashCode() < user2.hashCode()) {
            user1 + "_" + user2
        } else {
            user2 + "_" + user1
        }
    }


    private val _sendMessageState = MutableStateFlow<SendMessageState>(SendMessageState.Success)
    val sendMessageState: StateFlow<SendMessageState> = _sendMessageState.asStateFlow()

    fun sendMessage(chatRoom: ChatRoom, message: String) {
        _sendMessageState.value = SendMessageState.Sending

        val timestamp = Timestamp.now()


        chatRoom.lastMessage = message
        chatRoom.lastMessageSenderId = auth.currentUser?.uid ?: ""
        chatRoom.lastMessageTimestamp = timestamp
        val chatMessage = ChatMessage(
            messageId = UUID.randomUUID().toString(),
            message = message,
            senderId = auth.currentUser?.uid!!,
            timestamp = timestamp
        )

        FirebaseUtil.getChatroomReference(chatRoomId).set(chatRoom)
            .addOnCompleteListener { task ->
                _chatRoomState.value = ChatRoomState.Loading
                if (task.isSuccessful) {
                    _chatRoomState.value = ChatRoomState.Success(chatRoom)
                } else {
                    _chatRoomState.value =
                        ChatRoomState.Error("Failed to update chat room: ${task.exception.toString()}")
                }
            }
        FirebaseUtil.getChatroomMessageReference(chatRoomId).add(chatMessage)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _sendMessageState.value = SendMessageState.Success
                    sendNotification(message)
                } else {
                    _sendMessageState.value =
                        SendMessageState.Error("Failed to send message: ${task.exception.toString()}")
                }
            }
    }

    private fun sendNotification(message: String) {

        val fcmToken = _otherUser.value.fcmToken

        val jsonObject = JSONObject()

        val notificationObj = JSONObject()
        notificationObj.put("title", currentUser.value.name + " sent you a message")
        notificationObj.put("body", message)
        notificationObj.put("message", message)

        val data = JSONObject()
        data.put("userId", currentUser.value.userId)

        jsonObject.put("to", fcmToken)
        jsonObject.put("notification", notificationObj)
        jsonObject.put("data", data)



        val JSON: MediaType = "application/json".toMediaType()
        val body = jsonObject.toString().toRequestBody(JSON)
        val client = OkHttpClient()
        val url = "https://fcm.googleapis.com/fcm/send"

        val serverKey = "AAAAM8Ncjbw:APA91bFTytQI2wKEFX-kprS6Atp_mThYCv8WLgM7sISsOejjMNC7azAiY8QTisC0UWV-meZQI1rHoQclDiy1nKvh2m6VpkgCiTlfmGjcq36hWCaP4KM_dZHltyh-6PyK-RblylpMTM5Z"
        val request = okhttp3.Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer $serverKey")
            .build()

        viewModelScope.launch(Dispatchers.IO) {
            client.newCall(request).execute()
        }

    }

    private fun getChatMessagesQuery(): Query {
        return FirebaseUtil.getChatroomMessageReference(chatRoomId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
    }

    fun getQueryOptions(): FirestoreRecyclerOptions<ChatMessage> {
        return FirestoreRecyclerOptions.Builder<ChatMessage>()
            .setQuery(getChatMessagesQuery(), ChatMessage::class.java).build()
    }
}

sealed class ChatRoomState {
    object Loading : ChatRoomState()
    data class Success(val chatRoom: ChatRoom) : ChatRoomState()
    data class Error(val message: String) : ChatRoomState()
}

sealed class SendMessageState {
    object Sending : SendMessageState()
    object Success : SendMessageState()
    data class Error(val message: String) : SendMessageState()
}

