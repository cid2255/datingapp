package com.example.datingapp.models.chat

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    LOCATION,
    STICKER
}

data class Message(
    @DocumentId
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val type: MessageType = MessageType.TEXT,
    val content: String = "",
    val mediaUrl: String = "",
    val isRead: Boolean = false,
    val isDelivered: Boolean = false,
    val timestamp: Timestamp = Timestamp.now(),
    val replyToMessageId: String = ""
)
