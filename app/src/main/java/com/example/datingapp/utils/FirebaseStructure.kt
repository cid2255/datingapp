package com.example.datingapp.utils

/**
 * Firebase Firestore Database Structure
 */
object FirebaseStructure {
    // Collections
    const val USERS = "users"
    const val MATCHES = "matches"
    const val MESSAGES = "messages"
    const val LIKES = "likes"
    const val BLOCKS = "blocks"
    const val REPORTS = "reports"
    const val INTERESTS = "interests"
    const val NOTIFICATIONS = "notifications"

    // User Document Fields
    object UserFields {
        const val ID = "id"
        const val USERNAME = "username"
        const val EMAIL = "email"
        const val PASSWORD = "password"
        const val PROFILE_IMAGE_URL = "profileImageUrl"
        const val AGE = "age"
        const val LOCATION = "location"
        const val ABOUT = "about"
        const val INTERESTS = "interests"
        const val GENDER = "gender"
        const val LOOKING_FOR = "lookingFor"
        const val HEIGHT = "height"
        const val JOB = "job"
        const val EDUCATION = "education"
        const val RELATIONSHIP_STATUS = "relationshipStatus"
        const val LAST_ONLINE = "lastOnline"
        const val CREATED_AT = "createdAt"
        const val UPDATED_AT = "updatedAt"
    }

    // Match Document Fields
    object MatchFields {
        const val USER_IDS = "userIds"
        const val MATCHED_AT = "matchedAt"
        const val LAST_MESSAGE = "lastMessage"
        const val LAST_MESSAGE_TIMESTAMP = "lastMessageTimestamp"
    }

    // Message Document Fields
    object MessageFields {
        const val SENDER_ID = "senderId"
        const val RECEIVER_ID = "receiverId"
        const val MESSAGE = "message"
        const val TIMESTAMP = "timestamp"
        const val IS_READ = "isRead"
    }

    // Like Document Fields
    object LikeFields {
        const val LIKED_BY = "likedBy"
        const val LIKED_AT = "likedAt"
    }

    // Block Document Fields
    object BlockFields {
        const val BLOCKED_BY = "blockedBy"
        const val BLOCKED_AT = "blockedAt"
    }

    // Report Document Fields
    object ReportFields {
        const val REPORTED_BY = "reportedBy"
        const val REPORTED_USER = "reportedUser"
        const val REASON = "reason"
        const val REPORTED_AT = "reportedAt"
    }

    // Interest Document Fields
    object InterestFields {
        const val NAME = "name"
        const val CATEGORY = "category"
        const val POPULARITY = "popularity"
    }

    // Notification Document Fields
    object NotificationFields {
        const val USER_ID = "userId"
        const val TYPE = "type"
        const val MESSAGE = "message"
        const val TIMESTAMP = "timestamp"
        const val IS_READ = "isRead"
    }

    // Storage Paths
    object StoragePaths {
        const val PROFILE_IMAGES = "profile_images"
        const val CHAT_IMAGES = "chat_images"
        const val USER_DOCUMENTS = "user_documents"
    }

    // Security Rules
    object SecurityRules {
        const val USER_RULES = """
            match /users/{userId} {
                allow read: if request.auth != null && request.auth.uid == userId;
                allow write: if request.auth != null && request.auth.uid == userId;
            }
        """

        const val MATCH_RULES = """
            match /matches/{matchId} {
                allow read: if request.auth != null && 
                    (resource.data.userIds[0] == request.auth.uid || 
                     resource.data.userIds[1] == request.auth.uid);
                allow write: if request.auth != null && 
                    (resource.data.userIds[0] == request.auth.uid || 
                     resource.data.userIds[1] == request.auth.uid);
            }
        """

        const val MESSAGE_RULES = """
            match /messages/{messageId} {
                allow read: if request.auth != null && 
                    (resource.data.senderId == request.auth.uid || 
                     resource.data.receiverId == request.auth.uid);
                allow write: if request.auth != null && 
                    (resource.data.senderId == request.auth.uid || 
                     resource.data.receiverId == request.auth.uid);
            }
        """
    }
}
