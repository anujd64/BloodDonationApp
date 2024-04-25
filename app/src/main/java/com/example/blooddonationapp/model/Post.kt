package com.example.blooddonationapp.model

import com.google.firebase.Timestamp

data class Post(
    val postId: String = "",
    val title: String = "",
    val patientName: String? = null,
    val address: String = "",
    val author: String = "",
    val authorId: String = "",
    val bloodGroup: String = "",
    val phoneNumber: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    var imageUrl: String? = null,
    var imageName: String = ""
)
