package com.example.datingapp.verification

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import com.example.datingapp.R
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class DocumentUploadHandler(
    private val activity: Activity
) {
    private val storage = FirebaseStorage.getInstance()
    private val documentRef = storage.reference.child("documents")

    fun handleDocumentUpload(uri: Uri, documentType: String) {
        val fileName = UUID.randomUUID().toString() + "_" + documentType + "_" + System.currentTimeMillis()
        val fileRef = documentRef.child(fileName)

        // Show loading
        activity.showToast("Uploading document...")

        // Upload document
        fileRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                // Get download URL
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Save document reference
                    saveDocumentReference(downloadUri.toString(), documentType)
                    activity.showToast("Document uploaded successfully")
                }
                .addOnFailureListener { exception ->
                    activity.showToast("Failed to get document URL: ${exception.message}")
                }
            }
            .addOnFailureListener { exception ->
                activity.showToast("Failed to upload document: ${exception.message}")
            }
    }

    private fun saveDocumentReference(url: String, documentType: String) {
        // TODO: Save document reference to Firestore
        activity.showToast("Document reference saved")
    }
}
