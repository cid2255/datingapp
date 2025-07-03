package com.example.datingapp.verification

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.example.datingapp.R
import com.example.datingapp.models.VerificationOption
import com.example.datingapp.models.VerificationStatus
import com.example.datingapp.models.VerificationType
import com.example.datingapp.models.VerificationLevel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class VerificationFlow(
    private val activity: AppCompatActivity,
    private val option: VerificationOption
) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""

    fun start() {
        when (option.verificationType) {
            VerificationType.EMAIL -> startEmailVerification()
            VerificationType.PHONE -> startPhoneVerification()
            VerificationType.DOCUMENT -> startDocumentVerification()
            VerificationType.FACE -> startFaceVerification()
            VerificationType.PREMIUM -> startPremiumVerification()
            VerificationType.ENTERPRISE -> startEnterpriseVerification()
        }
    }

    private fun startEmailVerification() {
        // Check if email is already verified
        if (auth.currentUser?.isEmailVerified == true) {
            updateVerificationStatus(VerificationStatus.EMAIL_VERIFIED)
            activity.showToast("Email already verified")
            return
        }

        // Send verification email
        auth.currentUser?.sendEmailVerification()
            ?.addOnSuccessListener {
                activity.showToast("Verification email sent. Please check your inbox.")
                updateVerificationStatus(VerificationStatus.EMAIL_VERIFIED)
            }
            ?.addOnFailureListener { exception ->
                activity.showToast("Failed to send verification email: ${exception.message}")
            }
    }

    private fun startPhoneVerification() {
        // TODO: Implement phone verification flow
        activity.showToast("Phone verification coming soon")
    }

    private fun startDocumentVerification() {
        // Show document upload dialog
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        activity.startActivityForResult(intent, DOCUMENT_REQUEST_CODE)
    }

    private fun startFaceVerification() {
        // TODO: Implement face verification flow
        activity.showToast("Face verification coming soon")
    }

    private fun startPremiumVerification() {
        // Check if all basic verifications are complete
        val userRef = db.collection("users").document(currentUserId)
        userRef.get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user?.verificationStatus == VerificationStatus.FULLY_VERIFIED) {
                    updateVerificationStatus(VerificationStatus.PREMIUM_VERIFIED)
                    activity.showToast("Premium verification complete")
                } else {
                    activity.showToast("Please complete basic verifications first")
                }
            }
            .addOnFailureListener { exception ->
                activity.showToast("Failed to check verification status: ${exception.message}")
            }
    }

    private fun startEnterpriseVerification() {
        // TODO: Implement enterprise verification flow
        activity.showToast("Enterprise verification coming soon")
    }

    private fun updateVerificationStatus(status: VerificationStatus) {
        val userRef = db.collection("users").document(currentUserId)
        userRef.update("verificationStatus", status)
            .addOnSuccessListener {
                activity.showToast("Verification status updated")
                activity.updateVerificationBadge(status)
            }
            .addOnFailureListener { exception ->
                activity.showToast("Failed to update verification status: ${exception.message}")
            }
    }

    companion object {
        const val DOCUMENT_REQUEST_CODE = 1001
    }
}
