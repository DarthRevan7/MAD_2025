package com.example.voyago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.voyago.model.*

class UserViewModel(val model:Model): ViewModel() {
    //For now user with id = 1 is the logged user.
    var loggedUser: UserData = model.getUserDataById(1)

}

object UserFactory : ViewModelProvider.Factory{
    private val model:Model = Model()

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return when{
            modelClass.isAssignableFrom(UserViewModel::class.java)->
                UserViewModel(model) as T
            else -> throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}