package com.example.datingapp.repositories

import com.example.datingapp.models.chat.Chat
import com.example.datingapp.models.chat.Message
import com.example.datingapp.models.chat.MessageType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val chatsCollection = firestore.collection("chats")
    private val messagesCollection = firestore.collection("messages")
    private val storageRef = storage.reference

    suspend fun createChat(user1Id: String, user2Id: String): Result<Chat> {
        return try {
            val chat = Chat(
                userId1 = user1Id,
                userId2 = user2Id,
                lastMessage = "",
                lastMessageTimestamp = com.google.firebase.Timestamp.now(),
                lastMessageSenderId = user1Id
            )

            val chatId = chatsCollection.document().id
            chatsCollection.document(chatId).set(chat).await()
            Result.success(chat.copy(id = chatId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(chatId: String, message: Message): Result<Message> {
        return try {
            val messageId = messagesCollection.document().id
            val messageWithId = message.copy(id = messageId)
            messagesCollection.document(messageId).set(messageWithId).await()

            // Update chat last message
            updateChatLastMessage(chatId, messageWithId)
            Result.success(messageWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateChatLastMessage(chatId: String, message: Message) {
        val updates = hashMapOf(
            "lastMessage" to message.content,
            "lastMessageTimestamp" to message.timestamp,
            "lastMessageSenderId" to message.senderId,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        chatsCollection.document(chatId).update(updates).await()
    }

    suspend fun markMessageAsRead(messageId: String): Result<Unit> {
        return try {
            val updates = hashMapOf(
                "isRead" to true,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            messagesCollection.document(messageId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMessages(chatId: String, limit: Int = 20): Result<List<Message>> {
        return try {
            val messages = messagesCollection
                .whereEqualTo("chatId", chatId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Message::class.java) }
                .reversed()
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadMedia(file: Uri, type: MessageType): Result<String> {
        return try {
            val ref = storageRef.child("chat_media/${auth.currentUser?.uid}/${type.name}/${System.currentTimeMillis()}")
            val uploadTask = ref.putFile(file).await()
            val downloadUrl = ref.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChats(userId: String): Result<List<Chat>> {
        return try {
            val chats = chatsCollection
                .whereArrayContains("userIds", userId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Chat::class.java) }
            Result.success(chats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUnreadCount(chatId: String, count: Int): Result<Unit> {
        return try {
            val updates = hashMapOf(
                "unreadCount" to count,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            chatsCollection.document(chatId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            messagesCollection.document(messageId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteChat(chatId: String): Result<Unit> {
        return try {
            // Delete all messages in the chat
            val messages = messagesCollection
                .whereEqualTo("chatId", chatId)
                .get()
                .await()
                .documents
            
            messages.forEach { message ->
                messagesCollection.document(message.id).delete().await()
            }

            // Delete the chat
            chatsCollection.document(chatId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
