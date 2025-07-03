package com.example.datingapp.repository

import com.example.datingapp.models.VerificationStatus
import com.example.datingapp.models.VerificationType
import com.example.datingapp.models.VerificationLevel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

class VerificationRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""

    suspend fun updateVerificationStatus(status: VerificationStatus) {
        val userRef = db.collection("users").document(currentUserId)
        val verificationRef = userRef.collection("verification")
        
        // Update user's verification status
        userRef.update("verificationStatus", status)
            .await()
        
        // Update verification history
        val historyData = hashMapOf(
            "status" to status,
            "timestamp" to Timestamp.now(),
            "userId" to currentUserId
        )
        verificationRef.document("status_${System.currentTimeMillis()}")
            .set(historyData)
            .await()
    }

    suspend fun saveVerificationData(
        type: VerificationType,
        level: VerificationLevel,
        data: Map<String, Any>
    ) {
        val verificationRef = db.collection("users")
            .document(currentUserId)
            .collection("verification")
            .document(type.name.lowercase())

        val verificationData = hashMapOf(
            "type" to type.name,
            "level" to level.name,
            "data" to data,
            "timestamp" to Timestamp.now(),
            "status" to true
        )

        verificationRef.set(verificationData).await()
    }

    suspend fun uploadVerificationDocument(
        type: VerificationType,
        fileUri: Uri,
        fileName: String
    ): String {
        val fileRef = storage.reference.child("verification_documents/$currentUserId/$fileName")
        val uploadTask = fileRef.putFile(fileUri).await()
        
        val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
        
        // Save document reference
        val documentRef = db.collection("users")
            .document(currentUserId)
            .collection("verification_documents")
            .document(fileName)

        val documentData = hashMapOf(
            "type" to type.name,
            "url" to downloadUrl,
            "timestamp" to Timestamp.now(),
            "fileName" to fileName
        )

        documentRef.set(documentData).await()
        return downloadUrl
    }

    suspend fun getVerificationStatus(): VerificationStatus {
        val userRef = db.collection("users").document(currentUserId)
        val document = userRef.get().await()
        return document.data?.get("verificationStatus") as? VerificationStatus 
            ?: VerificationStatus.NOT_VERIFIED
    }

    suspend fun getVerificationHistory(): List<Map<String, Any>> {
        val verificationRef = db.collection("users")
            .document(currentUserId)
            .collection("verification")

        val documents = verificationRef.get().await()
        return documents.documents.map { it.data ?: emptyMap() }
    }

    suspend fun getVerificationDocuments(): List<Map<String, Any>> {
        val documentsRef = db.collection("users")
            .document(currentUserId)
            .collection("verification_documents")

        val documents = documentsRef.get().await()
        return documents.documents.map { it.data ?: emptyMap() }
    }

    suspend fun verifyDocument(documentId: String): Boolean {
        val documentRef = db.collection("users")
            .document(currentUserId)
            .collection("verification_documents")
            .document(documentId)

        val document = documentRef.get().await()
        if (!document.exists()) return false

        // Update verification status
        documentRef.update("verified", true)
            .await()
        
        // Update verification history
        val historyRef = db.collection("users")
            .document(currentUserId)
            .collection("verification_history")
            .document("document_${System.currentTimeMillis()}")

        val historyData = hashMapOf(
            "documentId" to documentId,
            "timestamp" to Timestamp.now(),
            "verified" to true
        )

        historyRef.set(historyData).await()
        return true
    }

    suspend fun getVerificationRequirements(): List<Map<String, Any>> {
        val requirementsRef = db.collection("verification_requirements")
        val requirements = requirementsRef.get().await()
        return requirements.documents.map { it.data ?: emptyMap() }
    }

    suspend fun getVerificationProgress(): Map<String, Any> {
        val userRef = db.collection("users").document(currentUserId)
        val document = userRef.get().await()
        
        val progress = hashMapOf<String, Any>()
        
        // Get current status
        progress["currentStatus"] = document.data?.get("verificationStatus") 
            ?: VerificationStatus.NOT_VERIFIED
        
        // Get completed verifications
        val verificationRef = userRef.collection("verification")
        val verifications = verificationRef.get().await()
        progress["completed"] = verifications.documents.size
        
        // Get total requirements
        val requirementsRef = db.collection("verification_requirements")
        val requirements = requirementsRef.get().await()
        progress["total"] = requirements.documents.size
        
        // Calculate progress
        progress["progress"] = (progress["completed"] as Int / progress["total"] as Int) * 100
        
        return progress
    }
}
