package com.example.datingapp.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

enum class NotificationType {
    LIKE,
    MATCH,
    MESSAGE,
    VISIT
}

data class Notification(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val fromUserId: String = "",
    val type: NotificationType = NotificationType.VISIT,
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
)
