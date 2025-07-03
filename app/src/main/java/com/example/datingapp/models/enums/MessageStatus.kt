package com.example.datingapp.models.enums

enum class MessageStatus(
    val id: String,
    val title: String,
    val description: String,
    val color: Int
) {
    SENT(
        "sent",
        "Sent",
        "Message has been sent",
        0xFF9E9E9E.toInt()
    ),
    DELIVERED(
        "delivered",
        "Delivered",
        "Message has been delivered",
        0xFF7CB342.toInt()
    ),
    READ(
        "read",
        "Read",
        "Message has been read",
        0xFF1B5E20.toInt()
    ),
    FAILED(
        "failed",
        "Failed",
        "Message failed to send",
        0xFFD32F2F.toInt()
    ),
    SCHEDULED(
        "scheduled",
        "Scheduled",
        "Message is scheduled to send",
        0xFF2196F3.toInt()
    ),
    FORWARDING(
        "forwarding",
        "Forwarding",
        "Message is being forwarded",
        0xFF9C27B0.toInt()
    ),
    DELETED(
        "deleted",
        "Deleted",
        "Message has been deleted",
        0xFF757575.toInt()
    ),
    EDITED(
        "edited",
        "Edited",
        "Message has been edited",
        0xFFFFA000.toInt()
    ),
    RECALLED(
        "recalled",
        "Recalled",
        "Message has been recalled",
        0xFF607D8B.toInt()
    );

    companion object {
        fun fromId(id: String): MessageStatus? {
            return values().firstOrNull { it.id == id }
        }
    }
}
