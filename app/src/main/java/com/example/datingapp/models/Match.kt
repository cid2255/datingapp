package com.example.datingapp.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Match(
    @DocumentId
    val id: String = "",
    val user1: String = "",
    val user2: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: MatchStatus = MatchStatus.MATCHED,
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val lastMessageSender: String? = null,
    val unreadCount: Int = 0,
    val user1Name: String? = null,
    val user2Name: String? = null,
    val user1Photo: String? = null,
    val user2Photo: String? = null,
    val location: LocationData? = null,
    val distance: Double? = null,
    val premiumFeatures: List<PremiumFeature> = emptyList()
)

enum class MatchStatus {
    MATCHED,
    BLOCKED,
    REPORTED,
    ARCHIVED,
    DELETED
}

enum class PremiumFeature {
    MESSAGE_PRIORITY,
    PHOTO_VERIFICATION,
    LOCATION_PRECISION,
    MATCH_NOTIFICATIONS,
    PROFILE_HIGHLIGHT
}
