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

class FaceLivenessDetectionService(
    private val context: Context
) {
    private val TAG = "FaceLivenessDetectionService"
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
            val modelFile = File(context.filesDir, "face_liveness.tflite")
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
            val inputStream = context.assets.open("face_liveness.tflite")
            val file = File(context.filesDir, "face_liveness.tflite")
            val outputStream = file.outputStream()
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error copying model file: ${e.message}", e)
        }
    }

    suspend fun detectLiveness(bitmap: Bitmap): FaceLivenessResult {
        return try {
            val face = detectFace(bitmap)
            if (face == null) {
                return FaceLivenessResult(
                    success = false,
                    message = "No face detected"
                )
            }

            val isLive = analyzeFace(bitmap)
            FaceLivenessResult(
                success = true,
                isLive = isLive,
                message = if (isLive) "Face is live" else "Face is not live"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting liveness: ${e.message}", e)
            FaceLivenessResult(
                success = false,
                message = "Error detecting liveness: ${e.message}"
            )
        }
    }

    private suspend fun detectFace(bitmap: Bitmap): Face? {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val faces = FaceDetection.getClient().process(inputImage).await()
        return faces.firstOrNull()
    }

    private fun analyzeFace(bitmap: Bitmap): Boolean {
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

            val outputBuffer = FloatArray(1) // Liveness score
            interpreter.run(inputBuffer, outputBuffer)
            
            // Threshold for liveness (adjust as needed)
            return outputBuffer[0] > 0.5f
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing face: ${e.message}", e)
            return false
        }
    }

    fun close() {
        interpreter.close()
        gpuDelegate.close()
    }
}
