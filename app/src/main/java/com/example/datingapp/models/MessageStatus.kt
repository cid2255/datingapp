package com.example.datingapp.models

enum class MessageStatus {
    SENT, // Message sent but not delivered
    DELIVERED, // Message delivered but not seen
    SEEN // Message seen by recipient
}
