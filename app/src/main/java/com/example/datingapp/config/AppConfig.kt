package com.example.datingapp.config

object AppConfig {
    // Firebase Configuration
    const val FIREBASE_PROJECT_ID = "your-project-id"
    const val FIREBASE_DATABASE_URL = "https://$FIREBASE_PROJECT_ID.firebaseio.com"
    const val FIREBASE_STORAGE_BUCKET = "$FIREBASE_PROJECT_ID.appspot.com"

    // Cloud Messaging Configuration
    const val FCM_SERVER_KEY = "your-fcm-server-key"
    const val FCM_SENDER_ID = "your-sender-id"

    // Premium Features Configuration
    const val PREMIUM_FEATURES_PRICE = 9.99
    const val PREMIUM_FEATURES_CURRENCY = "USD"
    const val PREMIUM_FEATURES_DURATION_DAYS = 30

    // Location Services Configuration
    const val LOCATION_UPDATE_INTERVAL = 60000L // 1 minute
    const val LOCATION_DISTANCE_THRESHOLD = 1000 // 1 km

    // Call Configuration
    const val CALL_TIMEOUT = 300000L // 5 minutes
    const val CALL_RECORDING_ENABLED = true
    const val CALL_RECORDING_MAX_DURATION = 3600000L // 1 hour

    // Storage Configuration
    const val MAX_PROFILE_IMAGE_SIZE = 5242880L // 5MB
    const val MAX_MESSAGE_IMAGE_SIZE = 5242880L // 5MB
    const val MAX_CALL_RECORDING_SIZE = 5242880L // 5MB

    // Security Configuration
    const val MAX_MESSAGE_LENGTH = 10000
    const val MAX_CALL_DURATION = 3600000L // 1 hour
    const val MAX_LOCATION_UPDATES_PER_HOUR = 60

    // Analytics Configuration
    const val ANALYTICS_ENABLED = true
    const val ANALYTICS_SAMPLE_RATE = 0.1f

    // Error Reporting Configuration
    const val ERROR_REPORTING_ENABLED = true
    const val ERROR_REPORTING_SAMPLE_RATE = 0.1f

    // Firestore Collection Names
    const val COLLECTION_USERS = "users"
    const val COLLECTION_MESSAGES = "messages"
    const val COLLECTION_CALLS = "calls"
    const val COLLECTION_MATCHES = "matches"
    const val COLLECTION_PREMIUM = "premium"
    const val COLLECTION_LOCATION = "location"
    const val COLLECTION_SETTINGS = "settings"

    // Firestore Indexes
    const val INDEX_MATCHES_USER1_USER2 = "matches_user1_user2"
    const val INDEX_MATCHES_USER1_STATUS = "matches_user1_status"
    const val INDEX_MATCHES_USER2_STATUS = "matches_user2_status"
    const val INDEX_MESSAGES_MATCHID = "messages_matchId"
    const val INDEX_CALLS_CALLERID = "calls_callerId"
    const val INDEX_USERS_PREMIUM = "users_premium"

    // Storage Paths
    const val STORAGE_PROFILE_IMAGES = "profile-images"
    const val STORAGE_MESSAGE_IMAGES = "message-images"
    const val STORAGE_CALL_RECORDINGS = "call-recordings"
    const val STORAGE_LOCATION_DATA = "location-data"
}
