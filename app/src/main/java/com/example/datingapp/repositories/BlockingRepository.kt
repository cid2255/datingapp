package com.example.datingapp.repositories

import com.example.datingapp.models.Block
import com.example.datingapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class BlockingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    companion object {
        const val BLOCKS_COLLECTION = "blocks"
        const val REPORTS_COLLECTION = "reports"
        const val USERS_COLLECTION = "users"
    }

    suspend fun blockUser(targetUserId: String): Result<Block> {
        return try {
            val block = Block(
                id = UUID.randomUUID().toString(),
                blockerId = currentUser?.uid ?: "",
                blockedUserId = targetUserId,
                timestamp = Date(),
                reason = "",
                evidence = ""
            )

            // Save block
            db.collection(BLOCKS_COLLECTION)
                .document("${currentUser?.uid}_$targetUserId")
                .set(block)
                .await()

            // Update match status if exists
            val matchDoc = db.collection("matches")
                .document("${currentUser?.uid}_$targetUserId")
                .get()
                .await()

            if (matchDoc.exists()) {
                db.collection("matches")
                    .document("${currentUser?.uid}_$targetUserId")
                    .update("blocked", true)
                    .await()
            }

            Result.success(block)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unblockUser(targetUserId: String): Result<Unit> {
        return try {
            // Delete block
            db.collection(BLOCKS_COLLECTION)
                .document("${currentUser?.uid}_$targetUserId")
                .delete()
                .await()

            // Update match status if exists
            val matchDoc = db.collection("matches")
                .document("${currentUser?.uid}_$targetUserId")
                .get()
                .await()

            if (matchDoc.exists()) {
                db.collection("matches")
                    .document("${currentUser?.uid}_$targetUserId")
                    .update("blocked", false)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportUser(targetUserId: String, reason: String, evidence: String?): Result<Unit> {
        return try {
            val report = mapOf(
                "reportId" to UUID.randomUUID().toString(),
                "reporterId" to currentUser?.uid ?: "",
                "reportedUserId" to targetUserId,
                "timestamp" to Date(),
                "reason" to reason,
                "evidence" to evidence ?: "",
                "status" to "pending",
                "adminId" to ""
            )

            // Save report
            db.collection(REPORTS_COLLECTION)
                .document("${UUID.randomUUID()}_${System.currentTimeMillis()}")
                .set(report)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBlockedUsers(): Result<List<Block>> {
        return try {
            val blocks = db.collection(BLOCKS_COLLECTION)
                .whereEqualTo("blockerId", currentUser?.uid)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Block::class.java) }

            Result.success(blocks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReports(): Result<List<Map<String, Any>>> {
        return try {
            val reports = db.collection(REPORTS_COLLECTION)
                .whereEqualTo("status", "pending")
                .get()
                .await()
                .documents
                .map { it.data ?: emptyMap() }

            Result.success(reports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun handleReport(reportId: String, status: String, adminId: String): Result<Unit> {
        return try {
            db.collection(REPORTS_COLLECTION)
                .document(reportId)
                .update(
                    "status" to status,
                    "adminId" to adminId
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
