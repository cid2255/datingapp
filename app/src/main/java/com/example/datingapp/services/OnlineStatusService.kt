package com.example.datingapp.services

import android.content.Context
import com.example.datingapp.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class OnlineStatusService(
    private val context: Context,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val currentUserId = auth.currentUser?.uid ?: ""
    private val userRef = db.collection("users").document(currentUserId)
    private val chatRef = db.collection("chats")

    // Online/Offline Status
    suspend fun updateOnlineStatus(isOnline: Boolean) {
        try {
            val status = if (isOnline) "Online" else "Offline"
            val statusColor = if (isOnline) "#4CAF50" else "#BDBDBD"
            val updateData = hashMapOf<String, Any>(
                "online" to isOnline,
                "lastSeen" to com.google.firebase.Timestamp.now(),
                "status" to status,
                "statusColor" to statusColor,
                "lastActivity" to com.google.firebase.Timestamp.now()
            )
            userRef.update(updateData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Typing Status
    suspend fun updateTypingStatus(chatId: String, message: String) {
        try {
            val typingRef = userRef.collection("typing").document(chatId)
            val updateData = hashMapOf(
                "isTyping" to true,
                "message" to message,
                "typingTime" to com.google.firebase.Timestamp.now()
            )
            typingRef.set(updateData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun clearTypingStatus(chatId: String) {
        try {
            val typingRef = userRef.collection("typing").document(chatId)
            typingRef.delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Message Status
    suspend fun updateMessageStatus(chatId: String, message: String) {
        try {
            val updateData = hashMapOf(
                "lastMessage" to message,
                "lastMessageTime" to com.google.firebase.Timestamp.now()
            )
            userRef.update(updateData).await()
            chatRef.document(chatId).update(updateData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Location Status
    suspend fun updateLocationStatus(latitude: Double, longitude: Double) {
        try {
            val updateData = hashMapOf(
                "lastLocation" to mapOf(
                    "latitude" to latitude,
                    "longitude" to longitude
                ),
                "lastLocationTime" to com.google.firebase.Timestamp.now()
            )
            userRef.update(updateData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Call Status
    suspend fun updateCallStatus(callData: Map<String, Any>) {
        try {
            val updateData = hashMapOf(
                "lastCall" to callData,
                "lastCallTime" to com.google.firebase.Timestamp.now()
            )
            userRef.update(updateData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Activity Status
    suspend fun updateActivityStatus(activity: String) {
        try {
            val updateData = hashMapOf(
                "activity" to activity,
                "activityTime" to com.google.firebase.Timestamp.now()
            )
            userRef.update(updateData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Current Chat Status
    suspend fun updateCurrentChat(chatId: String?) {
        try {
            val updateData = mapOf("currentChat" to chatId)
            userRef.update(updateData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Unread Count
    suspend fun updateUnreadCount(chatId: String, count: Int) {
        try {
            val updateData = hashMapOf(
                "unreadCount" to count,
                "lastActivity" to com.google.firebase.Timestamp.now()
            )
            userRef.update(updateData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Current Activity
    suspend fun updateCurrentActivity(activity: String?) {
        try {
            val updateData = mapOf("currentActivity" to activity)
            userRef.update(updateData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Last Seen
    suspend fun updateLastSeen() {
        try {
            val updateData = hashMapOf(
                "lastSeen" to com.google.firebase.Timestamp.now(),
                "lastActivity" to com.google.firebase.Timestamp.now()
            )
            userRef.update(updateData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private var instance: OnlineStatusService? = null

        fun getInstance(context: Context): OnlineStatusService {
            return instance ?: synchronized(this) {
                instance ?: OnlineStatusService(context).also { instance = it }
            }
        }
    }
}
