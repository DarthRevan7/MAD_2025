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

//User data structure
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

    //Check if a user is valid (its field must not be black)
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


    // Suspended function to retrieve the user's profile photo URL
    suspend fun getProfilePhoto(): String? {
        return try {
            // If the profile picture URL is empty or null, return the default avatar URL
            if (profilePictureUrl.isNullOrEmpty()) {
                return com.example.voyago.StorageHelper.getImageDownloadUrl("users/default_avatar.jpg")
            }

            when {
                // If it's already a complete HTTP(S) URL, return it directly
                profilePictureUrl!!.startsWith("http") -> profilePictureUrl

                // If it's a local content URI, log a warning and return null
                profilePictureUrl!!.startsWith("content://") -> {
                    Log.w("User", "Profile picture is still a local URI: $profilePictureUrl")
                    // This should be uploaded during profile editing, not used directly
                    null
                }

                // If the value contains "/", assume it's a Firebase Storage path
                profilePictureUrl!!.contains("/") -> {
                    val storageRef = Firebase.storage.reference.child(profilePictureUrl!!)
                    // Asynchronously retrieve and return the download URL
                    storageRef.downloadUrl.await().toString()
                }

                // Otherwise, assume it's just a file name stored under the "users/" directory
                else -> {
                    val storageRef = Firebase.storage.reference.child("users/$profilePictureUrl")
                    // Asynchronously retrieve and return the download URL
                    storageRef.downloadUrl.await().toString()
                }
            }
        } catch (e: Exception) {
            // If any error occurs, log the exception and return the default avatar URL
            Log.e("User", "Failed to get profile photo URL for $profilePictureUrl", e)
            com.example.voyago.StorageHelper.getImageDownloadUrl("users/default_avatar.jpg")
        }
    }

    // Suspended function to upload a profile photo and set its storage path
    suspend fun setProfilePhoto(imageUri: android.net.Uri): Boolean {
        return try {
            // Get the file extension (e.g., jpg, png) from the URI
            val extension = getFileExtension(imageUri.toString())

            // Define the path in Firebase Storage where the avatar will be saved
            val newPath = "users/${id}_avatar.$extension"

            // Upload the image to Firebase Storage using a helper function
            val (success, url) = com.example.voyago.StorageHelper.uploadImageToStorage(
                imageUri,
                newPath
            )

            // If upload succeeded and we got a URL, update the profile picture path
            if (success && url != null) {
                // Only store the path, not the full URL
                profilePictureUrl = newPath
                true
            } else {
                // Upload failed
                false
            }
        } catch (e: Exception) {
            // Log the exception if something goes wrong during upload
            Log.e("User", "Failed to upload profile photo", e)
            false
        }
    }

}

class UserModel {

    // Function to create a new user with a unique ID using a Firestore transaction
    fun createUser(newUser: User, onResult: (Boolean, User?) -> Unit): User {
        val firestore = Firebase.firestore
        // Reference to the counter for generating unique user IDs
        val counterRef = firestore.collection("metadata").document("userCounter")

        // Default user object to return (will be updated later)
        var userToReturn = User()

        firestore.runTransaction { transaction ->
            // Get the current user ID counter
            val snapshot = transaction.get(counterRef)
            val lastUserId = snapshot.getLong("lastUserId") ?: 0
            val newUserId = lastUserId + 1

            // Update the counter with the new ID
            transaction.update(counterRef, "lastUserId", newUserId)

            // Create a new User object with the new unique ID
            val userWithId = newUser.copy(id = newUserId.toInt())

            // Reference to the new document in the 'users' collection
            val userDocRef = firestore.collection("users").document(newUserId.toString())

            // Save the user document to Firestore
            transaction.set(userDocRef, userWithId)

            // Save locally to return outside the transaction
            userToReturn = userWithId

            // Return value from transaction block
            userWithId
        }.addOnSuccessListener { user ->
            // If transaction succeeds, return the user through the callback
            onResult(true, user)
        }.addOnFailureListener { e ->
            // If transaction fails, report failure
            onResult(false, null)
        }

        // Return the user object
        return userToReturn
    }

    //SUBSET OF USER LIST

    // Function to get a list of User objects from a list of usernames using a real-time Firestore listener
    fun getUsersFromUsernames(usernameList: List<String>): Flow<List<User>> =
        callbackFlow {// Creates a Flow that emits values as Firestore sends updates

            // Build a query to retrieve users whose usernames are in the provided list
            val query = Collections.users
                .whereIn("username", usernameList)  // Firestore 'whereIn' filter

            // Add a snapshot listener to the query, ordered by user ID
            val listener = query.orderBy("id")
                .addSnapshotListener { s, er ->
                    if (s != null)
                    // Emit the list of users when data is received
                        trySend(s.toObjects(User::class.java))
                    else {
                        // If there's an error, log it and emit an empty list
                        Log.e("Error", er.toString())
                        trySend(emptyList())
                    }
                }
            // Ensure the listener is removed when the flow collection is cancelled
            awaitClose {
                listener.remove()
            }
        }

