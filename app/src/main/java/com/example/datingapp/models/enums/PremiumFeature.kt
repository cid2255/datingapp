package com.example.datingapp.models.enums

enum class PremiumFeature(
    val id: String,
    val title: String,
    val description: String,
    val icon: Int,
    val animation: String,
    val type: FeatureType,
    val dailyLimit: Int? = null,
    val monthlyLimit: Int? = null
) {
    UNLIMITED_LIKES(
        "unlimited_likes",
        "Unlimited Likes",
        "Send as many likes as you want without any limits",
        R.drawable.ic_heart,
        "FADE",
        FeatureType.LIKE,
        dailyLimit = null,
        monthlyLimit = null
    ),
    SUPER_LIKES(
        "super_likes",
        "Super Likes",
        "Send special super likes that stand out",
        R.drawable.ic_super_like,
        "BOUNCE",
        FeatureType.LIKE,
        dailyLimit = 5,
        monthlyLimit = 20
    ),
    BOOST(
        "boost",
        "Profile Boost",
        "Get more matches by boosting your profile",
        R.drawable.ic_rocket,
        "SCALE",
        FeatureType.BOOST,
        dailyLimit = 1,
        monthlyLimit = 5
    ),
    REWIND(
        "rewind",
        "Unlimited Rewinds",
        "Undo as many decisions as you want",
        R.drawable.ic_undo,
        "ROTATE",
        FeatureType.REWIND,
        dailyLimit = 3,
        monthlyLimit = 10
    ),
    INVISIBLE_MODE(
        "invisible_mode",
        "Invisible Mode",
        "Browse profiles without being seen",
        R.drawable.ic_invisible,
        "SLIDE",
        FeatureType.BROWSE,
        dailyLimit = 1,
        monthlyLimit = 3
    ),
    UNLIMITED_MESSAGES(
        "unlimited_messages",
        "Unlimited Messages",
        "Send messages to anyone without restrictions",
        R.drawable.ic_message,
        "FADE",
        FeatureType.MESSAGE,
        dailyLimit = null,
        monthlyLimit = null
    ),
    PROFILE_HIGHLIGHT(
        "profile_highlight",
        "Profile Highlight",
        "Make your profile stand out with special effects",
        R.drawable.ic_star,
        "SHINE",
        FeatureType.PROFILE,
        dailyLimit = 1,
        monthlyLimit = 3
    ),
    MESSAGE_PRIORITY(
        "message_priority",
        "Message Priority",
        "Your messages will be shown first",
        R.drawable.ic_priority,
        "PULSE",
        FeatureType.MESSAGE,
        dailyLimit = null,
        monthlyLimit = null
    ),
    MATCH_NOTIFICATIONS(
        "match_notifications",
        "Match Notifications",
        "Get notified about potential matches",
        R.drawable.ic_notification,
        "FLASH",
        FeatureType.NOTIFICATION,
        dailyLimit = null,
        monthlyLimit = null
    ),
    PHOTO_VERIFICATION(
        "photo_verification",
        "Photo Verification",
        "Get your photos verified by our team",
        R.drawable.ic_check,
        "GLOW",
        FeatureType.PROFILE,
        dailyLimit = 1,
        monthlyLimit = 1
    ),
    LOCATION_PRECISION(
        "location_precision",
        "Location Precision",
        "Get more accurate location matches",
        R.drawable.ic_location,
        "PIN",
        FeatureType.MATCH,
        dailyLimit = null,
        monthlyLimit = null
    );

    companion object {
        fun fromId(id: String): PremiumFeature? {
            return values().firstOrNull { it.id == id }
        }

        fun getFeaturesByType(type: FeatureType): List<PremiumFeature> {
            return values().filter { it.type == type }
        }
    }

    enum class FeatureType {
        LIKE,
        BOOST,
        REWIND,
        BROWSE,
        MESSAGE,
        NOTIFICATION,
        PROFILE,
        MATCH
    }
}

    companion object {
        fun fromId(id: String): PremiumFeature? {
            return values().firstOrNull { it.id == id }
        }
    }
}
