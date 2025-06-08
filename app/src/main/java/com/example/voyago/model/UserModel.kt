package com.example.voyago.model

import android.util.Log
import com.example.voyago.Collections
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.Serializable
import java.util.Date

data class User(
    var id: Int = 0,
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
}

class UserModel {

    /*private val firestore = FirebaseFirestore.getInstance()

    suspend fun getUser(uid: String): User? {
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to get user", e)
            null
        }
    }*/

    //SUBSET OF USER LIST

    fun getUsersFromUsernames(usernameList: List<String>): Flow<List<User>> =
        callbackFlow {//Observes update from the Server

            val query = Collections.users
                .whereIn("username", usernameList)

            val listener = query.orderBy("born")
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

    fun getUserByUid(uid: String): Flow<User?> = callbackFlow {
        val ref = FirebaseFirestore.getInstance().collection("users").document(uid)
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val user = snapshot?.toObject(User::class.java)
            trySend(user)
        }
        awaitClose { listener.remove() }
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


    // -------------------------------------------------
    /*
    private var _users = privateUsers
    var users = _users

    //SUBSET OF USER LIST

    //Get a list of user given their ids
    fun getUsers(ids: List<Int>): List<UserData> {
        return _users.value.filter { it.id in ids }
    }

    //GET USER INFORMATION



    fun getUserDataById(id: Int): UserData {
        return _users.value.find { it.id == id }
            ?: throw NoSuchElementException("User with ID $id not found")
    }

    //EDIT USER

    //Edit user information
    fun editUserData(updatedUserData: UserData): List<UserData> {
        _users.value = _users.value.map {
            if (it.id == updatedUserData.id) updatedUserData else it
        }
        return _users.value
    }

    //Calculate average user rating by id
    fun refreshAllRatings(reviewModel: ReviewModel) {
        _users.value = _users.value.map { user ->
            user.copy(rating = reviewModel.calculateRatingById(user.id))
        }

    */

}

