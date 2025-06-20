package com.example.voyago.model

import android.util.Log
import com.example.voyago.Collections
import com.google.common.io.Files.getFileExtension
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.Serializable
import java.util.Date

data class User(
    var id: Int = 0,
    var uid: String = "",
    var firstname: String = "",
    var surname: String = "",
    var username: String = "",
    var country: String = "",
    var email: String = "",
    var userDescription: String = "",
    var dateOfBirth: Timestamp = Timestamp(Date(0)),
    var password: String = "",
    var profilePictureUrl: String? = null,
    var typeTravel: List<TypeTravel> = emptyList(),
    var desiredDestination: List<String> = emptyList(),
    var rating: Float = 0f,
    var reliability: Int = 0
) : Serializable {
    fun isValid(): Boolean {
        return firstname.isNotBlank() &&
                surname.isNotBlank() &&
                username.isNotBlank() &&
                country.isNotBlank() &&
                email.isNotBlank() &&
                userDescription.isNotBlank() &&
                dateOfBirth.toDate().time > 0 &&
                password.isNotBlank()
    }


    // 添加获取头像 URL 的方法
    suspend fun getProfilePhoto(): String? {
        return try {
            if (profilePictureUrl.isNullOrEmpty()) {
                // 如果没有头像，返回默认头像
                return com.example.voyago.StorageHelper.getImageDownloadUrl("users/default_avatar.jpg")
            }

            when {
                // 如果已经是完整的 HTTP URL，直接返回
                profilePictureUrl!!.startsWith("http") -> profilePictureUrl

                // 如果是 content:// URI，说明是本地文件，需要上传
                profilePictureUrl!!.startsWith("content://") -> {
                    Log.w("User", "Profile picture is still a local URI: $profilePictureUrl")
                    // 这种情况应该在编辑时上传到 Firebase Storage
                    null
                }

                // 如果包含路径分隔符，说明是 Firebase Storage 路径
                profilePictureUrl!!.contains("/") -> {
                    val storageRef = Firebase.storage.reference.child(profilePictureUrl!!)
                    storageRef.downloadUrl.await().toString()
                }

                // 否则假设在 users/ 目录下
                else -> {
                    val storageRef = Firebase.storage.reference.child("users/$profilePictureUrl")
                    storageRef.downloadUrl.await().toString()
                }
            }
        } catch (e: Exception) {
            Log.e("User", "Failed to get profile photo URL for $profilePictureUrl", e)
            // 返回默认头像
            com.example.voyago.StorageHelper.getImageDownloadUrl("users/default_avatar.jpg")
        }
    }

    // 设置头像并上传到 Firebase Storage
    suspend fun setProfilePhoto(imageUri: android.net.Uri): Boolean {
        return try {
            val extension = getFileExtension(imageUri.toString())
            val newPath = "users/${id}_avatar.$extension"

            val (success, url) = com.example.voyago.StorageHelper.uploadImageToStorage(
                imageUri,
                newPath
            )

            if (success && url != null) {
                profilePictureUrl = newPath
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("User", "Failed to upload profile photo", e)
            false
        }
    }

}

class UserModel {

    //Create a new user in the database
    fun createUser(newUser: User, onResult: (Boolean, User?) -> Unit): User {
        val firestore = Firebase.firestore
        val counterRef = firestore.collection("metadata").document("userCounter")

        var userToReturn = User()

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(counterRef)
            val lastUserId = snapshot.getLong("lastUserId") ?: 0
            val newUserId = lastUserId + 1
            transaction.update(counterRef, "lastUserId", newUserId)
            val userWithId = newUser.copy(id = newUserId.toInt())
            val userDocRef = firestore.collection("users").document(newUserId.toString())
            transaction.set(userDocRef, userWithId)
            userToReturn =
                userWithId
            userWithId
        }.addOnSuccessListener { user ->
            onResult(true, user)
        }.addOnFailureListener { e ->
            onResult(false, null)
        }
        return userToReturn
    }

    //SUBSET OF USER LIST

    //Get a list of users given their usernames
    fun getUsersFromUsernames(usernameList: List<String>): Flow<List<User>> =
        callbackFlow {//Observes update from the Server

            val query = Collections.users
                .whereIn("username", usernameList)

            val listener = query.orderBy("id")
                .addSnapshotListener { s, er ->
                    if (s != null)
                        trySend(s.toObjects(User::class.java))
                    else {
                        Log.e("Error", er.toString())
                        trySend(emptyList())
                    }
                }
            awaitClose {
                listener.remove()
            }
        }

    //Get a list of users given their ids
    fun getUsers(userIds: List<Int>): Flow<List<User>> = callbackFlow {
        if (userIds.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val query = Collections.users
            .whereIn("id", userIds)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (snapshot != null) {
                trySend(snapshot.toObjects(User::class.java))
            } else {
                Log.e("Error", error.toString())
                trySend(emptyList())
            }
        }

        awaitClose {
            listener.remove()
        }
    }

    //Get a user given its id
    fun getUser(userId: Int): Flow<User> = callbackFlow {
        if (userId <= 0) {
            close(IllegalArgumentException("Invalid userId"))
            return@callbackFlow
        }

        val query = Collections.users
            .whereEqualTo("id", userId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("getUser", "Errore nella query: ", error)
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val user = snapshot.toObjects(User::class.java).firstOrNull()
                if (user != null) {
                    trySend(user).isSuccess
                }
            }
        }

        awaitClose {
            listener.remove()
        }
    }

    //EDIT USER
    fun editUserData(updatedUser: User) {
        val userId = updatedUser.id.toString()
        val docRef = Collections.users.document(userId)
        docRef.set(updatedUser)
    }

    fun getUserByEmail(email: String): Flow<User?> = callbackFlow {
        val query = FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("email", email)
            .limit(1)

        val listener = query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val user = snapshots?.documents?.firstOrNull()?.toObject(User::class.java)
            trySend(user)
        }

        awaitClose { listener.remove() }
    }

    //MANAGEMENT OF RELIABILITY

    //Function that updates the reliability of a user
    fun updateUserReliability(userId: String, delta: Int, onResult: (Boolean) -> Unit) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentReliability = snapshot.getLong("reliability")?.toInt() ?: 100
            val newReliability = (currentReliability + delta).coerceIn(0, 100)
            transaction.update(userRef, "reliability", newReliability)
        }.addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to update reliability", e)
                onResult(false)
            }
    }

}

