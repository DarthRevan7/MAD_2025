package com.example.voyago.viewmodel

import androidx.lifecycle.*
import com.example.voyago.model.UserRepository
import com.example.voyago.model.*
import kotlinx.coroutines.launch


class MyProfileViewModel : ViewModel() {
    val userRepository: UserRepository = UserRepository()

    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> = _userData

    fun getUserData(){
        viewModelScope.launch {
            val userResult = userRepository.fetchUserData()
            _userData.postValue(userResult)
        }

    }

}