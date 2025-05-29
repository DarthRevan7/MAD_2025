package com.example.voyago.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.voyago.model.*
import kotlinx.coroutines.launch

class UserViewModel(val model:UserModel): ViewModel() {
    //For now user with id = 1 is the logged user.
    var loggedUser: User =  User()
    var userFlow = viewModelScope.launch { model.getUser(1).collect { userNotFlow -> loggedUser = userNotFlow } }


    //Edit user profile
    fun editUserData(updatedUserData: User) {
        model.editUserData(updatedUserData)
    }

    //Get user information
    fun getUserData(id: Int): User {
        var user = User()
        viewModelScope.launch { model.getUser(id).collect { u -> user = u } }
        return user
    }

    //Given a list of username get a list of their ids
    fun getIdListFromUsernames(usernames: List<String>): List<Int> {

        var userList = emptyList<User>()

        viewModelScope.launch {
            model.getUsersFromUsernames(usernames).collect { users -> userList = users }
        }

        var idList : MutableList<Int> = mutableListOf()

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

}

object UserFactory : ViewModelProvider.Factory{
    private val model:UserModel = UserModel()

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return when{
            modelClass.isAssignableFrom(UserViewModel::class.java)->
                UserViewModel(model) as T
            else -> throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}