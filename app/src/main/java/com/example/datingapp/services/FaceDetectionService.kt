package com.example.datingapp.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FaceDetectionService(
    private val context: Context
) {
    private val TAG = "FaceDetectionService"
    private lateinit var detector: FaceDetector
    private val highAccuracyOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.15f)
        .enableTracking()
        .build()

    init {
        detector = FaceDetection.getClient(highAccuracyOpts)
    }

    suspend fun detectFaceInImage(image: Bitmap): FaceDetectionResult {
        return try {
            val inputImage = InputImage.fromBitmap(image, 0)
            val faces = detectFaces(inputImage)
            
            if (faces.isEmpty()) {
                FaceDetectionResult(
                    success = false,
                    message = "No face detected in the image",
                    details = emptyMap()
                )
            } else {
                val face = faces.first()
                val validation = validateFace(face)
                
                FaceDetectionResult(
                    success = validation.success,
                    message = validation.message,
                    details = validation.details,
                    face = face
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting face: ${e.message}", e)
            FaceDetectionResult(
                success = false,
                message = "Error detecting face: ${e.message}",
                details = emptyMap()
            )
        }
    }

    private suspend fun detectFaces(image: InputImage): List<Face> = suspendCancellableCoroutine { cont ->
        detector.process(image)
            .addOnSuccessListener { faces ->
                cont.resume(faces)
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
            .addOnCanceledListener {
                cont.cancel()
            }
    }

    private fun validateFace(face: Face): FaceValidationResult {
        val details = mutableMapOf<String, Any>()
        
        // Face size validation
        val faceSize = face.boundingBox.size
        val imageSize = face.boundingBox.right - face.boundingBox.left
        val faceRatio = faceSize.toFloat() / imageSize.toFloat()
        
        if (faceRatio < 0.2) {
            return FaceValidationResult(
                success = false,
                message = "Face is too small in the image",
                details = mapOf("face_ratio" to faceRatio)
            )
        }
        
        // Face angle validation
        val rotationX = face.headEulerAngleX
        val rotationY = face.headEulerAngleY
        val rotationZ = face.headEulerAngleZ
        
        if (rotationX > 20 || rotationX < -20 ||
            rotationY > 20 || rotationY < -20 ||
            rotationZ > 20 || rotationZ < -20) {
            return FaceValidationResult(
                success = false,
                message = "Face is not facing the camera properly",
                details = mapOf(
                    "rotation_x" to rotationX,
                    "rotation_y" to rotationY,
                    "rotation_z" to rotationZ
                )
            )
        }
        
        // Eye open validation
        val leftEyeOpen = face.leftEyeOpenProbability ?: 0f
        val rightEyeOpen = face.rightEyeOpenProbability ?: 0f
        
        if (leftEyeOpen < 0.5 || rightEyeOpen < 0.5) {
            return FaceValidationResult(
                success = false,
                message = "Eyes should be open",
                details = mapOf(
                    "left_eye_open" to leftEyeOpen,
                    "right_eye_open" to rightEyeOpen
                )
            )
        }
        
        // Smile validation
        val smilingProbability = face.smilingProbability ?: 0f
        if (smilingProbability > 0.7) {
            return FaceValidationResult(
                success = false,
                message = "Please don't smile, neutral face required",
                details = mapOf("smiling_probability" to smilingProbability)
            )
        }
        
        // Save validation details
        details["face_ratio"] = faceRatio
        details["rotation_x"] = rotationX
        details["rotation_y"] = rotationY
        details["rotation_z"] = rotationZ
        details["left_eye_open"] = leftEyeOpen
        details["right_eye_open"] = rightEyeOpen
        details["smiling_probability"] = smilingProbability
        
        return FaceValidationResult(
            success = true,
            message = "Face validation successful",
            details = details
        )
    }

    private fun Rect.size(): Int {
        return width() * height()
    }

    fun close() {
        detector.close()
    }
}
