package com.example.datingapp.services

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.datingapp.models.*
import com.example.datingapp.repository.VerificationRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

class VerificationService(
    private val context: Context,
    private val repository: VerificationRepository
) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""
    private val TAG = "VerificationService"

    suspend fun verifyDocument(
        type: VerificationType,
        fileUri: Uri,
        fileName: String
    ): Result<String> {
        return try {
            // Validate document before upload
            val validation = validateDocument(type, fileUri)
            if (!validation.isSuccess) {
                return Result.failure(validation.error!!)
            }

            // Upload document
            val downloadUrl = repository.uploadVerificationDocument(type, fileUri, fileName)
            
            // Create verification record
            val verificationData = hashMapOf(
                "type" to type.name,
                "url" to downloadUrl,
                "status" to VerificationStatus.PENDING,
                "timestamp" to Timestamp.now(),
                "userId" to currentUserId,
                "documentType" to type.name,
                "fileName" to fileName
            )

            // Save verification record
            val verificationRef = db.collection("verification_records")
                .document("${currentUserId}_${System.currentTimeMillis()}")
            verificationRef.set(verificationData).await()

            // Update user's verification status
            val currentStatus = repository.getVerificationStatus().await()
            val newStatus = calculateNewStatus(currentStatus, type)
            repository.updateVerificationStatus(newStatus).await()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying document: ${e.message}", e)
            Result.failure(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun validateDocument(type: VerificationType, uri: Uri): Result<Unit> {
        return try {
            // Get file size
            val file = context.contentResolver.openInputStream(uri)
            val fileSize = file?.available() ?: 0
            
            // Basic validations
            when (type) {
                VerificationType.DOCUMENT -> {
                    if (fileSize > 5_000_000) { // 5MB limit
                        return Result.failure("Document size exceeds 5MB limit")
                    }
                    // Add more document-specific validations
                }
                VerificationType.FACE -> {
                    if (fileSize > 2_000_000) { // 2MB limit
                        return Result.failure("Face image size exceeds 2MB limit")
                    }
                    // Add face-specific validations
                }
                else -> {
                    // Add other type validations
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error validating document: ${e.message}", e)
            Result.failure(e.message ?: "Unknown error occurred")
        }
    }

    private fun calculateNewStatus(currentStatus: VerificationStatus, type: VerificationType): VerificationStatus {
        return when (type) {
            VerificationType.EMAIL -> {
                if (currentStatus == VerificationStatus.NOT_VERIFIED) {
                    VerificationStatus.EMAIL_VERIFIED
                } else {
                    currentStatus
                }
            }
            VerificationType.PHONE -> {
                if (currentStatus == VerificationStatus.EMAIL_VERIFIED) {
                    VerificationStatus.PHONE_VERIFIED
                } else {
                    currentStatus
                }
            }
            VerificationType.DOCUMENT -> {
                if (currentStatus == VerificationStatus.PHONE_VERIFIED) {
                    VerificationStatus.DOCUMENT_VERIFIED
                } else {
                    currentStatus
                }
            }
            VerificationType.FACE -> {
                if (currentStatus == VerificationStatus.DOCUMENT_VERIFIED) {
                    VerificationStatus.FACE_VERIFIED
                } else {
                    currentStatus
                }
            }
            VerificationType.PREMIUM -> {
                if (currentStatus == VerificationStatus.FACE_VERIFIED) {
                    VerificationStatus.PREMIUM_VERIFIED
                } else {
                    currentStatus
                }
            }
            VerificationType.ENTERPRISE -> {
                if (currentStatus == VerificationStatus.PREMIUM_VERIFIED) {
                    VerificationStatus.FULLY_VERIFIED
                } else {
                    currentStatus
                }
            }
        }
    }

    suspend fun getVerificationRequirements(): List<VerificationRequirement> {
        val requirementsRef = db.collection("verification_requirements")
        val documents = requirementsRef.get().await()
        
        return documents.documents.map { document ->
            val data = document.data ?: emptyMap<String, Any>()
            VerificationRequirement(
                type = VerificationType.valueOf(data["type"] as String),
                level = VerificationLevel.valueOf(data["level"] as String),
                documents = (data["documents"] as? List<String>) ?: emptyList()
            )
        }
    }

    suspend fun getVerificationProgress(): VerificationProgress {
        val currentStatus = repository.getVerificationStatus().await()
        val requirements = getVerificationRequirements().await()
        val completed = requirements.filter { it.type.name in currentStatus.name }
        
        return VerificationProgress(
            currentStatus = currentStatus,
            totalRequirements = requirements.size,
            completedRequirements = completed.size,
            progress = (completed.size.toFloat() / requirements.size.toFloat() * 100).toInt()
        )
    }

    suspend fun verifyFaceImage(imageUri: Uri): Result<String> {
        return try {
            // Validate image
            val validation = validateDocument(VerificationType.FACE, imageUri)
            if (!validation.isSuccess) {
                return Result.failure(validation.error!!)
            }

            // Process image
            val bitmap = context.contentResolver.openInputStream(imageUri)?.use {
                BitmapFactory.decodeStream(it)
            } ?: return Result.failure("Failed to load image")

            // Detect face
            val faceDetection = faceDetectionService.detectFaceInImage(bitmap)
            if (!faceDetection.success) {
                return Result.failure(faceDetection.message)
            }

            // Validate face quality
            val faceQuality = faceDetection.details as? Map<String, Any> ?: emptyMap()
            val metrics = FaceQualityMetrics(
                faceRatio = faceQuality["face_ratio"] as? Float ?: 0f,
                rotationX = faceQuality["rotation_x"] as? Float ?: 0f,
                rotationY = faceQuality["rotation_y"] as? Float ?: 0f,
                rotationZ = faceQuality["rotation_z"] as? Float ?: 0f,
                leftEyeOpen = faceQuality["left_eye_open"] as? Float ?: 0f,
                rightEyeOpen = faceQuality["right_eye_open"] as? Float ?: 0f,
                smilingProbability = faceQuality["smiling_probability"] as? Float ?: 0f,
                livenessScore = 0f,
                documentMatchScore = 0f,
                blinkCount = 0,
                headMovement = false,
                spoofScore = 0f
            )

            // Detect liveness
            val livenessResult = faceLivenessService.detectLiveness(bitmap)
            if (!livenessResult.success) {
                return Result.failure(livenessResult.message)
            }
            metrics.livenessScore = if (livenessResult.isLive) 1f else 0f

            // If document verification exists, compare faces
            val documentFace = getDocumentFace()
            val comparisonResult: FaceComparisonResult? = documentFace?.let { docFace ->
                faceComparisonService.compareFaces(bitmap, docFace)
            }
            
            comparisonResult?.let {
                metrics.documentMatchScore = it.similarity
            }

            // Save face verification record
            val verificationData = hashMapOf(
                "type" to VerificationType.FACE.name,
                "status" to VerificationStatus.PENDING,
                "timestamp" to Timestamp.now(),
                "userId" to currentUserId,
                "qualityMetrics" to metrics,
                "faceData" to faceDetection.details,
                "livenessResult" to livenessResult,
                "comparisonResult" to comparisonResult
            )

            // Save verification record
            val verificationRef = db.collection("face_verification")
                .document("${currentUserId}_${System.currentTimeMillis()}")
            verificationRef.set(verificationData).await()

            // Upload image
            val fileName = "face_${System.currentTimeMillis()}.jpg"
            val downloadUrl = repository.uploadVerificationDocument(VerificationType.FACE, imageUri, fileName)

            // Update user's verification status
            val currentStatus = repository.getVerificationStatus().await()
            val newStatus = calculateNewStatus(currentStatus, VerificationType.FACE)
            repository.updateVerificationStatus(newStatus).await()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying face: ${e.message}", e)
            Result.failure(e.message ?: "Unknown error occurred")
        }
    }
}
