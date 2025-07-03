package com.example.datingapp.models

import android.R

data class VerificationOption(
    val id: String,
    val title: String,
    val description: String,
    val icon: Int,
    val verificationType: VerificationType,
    val verificationLevel: VerificationLevel,
    val requiredDocuments: List<String>
)

enum class VerificationType {
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

data class VerificationRequirement(
    val type: VerificationType,
    val level: VerificationLevel,
    val documents: List<String>
)
