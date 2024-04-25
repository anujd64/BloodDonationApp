package com.example.blooddonationapp.model

import com.google.firebase.Timestamp

data class ChatMessage(
    val messageId: String = "",
    val message: String = "",
    val senderId: String = "",
    val timestamp: Timestamp? = Timestamp.now(),
)
