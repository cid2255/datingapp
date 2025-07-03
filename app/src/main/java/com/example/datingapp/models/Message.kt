package com.example.datingapp.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.UUID

enum class MessageType {
    TEXT,
    IMAGE,
    VOICE,
    LOCATION,
    FILE,
    CONTACT
}

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

data class ContactData(
    val name: String,
    val phone: String
)

enum class MessageStatus {
    SENT,
    DELIVERED,
    SEEN,
    FAILED
}

data class MessageReaction(
    val type: ReactionType,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ReactionType {
    LIKE,
    LOVE,
    SAD,
    ANGRY,
    HAHA
}

data class Message(
    @DocumentId
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val type: MessageType = MessageType.TEXT,
    val timestamp: Timestamp = Timestamp.now(),
    val status: MessageStatus = MessageStatus.SENT,
    val sentTime: Timestamp? = null,
    val deliveredTime: Timestamp? = null,
    val seenTime: Timestamp? = null,
    val statusUpdatedTime: Timestamp? = null,
    val reactions: List<MessageReaction> = emptyList(),
    val location: LocationData? = null,
    val contact: ContactData? = null,
    val fileUrl: String? = null,
    val voiceUrl: String? = null,
    val imageUrls: List<String> = emptyList(),
    val mediaUrl: String? = null,
    val duration: Long? = null,
    val fileName: String? = null,
    val fileSize: Long? = null
) {

    companion object {
        fun createTextMessage(
            senderId: String,
            receiverId: String,
            content: String
        ): Message {
            return Message(
                id = UUID.randomUUID().toString(),
                senderId = senderId,
                receiverId = receiverId,
                type = MessageType.TEXT,
                content = content
            )
        }

        fun createImageMessage(
            senderId: String,
            receiverId: String,
            imageUrl: String
        ): Message {
            return Message(
                id = UUID.randomUUID().toString(),
                senderId = senderId,
                receiverId = receiverId,
                type = MessageType.IMAGE,
                imageUrl = imageUrl
            )
        }

        fun createVoiceMessage(
            senderId: String,
            receiverId: String,
            audioUrl: String,
            duration: Long
        ): Message {
            return Message(
                id = UUID.randomUUID().toString(),
                senderId = senderId,
                receiverId = receiverId,
                type = MessageType.VOICE,
                audioUrl = audioUrl,
                duration = duration
            )
        }

        fun createLocationMessage(
            senderId: String,
            receiverId: String,
            latitude: Double,
            longitude: Double,
            address: String
        ): Message {
            return Message(
                id = UUID.randomUUID().toString(),
                senderId = senderId,
                receiverId = receiverId,
                type = MessageType.LOCATION,
                location = LocationData(latitude, longitude, address)
            )
        }

        fun createFileMessage(
            senderId: String,
            receiverId: String,
            fileUrl: String,
            fileName: String,
            fileSize: Long
        ): Message {
            return Message(
                id = UUID.randomUUID().toString(),
                senderId = senderId,
                receiverId = receiverId,
                type = MessageType.FILE,
                fileUrl = fileUrl,
                fileName = fileName,
                fileSize = fileSize
            )
        }

        fun createContactMessage(
            senderId: String,
            receiverId: String,
            name: String,
            phone: String
        ): Message {
            return Message(
                id = UUID.randomUUID().toString(),
                senderId = senderId,
                receiverId = receiverId,
                type = MessageType.CONTACT,
                contact = ContactData(name, phone)
            )
        }
    }
}
