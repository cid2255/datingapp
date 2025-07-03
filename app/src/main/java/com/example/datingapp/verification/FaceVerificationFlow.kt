package com.example.datingapp.verification

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.datingapp.R
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import java.util.*

class FaceVerificationFlow(
    private val activity: Activity
) : DialogFragment() {

    private lateinit var detector: FaceDetector
    private val storage = FirebaseStorage.getInstance()
    private val faceRef = storage.reference.child("faces")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_face_verification, container, false)
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val cameraButton = view.findViewById<Button>(R.id.cameraButton)
        val galleryButton = view.findViewById<Button>(R.id.galleryButton)
        val verifyButton = view.findViewById<Button>(R.id.verifyButton)
        val faceImageView = view.findViewById<ImageView>(R.id.faceImageView)

        cameraButton.setOnClickListener { openCamera() }
        galleryButton.setOnClickListener { openGallery() }
        verifyButton.setOnClickListener { verifyFace(faceImageView.drawable) }
    }

    private fun setupFaceDetector() {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        detector = FaceDetection.getClient(options)
    }

    private fun verifyFace(drawable: Drawable?) {
        if (drawable == null) {
            activity.showToast("Please select a face image first")
            return
        }

        val bitmap = (drawable as BitmapDrawable).bitmap
        val image = InputImage.fromBitmap(bitmap, 0)

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isEmpty()) {
                    activity.showToast("No face detected in the image")
                    return@addOnSuccessListener
                }

                // Convert bitmap to byte array
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val bytes = stream.toByteArray()

                // Upload to Firebase Storage
                val fileName = UUID.randomUUID().toString() + "_face_" + System.currentTimeMillis()
                val fileRef = faceRef.child(fileName)

                fileRef.putBytes(bytes)
                    .addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                            saveFaceReference(downloadUri.toString())
                            activity.showToast("Face verification successful")
                            updateVerificationStatus(VerificationStatus.FACE_VERIFIED)
                            dismiss()
                        }
                    }
                    .addOnFailureListener { exception ->
                        activity.showToast("Failed to upload face image: ${exception.message}")
                    }
            }
            .addOnFailureListener { exception ->
                activity.showToast("Face detection failed: ${exception.message}")
            }
    }

    private fun saveFaceReference(url: String) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(currentUserId)
        val faceData = hashMapOf(
            "url" to url,
            "timestamp" to Timestamp.now(),
            "verified" to true
        )
        userRef.collection("verification").document("face").set(faceData)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, FACE_CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, FACE_GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        val imageView = view?.findViewById<ImageView>(R.id.faceImageView)
        when (requestCode) {
            FACE_CAMERA_REQUEST_CODE -> {
                val imageBitmap = data?.extras?.get("data") as Bitmap?
                imageView?.setImageBitmap(imageBitmap)
            }
            FACE_GALLERY_REQUEST_CODE -> {
                val imageUri = data?.data
                imageView?.setImageURI(imageUri)
            }
        }
    }

    companion object {
        private const val FACE_CAMERA_REQUEST_CODE = 1002
        private const val FACE_GALLERY_REQUEST_CODE = 1003
    }
}
