package com.example.datingapp.models.chat

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    LOCATION,
    STICKER,
    DOCUMENT,
    GIF,
    REACTION,
    POLL,
    CONTACT,
    FILE,
    LINK,
    VOICE,
    VOICE_NOTE,
    VIDEO_CALL,
    AUDIO_CALL,
    SCREEN_SHARE
}

enum class MessageStatus {
    SENT,
    DELIVERED,
    READ,
    FAILED
}

enum class MessagePriority {
    NORMAL,
    HIGH
}

data class ChatMessage(
    @DocumentId
    val id: String = "",
    
    // Message content
    val content: String = "",
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String = "",
    val thumbnailUrl: String = "",
    val duration: Long = 0L, // For audio/video
    val latitude: Double = 0.0, // For location
    val longitude: Double = 0.0, // For location
    
    // Reply information
    val replyTo: String? = null, // ID of the message being replied to
    val replyContent: String? = null, // Content of the message being replied to
    val replyType: MessageType? = null, // Type of the message being replied to
    
    // Forward information
    val isForwarded: Boolean = false, // Indicates if this is a forwarded message
    val forwardCount: Int = 0, // Number of times this message has been forwarded
    val reaction: String = "", // For reactions
    
    // Message metadata
    val senderId: String = "",
    val receiverId: String = "",
    val chatId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val status: MessageStatus = MessageStatus.SENT,
    val priority: MessagePriority = MessagePriority.NORMAL,
    
    // Message flags
    val isDeleted: Boolean = false,
    val isEdited: Boolean = false,
    val isForwarded: Boolean = false,
    val isPinned: Boolean = false,
    
    // Reply information
    val replyToMessageId: String = "",
    val replyToContent: String = "",
    val replyToSenderName: String = "",
    
    // Quote information
    val quoteMessageId: String = "",
    val quoteContent: String = "",
    val quoteSenderName: String = "",
    
    // File information
    val fileName: String = "",
    val fileSize: Long = 0L,
    val fileType: String = "",
    
    // Location information
    val placeName: String = "",
    val placeAddress: String = "",
    
    // Reaction information
    val reactions: Map<String, String> = mapOf(), // userId -> emoji
    
    // Forward information
    val forwardCount: Int = 0,
    val forwardHistory: List<String> = listOf(), // List of chatIds
    
    // Edit information
    val editedAt: Timestamp? = null,
    val originalContent: String = "",
    
    // Additional metadata
    val metadata: Map<String, Any> = mapOf()
)
