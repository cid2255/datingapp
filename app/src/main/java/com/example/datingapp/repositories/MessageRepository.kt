package com.example.datingapp.repositories

import com.example.datingapp.models.Message
import com.example.datingapp.models.enums.MessageStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class MessageRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    companion object {
        const val MESSAGES_COLLECTION = "messages"
        const val CHATS_COLLECTION = "chats"
        const val MATCHES_COLLECTION = "matches"
    }

    suspend fun editMessage(messageId: String, newContent: String): Result<Message> {
        return try {
            val messageRef = db.collection(MESSAGES_COLLECTION)
                .document(messageId)

            val message = messageRef.get().await().toObject(Message::class.java)
            
            if (message?.senderId != currentUser?.uid) {
                throw SecurityException("Cannot edit other user's message")
            }

            // Update message with edit details
            val editedMessage = message.copy(
                content = newContent,
                editHistory = message.editHistory?.toMutableList()?.apply {
                    add(
                        mapOf(
                            "timestamp" to Date(),
                            "content" to message.content,
                            "editorId" to currentUser?.uid
                        )
                    )
                } ?: listOf(
                    mapOf(
                        "timestamp" to Date(),
                        "content" to message.content,
                        "editorId" to currentUser?.uid
                    )
                ),
                status = MessageStatus.EDITED.id,
                editedAt = Date()
            )

            // Update message
            messageRef.set(editedMessage).await()

            // Update chat last message
            val chatRef = db.collection(CHATS_COLLECTION)
                .whereArrayContains("messageIds", messageId)
                .get()
                .await()
                .documents
                .firstOrNull()

            chatRef?.reference?.update(
                "lastMessage", editedMessage.content,
                "lastMessageTimestamp", editedMessage.timestamp
            )?.await()

            Result.success(editedMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recallMessage(messageId: String): Result<Message> {
        return try {
            val messageRef = db.collection(MESSAGES_COLLECTION)
                .document(messageId)

            val message = messageRef.get().await().toObject(Message::class.java)
            
            if (message?.senderId != currentUser?.uid) {
                throw SecurityException("Cannot recall other user's message")
            }

            // Update message with recall details
            val recalledMessage = message.copy(
                content = "[Message recalled]",
                status = MessageStatus.RECALLED.id,
                recalledAt = Date()
            )

            // Update message
            messageRef.set(recalledMessage).await()

            // Update chat last message
            val chatRef = db.collection(CHATS_COLLECTION)
                .whereArrayContains("messageIds", messageId)
                .get()
                .await()
                .documents
                .firstOrNull()

            chatRef?.reference?.update(
                "lastMessage", recalledMessage.content,
                "lastMessageTimestamp", recalledMessage.timestamp
            )?.await()

            Result.success(recalledMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forwardMessage(messageId: String, targetChatId: String): Result<Message> {
        return try {
            val messageRef = db.collection(MESSAGES_COLLECTION)
                .document(messageId)

            val message = messageRef.get().await().toObject(Message::class.java)
            
            // Create new forwarded message
            val forwardedMessage = message.copy(
                id = UUID.randomUUID().toString(),
                senderId = currentUser?.uid ?: "",
                chatId = targetChatId,
                status = MessageStatus.FORWARDING.id,
                forwardedFrom = message.chatId,
                forwardedAt = Date()
            )

            // Save new message
            db.collection(MESSAGES_COLLECTION)
                .document(forwardedMessage.id)
                .set(forwardedMessage)
                .await()

            // Update chat
            db.collection(CHATS_COLLECTION)
                .document(targetChatId)
                .update(
                    "messageIds", FieldValue.arrayUnion(forwardedMessage.id),
                    "lastMessage", forwardedMessage.content,
                    "lastMessageTimestamp", forwardedMessage.timestamp
                )
                .await()

            Result.success(forwardedMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun scheduleMessage(message: Message, scheduleTime: Date): Result<Message> {
        return try {
            val scheduledMessage = message.copy(
                status = MessageStatus.SCHEDULED.id,
                scheduledAt = scheduleTime
            )

            // Save scheduled message
            db.collection(MESSAGES_COLLECTION)
                .document(message.id)
                .set(scheduledMessage)
                .await()

            // Update chat
            db.collection(CHATS_COLLECTION)
                .document(message.chatId)
                .update(
                    "scheduledMessageIds", FieldValue.arrayUnion(message.id)
                )
                .await()

            Result.success(scheduledMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
