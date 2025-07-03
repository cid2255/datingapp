package com.example.datingapp.repositories

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class VoiceMessageRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    suspend fun uploadVoiceMessage(file: File, chatId: String): Result<String> = flow {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val fileName = "${UUID.randomUUID()}.m4a"
            val storageRef = storage.reference.child("voice_messages/$userId/$fileName")
            
            // Upload to storage
            val uploadTask = storageRef.putFile(Uri.fromFile(file))
            val uploadResult = uploadTask.await()
            
            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await()
            
            // Create message in Firestore
            val message = hashMapOf(
                "senderId" to userId,
                "chatId" to chatId,
                "type" to "voice",
                "mediaUrl" to downloadUrl.toString(),
                "duration" to file.length() / 1000, // Duration in seconds
                "timestamp" to System.currentTimeMillis()
            )
            
            // Add message to chat
            val chatRef = db.collection("chats").document(chatId)
            val messagesRef = chatRef.collection("messages")
            val messageId = messagesRef.document().id
            
            message["id"] = messageId
            
            messagesRef.document(messageId).set(message).await()
            
            emit(Result.success(messageId))
            
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun deleteVoiceMessage(messageId: String, chatId: String): Result<Unit> = flow {
        try {
            val messageRef = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
            
            val message = messageRef.get().await().toObject(Message::class.java)
            
            // Delete from storage
            val storageRef = storage.reference
                .child("voice_messages/${auth.currentUser?.uid}/${message?.mediaUrl?.substringAfterLast('/')}")
            storageRef.delete().await()
            
            // Delete from Firestore
            messageRef.delete().await()
            
            emit(Result.success(Unit))
            
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun updateVoiceMessageStatus(messageId: String, chatId: String, status: String) {
        val messageRef = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            
        messageRef.update("status", status).await()
    }
}
