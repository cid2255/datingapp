package com.example.datingapp.repositories

import com.example.datingapp.models.Like
import com.example.datingapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class LikeRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    companion object {
        const val LIKES_COLLECTION = "likes"
        const val MATCHES_COLLECTION = "matches"
        const val USERS_COLLECTION = "users"
    }

    suspend fun likeUser(targetUserId: String): Result<Like> {
        return try {
            val like = Like(
                id = UUID.randomUUID().toString(),
                likerId = currentUser?.uid ?: "",
                likedUserId = targetUserId,
                timestamp = Date()
            )

            // Save like
            db.collection(LIKES_COLLECTION)
                .document("${currentUser?.uid}_$targetUserId")
                .set(like)
                .await()

            // Check for match
            val existingLike = db.collection(LIKES_COLLECTION)
                .document("${targetUserId}_${currentUser?.uid}")
                .get()
                .await()

            if (existingLike.exists()) {
                // Create match
                val match = createMatch(currentUser?.uid ?: "", targetUserId)
                db.collection(MATCHES_COLLECTION)
                    .document("${currentUser?.uid}_$targetUserId")
                    .set(match)
                    .await()
            }

            Result.success(like)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlikeUser(targetUserId: String): Result<Unit> {
        return try {
            // Delete like
            db.collection(LIKES_COLLECTION)
                .document("${currentUser?.uid}_$targetUserId")
                .delete()
                .await()

            // Check if match exists and delete if no longer valid
            val otherLike = db.collection(LIKES_COLLECTION)
                .document("${targetUserId}_${currentUser?.uid}")
                .get()
                .await()

            if (!otherLike.exists()) {
                db.collection(MATCHES_COLLECTION)
                    .document("${currentUser?.uid}_$targetUserId")
                    .delete()
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLikes(userId: String): Result<List<Like>> {
        return try {
            val likes = db.collection(LIKES_COLLECTION)
                .whereEqualTo("likedUserId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Like::class.java) }

            Result.success(likes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMatches(userId: String): Result<List<Match>> {
        return try {
            val matches = db.collection(MATCHES_COLLECTION)
                .whereEqualTo("userId1", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Match::class.java) }

            Result.success(matches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createMatch(userId1: String, userId2: String): Map<String, Any> {
        return mapOf(
            "userId1" to userId1,
            "userId2" to userId2,
            "timestamp" to Date(),
            "lastMessage" to "",
            "unseenMessages" to 0,
            "blocked" to false,
            "archived" to false
        )
    }

    suspend fun blockUser(targetUserId: String): Result<Unit> {
        return try {
            // Update match status
            db.collection(MATCHES_COLLECTION)
                .document("${currentUser?.uid}_$targetUserId")
                .update("blocked", true)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unblockUser(targetUserId: String): Result<Unit> {
        return try {
            // Update match status
            db.collection(MATCHES_COLLECTION)
                .document("${currentUser?.uid}_$targetUserId")
                .update("blocked", false)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun archiveMatch(targetUserId: String): Result<Unit> {
        return try {
            // Update match status
            db.collection(MATCHES_COLLECTION)
                .document("${currentUser?.uid}_$targetUserId")
                .update("archived", true)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unarchiveMatch(targetUserId: String): Result<Unit> {
        return try {
            // Update match status
            db.collection(MATCHES_COLLECTION)
                .document("${currentUser?.uid}_$targetUserId")
                .update("archived", false)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
