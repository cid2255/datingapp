package com.example.datingapp.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.datingapp.ui.VideoCallActivity
import com.example.datingapp.models.enums.PremiumFeature
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object CallUtils {

    private const val CAMERA_PERMISSION = android.Manifest.permission.CAMERA
    private const val RECORD_AUDIO_PERMISSION = android.Manifest.permission.RECORD_AUDIO
    private const val PERMISSION_REQUEST_CODE = 123

    fun launchVideoCall(context: Context, userId: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        
        // Check if user has premium subscription
        FirebaseFirestore.getInstance().collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                val isPremium = document.getBoolean("isPremium") ?: false
                
                if (!isPremium) {
                    showPremiumDialog(context)
                    return@addOnSuccessListener
                }

                // Check permissions
                if (!hasPermissions(context)) {
                    requestPermissions(context as Activity)
                    return@addOnSuccessListener
                }

                // Launch video call activity
                context.startActivity(Intent(context, VideoCallActivity::class.java))
            }
    }

    private fun hasPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(context, RECORD_AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(CAMERA_PERMISSION, RECORD_AUDIO_PERMISSION),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun showPremiumDialog(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Premium Feature")
            .setMessage("Video calls are a premium feature. Upgrade to premium to access this feature.")
            .setPositiveButton("Upgrade") { _, _ ->
                // Launch premium upgrade flow
                launchPremiumUpgrade(context)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun launchPremiumUpgrade(context: Context) {
        // TODO: Implement premium upgrade flow
        // This should open your premium subscription screen
    }

    fun formatDuration(duration: Long): String {
        val minutes = (duration / 1000 / 60).toInt()
        val seconds = (duration / 1000 % 60).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun getDeviceId(): String {
        return Settings.Secure.getString(
            FirebaseFirestore.getInstance().firestore.app.applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"
    }
}
