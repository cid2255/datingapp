package com.example.datingapp.services

import android.content.Context
import com.example.datingapp.models.MessageStatus
import com.example.datingapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.*

class MessageStatusService(
    private val context: Context,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val currentUserId = auth.currentUser?.uid ?: ""
    private val messageRef = db.collection("messages")

    // Update message status
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        try {
            val messageRef = messageRef.document(messageId)
            val updateData = hashMapOf(
                "status" to status.name,
                "statusUpdatedTime" to Timestamp.now()
            )
            
            when (status) {
                MessageStatus.SENT -> {
                    updateData["sentTime"] = Timestamp.now()
                }
                MessageStatus.DELIVERED -> {
                    updateData["deliveredTime"] = Timestamp.now()
                }
                MessageStatus.SEEN -> {
                    updateData["seenTime"] = Timestamp.now()
                }
            }
            
            messageRef.update(updateData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Get message status
    suspend fun getMessageStatus(messageId: String): MessageStatus? {
        return try {
            val snapshot = messageRef.document(messageId).get().await()
            val status = snapshot.getString("status")
            MessageStatus.valueOf(status ?: "SENT")
        } catch (e: Exception) {
            e.printStackTrace()
            MessageStatus.SENT
        }
    }

    // Listen to message status changes
    fun listenToMessageStatus(messageId: String): Flow<MessageStatus> = callbackFlow {
        val messageRef = messageRef.document(messageId)
        val listener = messageRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                e.printStackTrace()
                return@addSnapshotListener
            }

            val status = snapshot?.getString("status")
            val messageStatus = try {
                MessageStatus.valueOf(status ?: "SENT")
            } catch (e: Exception) {
                MessageStatus.SENT
            }

            trySend(messageStatus)
        }

        awaitClose {
            listener.remove()
        }
    }

    // Mark message as seen
    suspend fun markMessageAsSeen(messageId: String) {
        val message = messageRef.document(messageId).get().await()
        val senderId = message.getString("senderId")
        val receiverId = message.getString("receiverId")

        if (senderId != currentUserId && receiverId == currentUserId) {
            updateMessageStatus(messageId, MessageStatus.SEEN)
        }
    }

    companion object {
        private var instance: MessageStatusService? = null

        fun getInstance(context: Context): MessageStatusService {
            return instance ?: synchronized(this) {
                instance ?: MessageStatusService(context).also { instance = it }
            }
        }
    }
}
