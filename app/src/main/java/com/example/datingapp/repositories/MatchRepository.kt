package com.example.datingapp.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.datingapp.models.Match
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val currentUserId = auth.currentUser?.uid ?: ""

    fun getMatches(): Flow<List<Match>> = flow {
        try {
            val matches = mutableListOf<Match>()
            
            // Query for matches where current user is user1
            val user1Query = firestore.collection("matches")
                .whereEqualTo("user1", currentUserId)
                .get()
                .await()
            
            // Query for matches where current user is user2
            val user2Query = firestore.collection("matches")
                .whereEqualTo("user2", currentUserId)
                .get()
                .await()
            
            // Combine both queries
            matches.addAll(user1Query.documents.map { it.toObject(Match::class.java) ?: Match() })
            matches.addAll(user2Query.documents.map { it.toObject(Match::class.java) ?: Match() })
            
            // Remove duplicates and sort by timestamp
            val uniqueMatches = matches.distinctBy { 
                when (it.user1 == currentUserId) {
                    true -> it.user2
                    false -> it.user1
                }
            }.sortedByDescending { it.timestamp }
            
            emit(uniqueMatches)
            
        } catch (e: Exception) {
            throw Exception("Failed to get matches: ${e.message}")
        }
    }

    fun getMatchWithUser(userId: String): Flow<Match?> = flow {
        try {
            val match = firestore.collection("matches")
                .whereEqualTo("user1", currentUserId)
                .whereEqualTo("user2", userId)
                .get()
                .await()
                .documents.firstOrNull()?.toObject(Match::class.java)
                
            if (match == null) {
                val reverseMatch = firestore.collection("matches")
                    .whereEqualTo("user1", userId)
                    .whereEqualTo("user2", currentUserId)
                    .get()
                    .await()
                    .documents.firstOrNull()?.toObject(Match::class.java)
                
                emit(reverseMatch)
            } else {
                emit(match)
            }
        } catch (e: Exception) {
            throw Exception("Failed to get match: ${e.message}")
        }
    }

    fun createMatch(user1: String, user2: String): Flow<Match> = flow {
        try {
            val match = Match(
                user1 = user1,
                user2 = user2,
                timestamp = System.currentTimeMillis(),
                status = MatchStatus.MATCHED
            )
            
            firestore.collection("matches")
                .add(match)
                .await()
            
            emit(match)
        } catch (e: Exception) {
            throw Exception("Failed to create match: ${e.message}")
        }
    }

    fun updateMatchStatus(matchId: String, status: MatchStatus): Flow<Unit> = flow {
        try {
            firestore.collection("matches")
                .document(matchId)
                .update("status", status)
                .await()
            
            emit(Unit)
        } catch (e: Exception) {
            throw Exception("Failed to update match status: ${e.message}")
        }
    }

    fun deleteMatch(matchId: String): Flow<Unit> = flow {
        try {
            firestore.collection("matches")
                .document(matchId)
                .delete()
                .await()
            
            emit(Unit)
        } catch (e: Exception) {
            throw Exception("Failed to delete match: ${e.message}")
        }
    }
}
