package com.example.datingapp.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Chat(
    @DocumentId
    val id: String = "",
    val userId1: String = "",
    val userId2: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Timestamp = Timestamp.now(),
    val unreadCount: Int = 0,
    val isPremium: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp.now(),
    val typingUsers: Map<String, Boolean> = emptyMap(), // userId -> isTyping
    val participants: List<String> = listOf(userId1, userId2),
    val lastSeen: Map<String, Timestamp> = mapOf(userId1 to Timestamp.now(), userId2 to Timestamp.now())
)
