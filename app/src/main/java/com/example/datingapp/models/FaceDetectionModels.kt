package com.example.datingapp.models

import com.google.mlkit.vision.face.Face

data class FaceDetectionResult(
    val success: Boolean,
    val message: String,
    val details: Map<String, Any>,
    val face: Face? = null
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
    val smilingProbability: Float
)
