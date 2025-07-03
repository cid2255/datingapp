package com.example.datingapp.models

data class FaceComparisonResult(
    val success: Boolean,
    val similarity: Float,
    val message: String,
    val match: Boolean
)

data class FaceLivenessResult(
    val success: Boolean,
    val isLive: Boolean,
    val message: String
)

data class FaceValidationResult(
    val success: Boolean,
    val message: String,
    val details: Map<String, Any>
)

data class FaceQualityMetrics(
    val faceRatio: Float,
    val rotationX: Float,
    val rotationY: Float,
    val rotationZ: Float,
    val leftEyeOpen: Float,
    val rightEyeOpen: Float,
    val smilingProbability: Float,
    val livenessScore: Float,
    val documentMatchScore: Float,
    val blinkCount: Int,
    val headMovement: Boolean,
    val spoofScore: Float
)

data class FaceVerificationResult(
    val success: Boolean,
    val message: String,
    val qualityMetrics: FaceQualityMetrics,
    val comparisonResult: FaceComparisonResult?,
    val livenessResult: FaceLivenessResult?
)
