package com.example.datingapp.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Like(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val likedUserId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isMatch: Boolean = false
)
