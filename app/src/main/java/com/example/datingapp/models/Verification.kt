package com.example.datingapp.models

import android.R

enum class VerificationStatus {
    NOT_VERIFIED,
    EMAIL_VERIFIED,
    PHONE_VERIFIED,
    DOCUMENT_VERIFIED,
    FACE_VERIFIED,
    FULLY_VERIFIED,
    PREMIUM_VERIFIED,
    ENTERPRISE_VERIFIED
} {
    fun getVerificationText(): String {
        return when (this) {
            NOT_VERIFIED -> "Not Verified"
            EMAIL_VERIFIED -> "Email Verified"
            PHONE_VERIFIED -> "Phone Verified"
            DOCUMENT_VERIFIED -> "Document Verified"
            FACE_VERIFIED -> "Face Verified"
            FULLY_VERIFIED -> "Fully Verified"
            PREMIUM_VERIFIED -> "Premium Verified"
            ENTERPRISE_VERIFIED -> "Enterprise Verified"
        }
    }
    
    fun getVerificationColor(): Int {
        return when (this) {
            NOT_VERIFIED -> android.R.color.holo_red_dark
            EMAIL_VERIFIED -> android.R.color.holo_blue_light
            PHONE_VERIFIED -> android.R.color.holo_green_light
            DOCUMENT_VERIFIED -> android.R.color.holo_orange_light
            FACE_VERIFIED -> android.R.color.holo_purple
            FULLY_VERIFIED -> android.R.color.holo_green_dark
            PREMIUM_VERIFIED -> android.R.color.holo_blue_dark
            ENTERPRISE_VERIFIED -> android.R.color.holo_green_dark
        }
    }
    
    fun getVerificationBadge(): Int {
        return when (this) {
            NOT_VERIFIED -> 0
            EMAIL_VERIFIED -> R.drawable.badge_verification_basic
            PHONE_VERIFIED -> R.drawable.badge_verification_basic
            DOCUMENT_VERIFIED -> R.drawable.badge_verification_premium
            FACE_VERIFIED -> R.drawable.badge_verification_premium
            FULLY_VERIFIED -> R.drawable.badge_verification_premium
            PREMIUM_VERIFIED -> R.drawable.badge_verification_premium
            ENTERPRISE_VERIFIED -> R.drawable.badge_verification_enterprise
        }
    }
    
    fun getVerificationStyle(): VerificationStyle {
        return when (this) {
            NOT_VERIFIED -> VerificationStyle.NONE
            EMAIL_VERIFIED -> VerificationStyle.BASIC
            PHONE_VERIFIED -> VerificationStyle.BASIC
            DOCUMENT_VERIFIED -> VerificationStyle.PREMIUM
            FACE_VERIFIED -> VerificationStyle.PREMIUM
            FULLY_VERIFIED -> VerificationStyle.PREMIUM
            PREMIUM_VERIFIED -> VerificationStyle.PREMIUM
            ENTERPRISE_VERIFIED -> VerificationStyle.ENTERPRISE
        }
    }
}

enum class VerificationStyle {
    NONE,
    BASIC,
    PREMIUM,
    ENTERPRISE
}

data class VerificationData(
    val status: VerificationStatus = VerificationStatus.NOT_VERIFIED,
    val timestamp: Timestamp = Timestamp.now(),
    val documents: List<String> = listOf(),
    val verificationType: VerificationType = VerificationType.NONE,
    val verificationLevel: VerificationLevel = VerificationLevel.BASIC
)

enum class VerificationType {
    NONE,
    EMAIL,
    PHONE,
    DOCUMENT,
    FACE,
    PREMIUM,
    ENTERPRISE
}

enum class VerificationLevel {
    BASIC,
    PREMIUM,
    ENTERPRISE
}
