package com.example.datingapp.repository

import com.example.datingapp.models.Chat
import com.example.datingapp.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.*

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""

    // Create or get existing chat
    suspend fun createChat(otherUserId: String): Result<String> {
        return try {
            // Check if chat already exists
            val existingChat = getExistingChat(otherUserId)
            if (existingChat != null) {
                return Result.success(existingChat.id)
            }

            // Create new chat
            val chatId = UUID.randomUUID().toString()
            val chat = Chat(
                id = chatId,
                userId1 = currentUserId,
                userId2 = otherUserId,
                createdAt = com.google.firebase.Timestamp.now(),
                updatedAt = com.google.firebase.Timestamp.now()
            )

            // Add chat to both users' chat lists
            updateUsersChatList(chatId, currentUserId, otherUserId)

            // Save chat
            db.collection("chats").document(chatId)
                .set(chat)
                .await()

            Result.success(chatId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getExistingChat(otherUserId: String): Chat? {
        return try {
            val chats = db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .whereArrayContains("participants", otherUserId)
                .get()
                .await()

            return chats.documents.firstOrNull()?.toObject(Chat::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun updateUsersChatList(chatId: String, userId1: String, userId2: String) {
        val batch = db.batch()

        // Add chat to user1's chat list
        val user1Ref = db.collection("users").document(userId1)
        batch.update(user1Ref, "chats", FieldValue.arrayUnion(chatId))

        // Add chat to user2's chat list
        val user2Ref = db.collection("users").document(userId2)
        batch.update(user2Ref, "chats", FieldValue.arrayUnion(chatId))

        batch.commit().await()
    }

    // Send message
    suspend fun sendMessage(chatId: String, message: Message): Result<String> {
        return try {
            val messageId = UUID.randomUUID().toString()
            val messageWithId = message.copy(id = messageId)

            // Save message
            val messageRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
            messageRef.set(messageWithId).await()

            // Update chat metadata
            updateChatMetadata(chatId, messageWithId)

            Result.success(messageId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateChatMetadata(chatId: String, message: Message) {
        val chatRef = db.collection("chats").document(chatId)
        val chat = chatRef.get().await().toObject(Chat::class.java)

        if (chat != null) {
            val updateData = hashMapOf<String, Any>(
                "lastMessage" to message.content,
                "lastMessageTime" to com.google.firebase.Timestamp.now(),
                "updatedAt" to com.google.firebase.Timestamp.now()
            )

            // Update unread count for receiver
            if (message.senderId != currentUserId) {
                updateData["unreadCount"] = chat.unreadCount + 1
            }

            chatRef.set(updateData, SetOptions.merge()).await()
        }
    }

    // Mark message as seen
    suspend fun markMessageAsSeen(chatId: String, messageId: String): Result<Unit> {
        return try {
            val messageRef = db.collection("chats").document(chatId)
                .collection("messages").document(messageId)

            val message = messageRef.get().await().toObject(Message::class.java)
            if (message != null) {
                val seenBy = message.seenBy.toMutableMap()
                seenBy[currentUserId] = com.google.firebase.Timestamp.now()

                messageRef.update("seenBy", seenBy).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update typing status
    suspend fun updateTypingStatus(chatId: String, isTyping: Boolean): Result<Unit> {
        return try {
            val chatRef = db.collection("chats").document(chatId)
            val chat = chatRef.get().await().toObject(Chat::class.java)

            if (chat != null) {
                val typingUsers = chat.typingUsers.toMutableMap()
                typingUsers[currentUserId] = isTyping

                chatRef.update("typingUsers", typingUsers).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update online status
    suspend fun updateOnlineStatus(isOnline: Boolean): Result<Unit> {
        return try {
            val userRef = db.collection("users").document(currentUserId)
            val updateData = hashMapOf<String, Any>(
                "online" to isOnline,
                "lastSeen" to com.google.firebase.Timestamp.now()
            )
            userRef.update(updateData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get chat messages
    suspend fun getMessages(chatId: String): Result<List<Message>> {
        return try {
            val messages = db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()

            Result.success(messages.documents.map { it.toObject(Message::class.java) ?: Message() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get user chats
    suspend fun getUserChats(): Result<List<Chat>> {
        return try {
            val chats = db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .orderBy("lastMessageTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            Result.success(chats.documents.map { it.toObject(Chat::class.java) ?: Chat() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
