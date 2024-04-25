package com.example.blooddonationapp.model

data class User(
    var userId: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val imageUrl: String = "",
    val bloodGroup: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val fcmToken : String = "",
    val donor: Boolean = false
)
