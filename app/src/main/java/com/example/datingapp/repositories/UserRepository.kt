package com.example.datingapp.repositories

import com.example.datingapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")

    suspend fun createUser(user: User): Result<User> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("User not authenticated"))
            
            usersCollection.document(userId).set(user, SetOptions.merge()).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<User> {
        return try {
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) {
                Result.success(document.toObject(User::class.java) ?: User())
            } else {
                Result.failure(IllegalStateException("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(userId: String, userData: Map<String, Any>): Result<User> {
        return try {
            usersCollection.document(userId).update(userData).await()
            val updatedUser = usersCollection.document(userId).get().await().toObject(User::class.java) ?: User()
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfileImage(userId: String, imageUrl: String): Result<User> {
        return try {
            val userData = mapOf("profileImageUrl" to imageUrl, "updatedAt" to com.google.firebase.Timestamp.now())
            usersCollection.document(userId).update(userData).await()
            val updatedUser = usersCollection.document(userId).get().await().toObject(User::class.java) ?: User()
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLastOnline(userId: String): Result<Unit> {
        return try {
            val userData = mapOf("lastOnline" to com.google.firebase.Timestamp.now())
            usersCollection.document(userId).update(userData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserByUid(uid: String): Result<User> {
        return try {
            val document = usersCollection.document(uid).get().await()
            if (document.exists()) {
                Result.success(document.toObject(User::class.java) ?: User())
            } else {
                Result.failure(IllegalStateException("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val users = usersCollection
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThan("username", query + "\uf8ff")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(User::class.java) }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNearbyUsers(location: String, radius: Double): Result<List<User>> {
        return try {
            // TODO: Implement geolocation-based search
            // This is a placeholder implementation
            val users = usersCollection
                .whereEqualTo("location", location)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(User::class.java) }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserInterests(userId: String): Result<List<String>> {
        return try {
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) {
                val user = document.toObject(User::class.java) ?: User()
                Result.success(user.interests)
            } else {
                Result.failure(IllegalStateException("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateInterests(userId: String, interests: List<String>): Result<User> {
        return try {
            val userData = mapOf("interests" to interests, "updatedAt" to com.google.firebase.Timestamp.now())
            usersCollection.document(userId).update(userData).await()
            val updatedUser = usersCollection.document(userId).get().await().toObject(User::class.java) ?: User()
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