    // Function to retrieve a list of users by their user IDs using a Firestore real-time listener
    fun getUsers(userIds: List<Int>): Flow<List<User>> = callbackFlow {

        // If the userIds list is empty, immediately send an empty list and close the flow
        if (userIds.isEmpty()) {
            trySend(emptyList())
            close()     // Close the flow to prevent further emissions
            return@callbackFlow
        }

        // Build a Firestore query to get users whose "id" field matches any value in the userIds list
        val query = Collections.users
            .whereIn("id", userIds)     // Firestore supports up to 10 items in whereIn()

        // Add a snapshot listener to observe real-time updates from Firestore
        val listener = query.addSnapshotListener { snapshot, error ->
            if (snapshot != null) {
                // Successfully retrieved data, convert it to a list of User objects and send it
                trySend(snapshot.toObjects(User::class.java))
            } else {
                // If there's an error, log it and emit an empty list
                Log.e("Error", error.toString())
                trySend(emptyList())
            }
        }

        // Clean up: remove the Firestore listener when the flow is no longer being collected
        awaitClose {
            listener.remove()
        }
    }

    // Returns a Flow that emits the User with the given userId using Firestore's real-time listener
    fun getUser(userId: Int): Flow<User> = callbackFlow {
        // Validate the input userId. If it's invalid (e.g., <= 0), close the flow with an error
        if (userId <= 0) {
            close(IllegalArgumentException("Invalid userId"))
            return@callbackFlow
        }

        // Create a Firestore query to find the user document where "id" matches the given userId
        val query = Collections.users
            .whereEqualTo("id", userId)

        // Attach a real-time listener to the query
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Log the error and close the flow with the exception
                Log.e("getUser", "Errore nella query: ", error)
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                // Convert the snapshot to a User object and emit the first match
                val user = snapshot.toObjects(User::class.java).firstOrNull()
                if (user != null) {
                    trySend(user).isSuccess     // Emit the user to the flow
                }
            }
        }

        // Ensure the listener is removed when the flow collection is cancelled
        awaitClose {
            listener.remove()
        }
    }

    //EDIT USER

    // Updates a user's data in Firestore with the provided User object
    fun editUserData(updatedUser: User) {
        // Convert the user's ID to a string to match the Firestore document ID format
        val userId = updatedUser.id.toString()
        // Get a reference to the user's document in the "users" collection
        val docRef = Collections.users.document(userId)

        // Overwrite the existing user document with the new data
        docRef.set(updatedUser)
    }

    // Retrieves a user from Firestore based on their email and emits the result as a Flow
    fun getUserByEmail(email: String): Flow<User?> = callbackFlow {

        // Create a query to the "users" collection where the email matches
        val query = FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("email", email)
            .limit(1)       // Limit to the first matching document


        // Listen for real-time updates to the query result
        val listener = query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                // If an error occurs, close the flow with the error
                close(error)
                return@addSnapshotListener
            }

            // Convert the first matching document to a User object, or null if not found
            val user = snapshots?.documents?.firstOrNull()?.toObject(User::class.java)

            // Emit the result to the Flow
            trySend(user)
        }

        // Remove the listener when the flow is closed or cancelled
        awaitClose { listener.remove() }
    }

    //MANAGEMENT OF RELIABILITY

    // Updates a user's reliability score in Firestore by applying a delta value
    fun updateUserReliability(userId: Int, delta: Int, onResult: (Boolean) -> Unit) {
        // Get a reference to the user's document in the "users" collection
        val userRef =
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId.toString())

        // Start a Firestore transaction to safely update the reliability value
        FirebaseFirestore.getInstance().runTransaction { transaction ->
            // Read the current snapshot of the user document
            val snapshot = transaction.get(userRef)

            // Get the current reliability value (default to 100 if not found)
            val currentReliability = snapshot.getLong("reliability")?.toInt() ?: 100

            // Calculate the new reliability, ensuring it's between 0 and 100
            val newReliability = (currentReliability + delta).coerceIn(0, 100)

            // Update the "reliability" field with the new value
            transaction.update(userRef, "reliability", newReliability)

        }
            // If the transaction was successful, invoke the callback with true
            .addOnSuccessListener { onResult(true) }
            // If the transaction failed, log the error and invoke the callback with false
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to update reliability", e)
                onResult(false)
            }
    }

}

