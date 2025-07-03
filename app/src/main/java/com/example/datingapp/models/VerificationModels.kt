package com.example.datingapp.models

import com.google.firebase.Timestamp

data class VerificationRequirement(
    val type: VerificationType,
    val level: VerificationLevel,
    val documents: List<String>
)

data class VerificationProgress(
    val currentStatus: VerificationStatus,
    val totalRequirements: Int,
    val completedRequirements: Int,
    val progress: Int
)

data class VerificationRecord(
    val id: String = "",
    val type: String = "",
    val url: String = "",
    val status: VerificationStatus = VerificationStatus.PENDING,
    val timestamp: Timestamp = Timestamp.now(),
    val userId: String = "",
    val documentType: String = "",
    val fileName: String = ""
)

data class FaceVerificationResult(
    val success: Boolean = false,
    val message: String = "",
    val url: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

data class DocumentValidationResult(
    val success: Boolean = false,
    val error: String? = null,
    val validationDetails: Map<String, Any> = emptyMap()
)
