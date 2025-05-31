package com.example.voyago.model

import android.util.Log
import com.example.voyago.Collections
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Date

data class User(
    val id: Int = 0,
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
)

class UserModel {
    //SUBSET OF USER LIST

    fun getUsersFromUsernames(usernameList : List<String>): Flow<List<User>> = callbackFlow {//Observes update from the Server

        val query = Collections.users
            .whereIn("username", usernameList)

        val listener = query.
        orderBy("born")
            .addSnapshotListener { s, er ->
                if(s!=null)
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
    fun getUser(userId: Int) : Flow<User> = callbackFlow {
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

