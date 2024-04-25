package com.example.blooddonationapp.model

import com.google.firebase.Timestamp

data class ChatRoom(
    val chatRoomId: String = "",
    val user1: String = "",
    val user2: String = "",
    var lastMessage: String = "",
    var lastMessageSenderId: String = "",
    var lastMessageTimestamp: Timestamp? = null,
)