package com.example.voyago.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.voyago.model.*

class UserViewModel(val model:UserModel): ViewModel() {
    //For now user with id = 1 is the logged user.
    var loggedUser: UserData = model.getUserDataById(1)

    //Edit user profile
    fun editUserData(updatedUserData: UserData): List<UserData> {
        val updatedList = model.editUserData(updatedUserData)
        return updatedList
    }

    //Get user information
    fun getUserData(id: Int): UserData {
        return model.getUserDataById(id)
    }

    //Given a list of username get a list of their ids
    fun getIdListFromUsernames(usernames: List<String>): List<Int> {
        return model.users.value.filter { user ->
            usernames.contains(user.username)
        }.map { user ->
            user.id
        }
    }

    fun doesUserExist(username: String): Boolean {
        return model.users.value.any { user ->
            user.username == username
        }
    }

    // Camera
    private val _profileImageUri = mutableStateOf<Uri?>(null)
    var profileImageUri: State<Uri?> = _profileImageUri

    fun setProfileImageUri(uri: Uri?) {
        _profileImageUri.value = uri
    }

    fun updateAllRatings(reviewModel: ReviewModel) {
        model.refreshAllRatings(reviewModel)
    }

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