package com.example.voyago.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.voyago.model.User
import com.example.voyago.model.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(val model: UserModel) : ViewModel() {

    //For now user with id = 1 is the logged user.
    private val _loggedUser = MutableStateFlow<User>(User())
    val loggedUser: StateFlow<User> = _loggedUser

    init {
        viewModelScope.launch {
            model.getUser(1).collect { user ->
                _loggedUser.value = user
            }
        }
    }

    /*init {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                val user = model.getUser(uid)
                if (user != null) {
                    _loggedUser.value = user
                } else {
                    Log.e("UserViewModel", "User document not found for uid: $uid")
                    // You could also emit a default/fallback value or show an error
                }
            }
        } else {
            Log.e("UserViewModel", "No user is currently logged in")
        }
    }*/

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

    private val _userData = MutableStateFlow<User>(User())
    val userData: StateFlow<User> = _userData

    //Create a new user
    fun createUser(newUser: User) {
        pendingUser =
            model.createUser(newUser) { success, user ->
                if (user != null) {
                    pendingUser = user
                }
            }
    }

    //Edit user profile
    fun editUserData(updatedUserData: User) {
        model.editUserData(updatedUserData)
    }

    //Get user information
    fun getUserData(id: Int): Flow<User?> {
        return model.getUser(id)
    }


    //Given a list of username get a list of their ids
    fun getIdListFromUsernames(usernames: List<String>): List<Int> {

        var userList = emptyList<User>()

        viewModelScope.launch {
            model.getUsersFromUsernames(usernames).collect { users -> userList = users }
        }

        var idList: MutableList<Int> = mutableListOf()

        userList.forEach { idList.add(it.id) }

        return idList
    }

    fun doesUserExist(username: String): Boolean {

        var userList = emptyList<User>()

        viewModelScope.launch {
            model.getUsersFromUsernames(listOf(username)).collect { users -> userList = users }
        }

        return userList.isNotEmpty()
    }

    // Camera
    private val _profileImageUri = mutableStateOf<Uri?>(null)
    var profileImageUri: State<Uri?> = _profileImageUri

    fun setProfileImageUri(uri: Uri?) {
        _profileImageUri.value = uri
    }

    /*
    fun updateAllRatings(reviewModel: ReviewModel) {
        model.refreshAllRatings(reviewModel)
    }
     */

    // Add verification state
    private val _userVerified = MutableStateFlow(false)
    val userVerified: StateFlow<Boolean> = _userVerified

    fun setUserVerified(value: Boolean) {
        _userVerified.value = value
    }

    fun resetUserVerified() {
        _userVerified.value = false
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