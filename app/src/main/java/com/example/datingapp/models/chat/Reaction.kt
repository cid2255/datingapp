package com.example.datingapp.models.chat

import java.util.*

data class Reaction(
    val messageId: String,
    val userId: String,
    val reactionType: ReactionType,
    val timestamp: Date = Date(),
    val isRemoved: Boolean = false
)
