package com.example.voyago.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.voyago.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    val chatModel: ChatModel,
    val userModel: UserModel,       //Need this for user IDs
    val tripModel: TripModel        //Need this for trip IDs
    ) : ViewModel() {

        init {

        }

        val privateChat : MutableStateFlow<PrivateChat> = MutableStateFlow(PrivateChat())

    fun LoadChats() {
        viewModelScope.launch {
            chatModel.getPrivateChat(1,2)
            privateChat.value = chatModel.privateChat.value
            Log.d("ChatViewModel", privateChat.value.toString())
        }
    }


}

object ChatFactory : ViewModelProvider.Factory {
    private val chatModel = ChatModel()

    private val tripModel: TripModel = TripModel()
    private val userModel: UserModel = UserModel()
    private val reviewModel: ReviewModel = ReviewModel()

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {

        val savedStateHandle = extras.createSavedStateHandle()

        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(ChatViewModel::class.java) ->
                ChatViewModel(chatModel, userModel, tripModel) as T

            modelClass.isAssignableFrom(UserViewModel::class.java) ->
                UserViewModel(userModel) as T

            modelClass.isAssignableFrom(TripViewModel::class.java) ->
                TripViewModel(tripModel, userModel, reviewModel, savedStateHandle) as T

            else -> throw IllegalArgumentException("Unknown ViewModel")
        }
    }


}