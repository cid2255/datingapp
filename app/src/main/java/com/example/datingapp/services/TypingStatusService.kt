package com.example.datingapp.services

import android.content.Context
import com.example.datingapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class TypingStatusService(
    private val context: Context,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val currentUserId = auth.currentUser?.uid ?: ""
    private val typingRef = db.collection("users").document(currentUserId).collection("typing")
    private val animationRef = db.collection("animations")

    // Update typing status
    suspend fun updateTypingStatus(chatId: String, status: TypingStatus) {
        try {
            val typingData = hashMapOf(
                "isTyping" to true,
                "message" to status.message,
                "typingTime" to Timestamp.now(),
                "typingUser" to currentUserId,
                "animation" to status.getAnimation()
            )
            typingRef.document(chatId).set(typingData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Clear typing status
    suspend fun clearTypingStatus(chatId: String) {
        try {
            typingRef.document(chatId).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Get typing status for a chat
    suspend fun getTypingStatus(chatId: String): Map<String, Any>? {
        return try {
            val snapshot = typingRef.document(chatId).get().await()
            val data = snapshot.data ?: return null
            // Add animation data if available
            val animation = data["animation"] as? String ?: ""
            if (animation.isNotEmpty()) {
                val animationData = animationRef.document(animation).get().await().data
                data["animationData"] = animationData
            }
            return data
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Listen to typing status changes for a chat
    fun listenToTypingStatus(chatId: String): Flow<Map<String, Any>?> = callbackFlow {
        val typingDocRef = typingRef.document(chatId)
        val typingListener = typingDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                e.printStackTrace()
                return@addSnapshotListener
            }

            snapshot?.data?.let { typingData ->
                trySend(typingData)
            } ?: trySend(null)
        }

        awaitClose {
            typingListener.remove()
        }
    }

    // Get typing status for all chats
    suspend fun getAllTypingStatus(): Map<String, Map<String, Any>> {
        return try {
            val querySnapshot = typingRef.get().await()
            querySnapshot.documents.associate {
                it.id to it.data ?: emptyMap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    // Listen to typing status changes for all chats
    fun listenToAllTypingStatus(): Flow<Map<String, Map<String, Any>>> = callbackFlow {
        val typingListener = typingRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                e.printStackTrace()
                return@addSnapshotListener
            }

            val typingData = snapshot?.documents?.associate {
                it.id to it.data ?: emptyMap()
            } ?: emptyMap()

            trySend(typingData)
        }

        awaitClose {
            typingListener.remove()
        }
    }

    companion object {
        private var instance: TypingStatusService? = null

        fun getInstance(context: Context): TypingStatusService {
            return instance ?: synchronized(this) {
                instance ?: TypingStatusService(context).also { instance = it }
            }
        }
    }
}
