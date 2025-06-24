@file:Suppress("DEPRECATION")

package com.example.voyago.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.voyago.Collections
import com.example.voyago.model.User
import com.example.voyago.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserViewModel(val model: UserModel) : ViewModel() {

    //Logged in user
    private val _loggedUser = MutableStateFlow<User>(User())
    val loggedUser: StateFlow<User> = _loggedUser

    // Initialize the ViewModel by fetching the logged-in user from Firebase
    init {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val email = firebaseUser?.email

        if (email != null) {
            viewModelScope.launch {
                model.getUserByEmail(email).collect { user ->
                    if (user != null) {
                        _loggedUser.value = user
                        Log.d("UserViewModel", "Logged user loaded: ${user.firstname}")
                    } else {
                        Log.e("UserViewModel", "No user found with email: $email")
                    }
                }
            }
        } else {
            Log.e("UserViewModel", "No Firebase user logged in")
        }
    }


    //  添加这个方法来处理头像上传和用户更新
    fun updateUserWithProfileImage(
        updatedUser: User,
        newImageUri: Uri?,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (newImageUri != null && newImageUri.toString() != updatedUser.profilePictureUrl) {
                    Log.d("UserViewModel", "Uploading new profile image: $newImageUri")

                    // 上传图片到 Firebase Storage
                    val uploadSuccess = updatedUser.setProfilePhoto(newImageUri)

                    if (uploadSuccess) {
                        Log.d("UserViewModel", "Profile image uploaded successfully")
                        // 更新用户数据
                        editUserData(updatedUser)
                        onResult(true)
                    } else {
                        Log.e("UserViewModel", "Failed to upload profile image")
                        onResult(false)
                    }
                } else {
                    // 没有新图片，直接更新其他信息
                    Log.d("UserViewModel", "No new image, updating other profile info")
                    editUserData(updatedUser)
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error updating user profile", e)
                onResult(false)
            }
        }
    }

    var pendingUser: User? = null

    fun storeUser(user: User) {
        if (user.id == -1) {
            pendingUser = user
        } else {
            throw IllegalArgumentException("User ID must be -1 to store a pending user")
        }
    }

    fun clearUser() {
        pendingUser = null
    }

    //Create a new user
    fun createUser(newUser: User) {
        pendingUser =
            model.createUser(newUser) { success, user ->
                if (user != null) {
                    pendingUser = user
                }
            }
    }

    var account: GoogleSignInAccount? = null

    //Edit user profile
    fun editUserData(updatedUserData: User) {
        model.editUserData(updatedUserData)
    }

    //Get user information
    fun getUserData(id: Int): Flow<User?> {
        return model.getUser(id)
    }

    // Camera
    private val _profileImageUri = mutableStateOf<Uri?>(null)
    var profileImageUri: State<Uri?> = _profileImageUri

    // Set the profile image URI
    fun setProfileImageUri(uri: Uri?) {
        _profileImageUri.value = uri
    }

    // Add verification state
    private val _userVerified = MutableStateFlow(false)
    val userVerified: StateFlow<Boolean> = _userVerified

    // Set the user verification state
    fun setUserVerified(value: Boolean) {
        _userVerified.value = value
    }

    // Reset the user verification state
    fun resetUserVerified() {
        _userVerified.value = false
    }

    // Get a list of user IDs that match the given type of travel
    fun getMatchingUserIdsByTypeTravel(
        typeTravelInput: List<String>,
        onResult: (List<Int>) -> Unit
    ) {
        Collections.users.get()
            .addOnSuccessListener { snapshot ->
                val allUsers = snapshot.toObjects(User::class.java)
                val filteredUsers = allUsers.filter { user ->
                    user.typeTravel.map { it.name }.any { it in typeTravelInput }
                }

                val matchingIds = filteredUsers.map { it.id }.toSet().toList() // Remove duplicates
                onResult(matchingIds)
            }
            .addOnFailureListener { error ->
                Log.e("getMatchingUserIds", "Error fetching users: ${error.message}")
                onResult(emptyList())
            }
    }

    // Check if a user exists asynchronously and return the user object if it exists
    fun checkUserExistsAsync(username: String, callback: (Boolean, User) -> Unit) {
        viewModelScope.launch {
            var user = User()
            try {
                Log.d("UserViewModel", "Checking if user exists: $username")

                val result = Collections.users
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (!snapshot.isEmpty) {
                            val userGot = snapshot.documents[0].toObject(User::class.java)
                            if (userGot != null) {
                                user = userGot
                            }
                        }
                    }
                    .await()

                val exists = !result.isEmpty
                Log.d("UserViewModel", "User '$username' exists: $exists, user $user")

                // 确保在主线程回调
                withContext(Dispatchers.Main) {
                    callback(exists, user)
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error checking user existence", e)
                withContext(Dispatchers.Main) {
                    callback(false, user)
                }
            }
        }
    }

    fun updateUserReliability(userId: Int, delta: Int, onResult: (Boolean) -> Unit) =
        model.updateUserReliability(userId, delta) { success ->
            onResult(success)
        }

    fun getAllOtherUserIds(loggedUserId: Int, onResult: (List<String>) -> Unit) {
        Collections.users.get()
            .addOnSuccessListener { snapshot ->
                val allUsers = snapshot.toObjects(User::class.java)
                val otherUserIds = allUsers
                    .filter { it.id != loggedUserId }
                    .map { it.id.toString() }

                onResult(otherUserIds)
            }
            .addOnFailureListener { error ->
                Log.e("getAllOtherUserIds", "Error fetching users: ${error.message}")
                onResult(emptyList())
            }
    }

}

object UserFactory : ViewModelProvider.Factory {
    private val model: UserModel = UserModel()

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(UserViewModel::class.java) ->
                UserViewModel(model) as T

            else -> throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}