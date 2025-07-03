package com.example.datingapp.services

import android.content.Context
import com.example.datingapp.models.Block
import com.example.datingapp.models.Report
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.*

class BlockService(
    private val context: Context,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val currentUserId = auth.currentUser?.uid ?: ""
    private val blocksRef = db.collection("blocks")
    private val reportsRef = db.collection("reports")

    // Block a user
    suspend fun blockUser(blockedId: String) {
        try {
            val block = Block(
                uid = currentUserId,
                blockedId = blockedId
            )
            blocksRef.document("${currentUserId}_$blockedId").set(block).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to block user")
        }
    }

    // Unblock a user
    suspend fun unblockUser(blockedId: String) {
        try {
            blocksRef.document("${currentUserId}_$blockedId").delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to unblock user")
        }
    }

    // Check if user is blocked
    suspend fun isUserBlocked(blockedId: String): Boolean {
        return try {
            val doc = blocksRef.document("${currentUserId}_$blockedId").get().await()
            doc.exists()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Get blocked users
    suspend fun getBlockedUsers(): List<String> {
        return try {
            val query = blocksRef
                .whereEqualTo("uid", currentUserId)
                .get()
                .await()
            query.documents.map { it.getString("blockedId") ?: "" }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Report a user
    suspend fun reportUser(
        reportedId: String,
        reason: Report.ReportReason,
        description: String = "",
        evidence: List<String> = emptyList()
    ) {
        try {
            val reportId = UUID.randomUUID().toString()
            val report = Report(
                id = reportId,
                reportingUserId = currentUserId,
                reportedUserId = reportedId,
                reason = reason,
                description = description,
                evidence = evidence
            )
            reportsRef.document(reportId).set(report).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to report user")
        }
    }

    // Get reports against a user
    suspend fun getReportsAgainstUser(userId: String): List<Report> {
        return try {
            val query = reportsRef
                .whereEqualTo("reportedUserId", userId)
                .get()
                .await()
            query.documents.mapNotNull { it.toObject(Report::class.java) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Listen to reports
    fun listenToReports(): Flow<List<Report>> = callbackFlow {
        val query = reportsRef
            .whereEqualTo("reportingUserId", currentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.printStackTrace()
                return@addSnapshotListener
            }

            val reports = snapshot?.documents?.mapNotNull { it.toObject(Report::class.java) } ?: emptyList()
            trySend(reports)
        }

        awaitClose {
            listener.remove()
        }
    }

    companion object {
        private var instance: BlockService? = null

        fun getInstance(context: Context): BlockService {
            return instance ?: synchronized(this) {
                instance ?: BlockService(context).also { instance = it }
            }
        }
    }
}
