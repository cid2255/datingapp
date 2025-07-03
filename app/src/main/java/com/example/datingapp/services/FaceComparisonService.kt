package com.example.datingapp.services

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class FaceComparisonService(
    private val context: Context
) {
    private val TAG = "FaceComparisonService"
    private lateinit var interpreter: Interpreter
    private lateinit var gpuDelegate: GpuDelegate
    private val inputSize = 112
    private val inputChannels = 3

    init {
        setupFaceDetector()
        initializeInterpreter()
    }

    private fun setupFaceDetector() {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        FaceDetection.getClient(options)
    }

    private fun initializeInterpreter() {
        try {
            val modelFile = File(context.filesDir, "face_comparison.tflite")
            if (!modelFile.exists()) {
                copyModelFile()
            }

            val options = Interpreter.Options()
            gpuDelegate = GpuDelegate()
            options.addDelegate(gpuDelegate)
            interpreter = Interpreter(modelFile, options)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing interpreter: ${e.message}", e)
        }
    }

    private fun copyModelFile() {
        try {
            val inputStream = context.assets.open("face_comparison.tflite")
            val file = File(context.filesDir, "face_comparison.tflite")
            val outputStream = file.outputStream()
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error copying model file: ${e.message}", e)
        }
    }

    suspend fun compareFaces(
        face1: Bitmap,
        face2: Bitmap
    ): FaceComparisonResult {
        return try {
            val embedding1 = getFaceEmbedding(face1)
            val embedding2 = getFaceEmbedding(face2)
            
            if (embedding1 == null || embedding2 == null) {
                return FaceComparisonResult(
                    success = false,
                    similarity = 0f,
                    message = "Failed to extract face embeddings"
                )
            }

            val similarity = calculateSimilarity(embedding1, embedding2)
            val threshold = 0.6f // Adjust threshold as needed

            FaceComparisonResult(
                success = true,
                similarity = similarity,
                message = "Face comparison successful",
                match = similarity > threshold
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error comparing faces: ${e.message}", e)
            FaceComparisonResult(
                success = false,
                similarity = 0f,
                message = "Error comparing faces: ${e.message}"
            )
        }
    }

    private fun getFaceEmbedding(bitmap: Bitmap): FloatArray? {
        try {
            val inputBuffer = ByteBuffer.allocateDirect(inputSize * inputSize * inputChannels * 4)
            inputBuffer.order(ByteOrder.nativeOrder())
            
            // Preprocess image
            val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
            val pixels = IntArray(inputSize * inputSize)
            resized.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
            
            for (y in 0 until inputSize) {
                for (x in 0 until inputSize) {
                    val pixelValue = pixels[y * inputSize + x]
                    inputBuffer.putFloat(((pixelValue shr 16 and 0xFF) - 127.5f) / 127.5f) // R
                    inputBuffer.putFloat(((pixelValue shr 8 and 0xFF) - 127.5f) / 127.5f) // G
                    inputBuffer.putFloat(((pixelValue and 0xFF) - 127.5f) / 127.5f) // B
                }
            }

            val outputBuffer = FloatArray(512) // Face embedding size
            interpreter.run(inputBuffer, outputBuffer)
            return outputBuffer
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting embedding: ${e.message}", e)
            return null
        }
    }

    private fun calculateSimilarity(embedding1: FloatArray, embedding2: FloatArray): Float {
        var dotProduct = 0f
        var norm1 = 0f
        var norm2 = 0f

        for (i in embedding1.indices) {
            dotProduct += embedding1[i] * embedding2[i]
            norm1 += embedding1[i] * embedding1[i]
            norm2 += embedding2[i] * embedding2[i]
        }

        val norm = Math.sqrt(norm1 * norm2)
        return if (norm > 0) dotProduct / norm.toFloat() else 0f
    }

    fun close() {
        interpreter.close()
        gpuDelegate.close()
    }
}
