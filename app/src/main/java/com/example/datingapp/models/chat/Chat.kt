package com.example.datingapp.models.chat

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Chat(
    @DocumentId
    val id: String = "",
    val userId1: String = "",
    val userId2: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp = Timestamp.now(),
    val lastMessageSenderId: String = "",
    val unreadCount: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
