package com.example.datingapp.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    var id: String = "",
    var username: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var profileImageUrl: String? = null,
    var bio: String = "",
    var age: Int = 0,
    var gender: String = "",
    var interests: List<String> = emptyList(),
    var location: String = "",
    var verificationStatus: VerificationStatus = VerificationStatus.NOT_VERIFIED,
    var idProofUrl: String? = null,
    var faceVerificationUrl: String? = null,
    var online: Boolean = false,
    var lastSeen: Timestamp = Timestamp.now(),
    var typing: Map<String, Boolean> = emptyMap(), // chatId -> isTyping
    var chats: List<String> = emptyList(), // List of chat IDs
    var lastActivity: Timestamp = Timestamp.now(),
    var lastMessage: String = "",
    var lastMessageTime: Timestamp = Timestamp.now(),
    var unreadCount: Int = 0,
    var status: String = "Online",
    var statusColor: String = "#4CAF50",
    var activity: String = "Active",
    var activityTime: Timestamp = Timestamp.now(),
    var typingStatus: Map<String, String> = emptyMap(), // chatId -> typing message
    var currentChat: String? = null,
    var currentActivity: String? = null,
    var lastLocation: Map<String, Any> = emptyMap(),
    var lastLocationTime: Timestamp = Timestamp.now(),
    var lastCall: Map<String, Any> = emptyMap(),
    var lastCallTime: Timestamp = Timestamp.now()
    val about: String = "",
    val interests: List<String> = listOf(),
    val height: Int = 0,
    val job: String = "",
    val education: String = "",
    val relationshipStatus: RelationshipStatus = RelationshipStatus.SINGLE,
    
    // Preferences
    val matchPreferences: MatchPreferences = MatchPreferences(),
    
    // Statistics
    val stats: UserStats = UserStats(),
    
    // Timestamps
    val lastOnline: Timestamp = Timestamp.now(),
    val lastActive: Timestamp = Timestamp.now(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    
    enum class VerificationStatus {
        NOT_VERIFIED,
        EMAIL_VERIFIED,
        PHONE_VERIFIED,
        DOCUMENT_VERIFIED,
        FACE_VERIFIED,
        FULLY_VERIFIED
    }
    
    enum class Gender {
        MALE, FEMALE, OTHER
    }
    
    enum class RelationshipStatus {
        SINGLE, IN_RELATIONSHIP, MARRIED, DIVORCED, WIDOWED
    }
    
    data class Location(
        val coordinates: GeoPoint? = null,
        val city: String = "",
        val state: String = "",
        val country: String = "",
        val lastUpdated: Timestamp = Timestamp.now()
    )
    
    data class MatchPreferences(
        val minAge: Int = 18,
        val maxAge: Int = 99,
        val minHeight: Int = 140,
        val maxHeight: Int = 220,
        val preferredGenders: List<Gender> = listOf(),
        val distance: Int = 50,
        val showOnlyPremium: Boolean = false
    )
    
    data class UserStats(
        val likesReceived: Int = 0,
        val likesGiven: Int = 0,
        val matches: Int = 0,
        val messagesSent: Int = 0,
        val messagesReceived: Int = 0,
        val callsMade: Int = 0,
        val callsReceived: Int = 0,
        val profileViews: Int = 0,
        val lastMessageTime: Timestamp = Timestamp.now()
    )
    val age: Int = 0,
    val birthDate: Timestamp? = null,
    val location: Location? = null,
    val about: String = "",
    val interests: List<String> = listOf(),
    val gender: Gender = Gender.OTHER,
    val lookingFor: List<Gender> = listOf(),
    val height: Int = 0,
    val job: String = "",
    val education: String = "",
    val relationshipStatus: RelationshipStatus = RelationshipStatus.SINGLE,
    val ethnicity: String = "",
    val religion: String = "",
    val zodiacSign: String = "",
    val languages: List<String> = listOf(),
    val children: ChildrenStatus = ChildrenStatus.NONE,
    val wantChildren: Boolean = false,
    val smoking: Boolean = false,
    val drinking: Boolean = false,
    val diet: String = "",
    val bodyType: BodyType = BodyType.AVERAGE,
    val exercise: ExerciseFrequency = ExerciseFrequency.OCCASIONAL,
    val educationLevel: EducationLevel = EducationLevel.HIGH_SCHOOL,
    val jobTitle: String = "",
    val company: String = "",
    val income: IncomeRange = IncomeRange.UNKNOWN,
    val locationPreferences: List<String> = listOf(),
    val matchPreferences: MatchPreferences = MatchPreferences(),
    val lastOnline: Timestamp = Timestamp.now(),
    val lastActive: Timestamp = Timestamp.now(),
    val isPremium: Boolean = false,
    val premiumUntil: Timestamp? = null,
    val notifications: Notifications = Notifications(),
    val settings: UserSettings = UserSettings(),
    val stats: UserStats = UserStats(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    
    enum class Gender {
        MALE, FEMALE, OTHER
    }
    
    enum class RelationshipStatus {
        SINGLE, IN_RELATIONSHIP, MARRIED, DIVORCED, WIDOWED
    }
    
    enum class VerificationStatus {
        NOT_VERIFIED,
        EMAIL_VERIFIED,
        PHONE_VERIFIED,
        DOCUMENT_VERIFIED,
        FACE_VERIFIED,
        FULLY_VERIFIED
    }
    
    enum class BodyType {
        SLIM, AVERAGE, ATHLETIC, MUSCULAR, CURVY, PEAR_SHAPED, APPLE_SHAPED
    }
    
    enum class ExerciseFrequency {
        NEVER, OCCASIONAL, REGULAR, DAILY
    }
    
    enum class EducationLevel {
        HIGH_SCHOOL, COLLEGE, BACHELORS, MASTERS, DOCTORATE, OTHER
    }
    
    enum class IncomeRange {
        UNKNOWN, LESS_THAN_30K, _30K_TO_60K, _60K_TO_100K, _100K_TO_150K, MORE_THAN_150K
    }
    
    data class Location(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val city: String = "",
        val state: String = "",
        val country: String = "",
        val lastUpdated: Timestamp = Timestamp.now()
    )
    
    data class MatchPreferences(
        val minAge: Int = 18,
        val maxAge: Int = 99,
        val minHeight: Int = 140,
        val maxHeight: Int = 220,
        val preferredGenders: List<Gender> = listOf(),
        val distance: Int = 50,
        val showOnlyPremium: Boolean = false,
        val showOnlyOnline: Boolean = false
    )
    
    data class Notifications(
        val messages: Boolean = true,
        val matches: Boolean = true,
        val likes: Boolean = true,
        val superLikes: Boolean = true,
        val premiumOffers: Boolean = true,
        val dailyDigest: Boolean = true,
        val weeklyDigest: Boolean = true
    )
    
    data class UserSettings(
        val darkMode: Boolean = false,
        val notificationsEnabled: Boolean = true,
        val locationTracking: Boolean = true,
        val showOnlineStatus: Boolean = true,
        val showLastSeen: Boolean = true,
        val hideFromSearch: Boolean = false,
        val allowMessagesFrom: List<String> = listOf("everyone"),
        val ageRange: IntRange = 18..99,
        val distanceRange: Int = 50,
        val languagePreferences: List<String> = listOf()
    )
    
    data class UserStats(
        val likesReceived: Int = 0,
        val likesGiven: Int = 0,
        val matches: Int = 0,
        val messagesSent: Int = 0,
        val messagesReceived: Int = 0,
        val callsMade: Int = 0,
        val callsReceived: Int = 0,
        val profileViews: Int = 0,
        val superLikesReceived: Int = 0,
        val superLikesGiven: Int = 0,
        val lastMessageTime: Timestamp = Timestamp.now()
    )
    
    data class ChildrenStatus(
        val hasChildren: Boolean = false,
        val numberOfChildren: Int = 0,
        val youngestAge: Int? = null,
        val oldestAge: Int? = null,
        val custody: String = "",
        val livingWithChildren: Boolean = false
    )
}
